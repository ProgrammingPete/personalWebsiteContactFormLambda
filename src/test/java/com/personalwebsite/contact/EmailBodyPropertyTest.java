package com.personalwebsite.contact;

import com.personalwebsite.contact.EmailFormatter;
import com.personalwebsite.contact.FormData;

import net.jqwik.api.*;

/**
 * Feature: contact-form-processing, Property 3: Email body contains all provided form field values
 * Validates: Requirements 3.1
 */
class EmailBodyPropertyTest {

    private final EmailFormatter formatter = new EmailFormatter();

    /**
     * For any valid FormData with non-empty field values, every non-empty field value
     * appears in the formatted email body.
     */
    @Property(tries = 100)
    @Tag("Feature: contact-form-processing, Property 3: Email body contains all provided form field values")
    void emailBodyContainsAllProvidedFieldValues(@ForAll("validFormData") FormData form) {
        String body = formatter.buildBody(form);

        assertFieldInBody(body, form.getFirstName(), "firstName");
        assertFieldInBody(body, form.getLastName(), "lastName");
        assertFieldInBody(body, form.getEmail(), "email");
        assertFieldInBody(body, form.getPhone(), "phone");
        assertFieldInBody(body, form.getCompany(), "company");
        assertFieldInBody(body, form.getTitle(), "title");
        assertFieldInBody(body, form.getIndustry(), "industry");
        assertFieldInBody(body, form.getProductLines(), "productLines");
        assertFieldInBody(body, form.getQuotingProcess(), "quotingProcess");
        assertFieldInBody(body, form.getMessage(), "message");
    }

    private void assertFieldInBody(String body, String value, String fieldName) {
        if (value != null && !value.isEmpty()) {
            assert body.contains(value) :
                    "Email body does not contain " + fieldName + " value: \"" + value + "\"\nBody:\n" + body;
        }
    }

    @Provide
    Arbitrary<FormData> validFormData() {
        Arbitrary<String> nonEmptyString = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(30);
        Arbitrary<String> optionalString = Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.just(""),
                nonEmptyString
        );

        // jqwik Combinators.combine supports max 8 params, so build in two stages
        Arbitrary<FormData> baseForm = Combinators.combine(
                nonEmptyString,  // firstName (required)
                optionalString,  // lastName
                nonEmptyString,  // email (required)
                optionalString,  // phone
                nonEmptyString,  // company (required)
                optionalString,  // title
                optionalString,  // industry
                optionalString   // productLines
        ).as((firstName, lastName, email, phone, company, title, industry, productLines) -> {
            FormData form = new FormData();
            form.setFirstName(firstName);
            form.setLastName(lastName);
            form.setEmail(email);
            form.setPhone(phone);
            form.setCompany(company);
            form.setTitle(title);
            form.setIndustry(industry);
            form.setProductLines(productLines);
            return form;
        });

        // Add remaining optional fields
        return Combinators.combine(baseForm, optionalString, optionalString)
                .as((form, quotingProcess, message) -> {
                    form.setQuotingProcess(quotingProcess);
                    form.setMessage(message);
                    return form;
                });
    }
}
