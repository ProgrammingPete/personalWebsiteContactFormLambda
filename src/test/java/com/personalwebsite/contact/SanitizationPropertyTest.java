package com.personalwebsite.contact;

import com.personalwebsite.contact.InputValidator;

import net.jqwik.api.*;

/**
 * Feature: contact-form-processing, Property 2: Sanitization removes email header injection characters
 * Validates: Requirements 2.3
 */
class SanitizationPropertyTest {

    private final InputValidator validator = new InputValidator();

    /**
     * For any string input containing \r or \n characters, the sanitizer produces output
     * that contains no \r or \n characters, and all other content is preserved.
     */
    @Property(tries = 100)
    @Tag("Feature: contact-form-processing, Property 2: Sanitization removes email header injection characters")
    void sanitizationRemovesInjectionCharacters(@ForAll("stringsWithInjection") String input) {
        String sanitized = validator.sanitize(input);

        // Sanitized output must not contain \r or \n
        assert !sanitized.contains("\r") : "Sanitized output still contains \\r: " + repr(sanitized);
        assert !sanitized.contains("\n") : "Sanitized output still contains \\n: " + repr(sanitized);

        // All non-injection content must be preserved
        String expectedContent = input.replace("\r", "").replace("\n", "");
        assert sanitized.equals(expectedContent) :
                "Non-injection content not preserved. Expected: " + repr(expectedContent) +
                " but got: " + repr(sanitized);
    }

    @Property(tries = 100)
    @Tag("Feature: contact-form-processing, Property 2: Sanitization removes email header injection characters")
    void sanitizeReturnsNullForNullInput() {
        assert validator.sanitize(null) == null : "sanitize(null) should return null";
    }

    @Provide
    Arbitrary<String> stringsWithInjection() {
        // Generate strings that mix normal characters with \r and \n
        Arbitrary<String> normalChars = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars(' ', '@', '.', '-', '_')
                .ofMinLength(0).ofMaxLength(30);

        Arbitrary<String> injectionChars = Arbitraries.of("\r", "\n", "\r\n", "\n\r");

        // Combine normal strings with injection characters interspersed
        return Combinators.combine(normalChars, injectionChars, normalChars)
                .as((prefix, injection, suffix) -> prefix + injection + suffix);
    }

    private String repr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\r", "\\r").replace("\n", "\\n") + "\"";
    }
}
