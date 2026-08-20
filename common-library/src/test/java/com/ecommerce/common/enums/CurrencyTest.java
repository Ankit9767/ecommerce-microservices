package com.ecommerce.common.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the currency representation: the Currency enum must serialize to its
 * stable 3-letter ISO code and tolerate unknown codes.
 */
class CurrencyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesToStableIsoCodes() throws Exception {

        assertThat(serialize(Currency.INR)).isEqualTo("INR");
        assertThat(serialize(Currency.USD)).isEqualTo("USD");
    }

    @Test
    void deserializesFromStableIsoCodes() throws Exception {

        assertThat(deserialize("INR")).isEqualTo(Currency.INR);
        assertThat(deserialize("USD")).isEqualTo(Currency.USD);
    }

    @Test
    void unknownCodeMapsToNullGracefully() throws Exception {

        assertThat(deserialize("EUR")).isNull();
        assertThat(deserialize(null)).isNull();
        assertThat(deserialize("")).isNull();
    }

    private String serialize(Currency currency) throws Exception {
        return objectMapper.writeValueAsString(currency).replace("\"", "");
    }

    private Currency deserialize(String code) throws Exception {
        return objectMapper.readValue(
                code == null ? "null" : "\"" + code + "\"",
                Currency.class
        );
    }
}