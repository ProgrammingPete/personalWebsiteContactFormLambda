package com.personalwebsite.contact;

import net.jqwik.api.*;

/**
 * Feature: contact-form-processing, Property 1: Validation correctly identifies missing or empty required fields
 * Validates: Requirements 2.1, 2.2
 */
class InputValidatorPropertyTest {


    @Provide
    Arbitrary<String> fieldValueOrInvalid() {
        return Arbitraries.oneOf(
                // null value
                Arbitraries.just(null),
                // empty string
                Arbitraries.just(""),
                // whitespace-only strings
                Arbitraries.of("   ", " ", "\t", "  \t  "),
                // valid non-empty strings
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50)
        );
    }
}
