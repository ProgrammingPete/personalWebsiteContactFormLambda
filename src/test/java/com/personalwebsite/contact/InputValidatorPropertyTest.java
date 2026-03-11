package com.personalwebsite.contact;

import net.jqwik.api.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Feature: contact-form-processing, Property 1: Validation correctly identifies missing or empty required fields
 * Validates: Requirements 2.1, 2.2
 */
class InputValidatorPropertyTest {

    private final InputValidator validator = new InputValidator();

    /**
     * For any combination of present/missing/null/whitespace-only values for firstName, email, company,
     * the validator returns exactly the field names that are invalid.
     */
    @Property(tries = 100)
    @Tag("Feature: contact-form-processing, Property 1: Validation correctly identifies missing or empty required fields")
    void validationIdentifiesMissingOrEmptyRequiredFields(
            @ForAll("fieldValueOrInvalid") String firstName,
            @ForAll("fieldValueOrInvalid") String email,
            @ForAll("fieldValueOrInvalid") String company
    ) {
        FormData form = new FormData();
        form.setFirstName(firstName);
        form.setEmail(email);
        form.setCompany(company);

        List<String> result = validator.validate(form);

        List<String> expected = new ArrayList<>();
        if (isBlank(firstName)) expected.add("firstName");
        if (isBlank(email)) expected.add("email");
        if (isBlank(company)) expected.add("company");

        assert result.equals(expected) :
                "Expected " + expected + " but got " + result +
                " for firstName=" + repr(firstName) + ", email=" + repr(email) + ", company=" + repr(company);
    }

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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String repr(String s) {
        return s == null ? "null" : "\"" + s + "\"";
    }
}
