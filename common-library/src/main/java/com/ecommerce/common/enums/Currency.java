package com.ecommerce.common.enums;

import com.ecommerce.common.kafka.EventType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Currency {

    INR("INR"),
    USD("USD");

    private final String code;

    Currency(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    @JsonCreator
    public static Currency fromCode(String code) {

        if (code == null || code.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(currency -> currency.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}