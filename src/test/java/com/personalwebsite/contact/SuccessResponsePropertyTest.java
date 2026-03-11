package com.personalwebsite.contact;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;

import net.jqwik.api.*;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

/**
 * Feature: contact-form-processing, Property 5: Successful submissions return HTTP 200
 * Validates: Requirements 4.2
 */
class SuccessResponsePropertyTest {

    private final Gson gson = new Gson();

    /**
     * For any valid form submission where SES delivery succeeds,
     * the Lambda returns HTTP 200 with a confirmation message.
     */
    @Property(tries = 100)
    @Tag("Feature: contact-form-processing, Property 5: Successful submissions return HTTP 200")
    void validSubmissionsReturnHttp200(@ForAll("validFormData") FormData form) {
        // Mock SES client to succeed
        SesClient mockSes = Mockito.mock(SesClient.class);
        Mockito.when(mockSes.sendEmail(Mockito.any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("test-id").build());

        // Set required env var
        try {
            setEnv("RECIPIENT_EMAIL", "test@example.com");
        } catch (Exception e) {
            // If we can't set env, skip — handled below
        }

        ContactFormHandler handler = new ContactFormHandler(mockSes);

        String jsonBody = gson.toJson(form);
        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody(jsonBody)
                .build();

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, null);

        assert response.getStatusCode() == 200 :
                "Expected HTTP 200 but got " + response.getStatusCode() +
                " for form: " + jsonBody + "\nBody: " + response.getBody();

        assert response.getBody().contains("received") :
                "Response body should contain confirmation message. Got: " + response.getBody();
    }

    @Provide
    Arbitrary<FormData> validFormData() {
        Arbitrary<String> nonEmptyAlpha = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(20);

        Arbitrary<String> emailLike = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8),
                Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(6)
        ).as((user, domain) -> user + "@" + domain + ".com");

        Arbitrary<String> optionalString = Arbitraries.oneOf(
                Arbitraries.just(null),
                nonEmptyAlpha
        );

        return Combinators.combine(
                nonEmptyAlpha,  // firstName
                optionalString, // lastName
                emailLike,      // email
                optionalString, // phone
                nonEmptyAlpha,  // company
                optionalString, // title
                optionalString  // industry
        ).as((firstName, lastName, email, phone, company, title, industry) -> {
            FormData form = new FormData();
            form.setFirstName(firstName);
            form.setLastName(lastName);
            form.setEmail(email);
            form.setPhone(phone);
            form.setCompany(company);
            form.setTitle(title);
            form.setIndustry(industry);
            return form;
        });
    }

    @SuppressWarnings("unchecked")
    private static void setEnv(String key, String value) throws Exception {
        java.util.Map<String, String> env = System.getenv();
        java.lang.reflect.Field field = env.getClass().getDeclaredField("m");
        field.setAccessible(true);
        ((java.util.Map<String, String>) field.get(env)).put(key, value);
    }
}
