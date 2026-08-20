package com.ecommerce.common.enums;

import com.ecommerce.common.kafka.EventType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * ISO 4217 currency codes carried on order and payment events.
 *
 * <p>Like {@link EventType}, the enum's stable 3-letter code is the wire/DB
 * representation, so {@link JsonValue}/{@link JsonCreator} keep serialized
 * values byte-for-byte identical to the plain-string format they replace.
 * Unknown codes map to {@code null} rather than failing deserialisation.</p>
 *
 * <p>INVARIANT: {@code name()} must equal {@code code()} for every constant.
 * Entities persist the enum via {@code @Enumerated(EnumType.STRING)}, which
 * stores {@code name()}, while JSON uses the {@code @JsonValue} code. A future
 * constant whose name differs from its code (e.g. {@code EURO("EUR")}) would
 * break both the {@code length = 3} DB column and the byte-for-byte wire
 * contract.</p>
 */
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