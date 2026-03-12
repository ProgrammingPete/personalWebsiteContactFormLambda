package com.personalwebsite.contact;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ContactFormHandler.
 * Requirements: 2.1, 2.2, 3.1, 3.2, 4.1, 4.2, 4.3
 */
@ExtendWith(MockitoExtension.class)
class ContactFormHandlerTest {

    @Mock
    private SesClient mockSesClient;

    private ContactFormHandler handler;
    private final Gson gson = new Gson();

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void setUpEnv() throws Exception {
        Map<String, String> env = System.getenv();
        java.lang.reflect.Field field = env.getClass().getDeclaredField("m");
        field.setAccessible(true);
        ((Map<String, String>) field.get(env)).put("RECIPIENT_EMAIL", "recipient@example.com");
    }

    @BeforeEach
    void setUp() {
        handler = new ContactFormHandler(mockSesClient);
    }

    // --- Validation tests ---

    @Test
    void allFieldsPresent_returns200() {
        when(mockSesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("msg-1").build());

        FormData form = createValidForm();
        APIGatewayV2HTTPResponse response = invokeHandler(form);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("received"));
    }

    @Test
    void missingEmail_returns400WithFieldName() {
        FormData form = createValidForm();
        form.setEmail(null);
        APIGatewayV2HTTPResponse response = invokeHandler(form);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("email"));
    }

    @Test
    void missingCompany_returns400WithFieldName() {
        FormData form = createValidForm();
        form.setCompany(null);
        APIGatewayV2HTTPResponse response = invokeHandler(form);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("company"));
    }

    // --- Malformed JSON test ---

    @Test
    void malformedJsonBody_returns400() {
        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody("this is not json{{{")
                .build();

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, null);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid request body"));
    }

    @Test
    void nullBody_returns400() {
        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody(null)
                .build();

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, null);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid request body"));
    }

    // --- SES error test ---

    @Test
    void sesError_returns500WithUserFriendlyMessage() {
        when(mockSesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder().message("SES failure").build());

        FormData form = createValidForm();
        APIGatewayV2HTTPResponse response = invokeHandler(form);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("An error occurred processing your request"));
    }

    // --- CORS headers tests ---

    @Test
    void successResponse_hasCorsHeaders() {
        when(mockSesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("msg-1").build());

        FormData form = createValidForm();
        APIGatewayV2HTTPResponse response = invokeHandler(form);

        assertCorsHeaders(response);
    }

    @Test
    void validationErrorResponse_hasCorsHeaders() {
        FormData form = new FormData(); // all fields null
        APIGatewayV2HTTPResponse response = invokeHandler(form);

        assertEquals(400, response.getStatusCode());
        assertCorsHeaders(response);
    }

    @Test
    void serverErrorResponse_hasCorsHeaders() {
        when(mockSesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(SesException.builder().message("SES failure").build());

        FormData form = createValidForm();
        APIGatewayV2HTTPResponse response = invokeHandler(form);

        assertEquals(500, response.getStatusCode());
        assertCorsHeaders(response);
    }

    @Test
    void malformedJsonResponse_hasCorsHeaders() {
        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody("{bad json")
                .build();

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, null);

        assertEquals(400, response.getStatusCode());
        assertCorsHeaders(response);
    }

    // --- Email formatting tests ---


    @Test
    void emailFormatting_minimalRequiredFieldsOnly() {
        EmailFormatter formatter = new EmailFormatter();
        FormData form = new FormData();
        form.setName("Jane");
        form.setEmail("jane@test.com");
        form.setCompany("TestCo");

        String body = formatter.buildBody(form);
        String subject = formatter.buildSubject(form);

        assertTrue(body.contains("Jane"));
        assertTrue(body.contains("jane@test.com"));
        assertTrue(body.contains("TestCo"));
        assertTrue(subject.contains("TestCo"));
        // Optional fields should not cause errors — body should still be well-formed
        assertTrue(body.contains("Name:"));
        assertTrue(body.contains("Email:"));
        assertTrue(body.contains("Company:"));
    }

    // --- Helper methods ---

    private FormData createValidForm() {
        FormData form = new FormData();
        form.setName("John");
        form.setEmail("john@example.com");
        form.setCompany("Acme Corp");
        return form;
    }

    private APIGatewayV2HTTPResponse invokeHandler(FormData form) {
        String jsonBody = gson.toJson(form);
        APIGatewayV2HTTPEvent event = APIGatewayV2HTTPEvent.builder()
                .withBody(jsonBody)
                .build();
        return handler.handleRequest(event, null);
    }

    private void assertCorsHeaders(APIGatewayV2HTTPResponse response) {
        Map<String, String> headers = response.getHeaders();
        assertNotNull(headers, "Response headers should not be null");
        assertEquals("*", headers.get("Access-Control-Allow-Origin"));
        assertEquals("Content-Type", headers.get("Access-Control-Allow-Headers"));
        assertEquals("POST,OPTIONS", headers.get("Access-Control-Allow-Methods"));
        assertEquals("application/json", headers.get("Content-Type"));
    }
}
