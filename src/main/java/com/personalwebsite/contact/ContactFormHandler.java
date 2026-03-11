package com.personalwebsite.contact;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ContactFormHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final Logger logger = Logger.getLogger(ContactFormHandler.class.getName());

    private final SesClient sesClient;
    private final Gson gson;
    private final InputValidator validator;
    private final EmailFormatter formatter;

    public ContactFormHandler() {
        this.sesClient = SesClient.create();
        this.gson = new Gson();
        this.validator = new InputValidator();
        this.formatter = new EmailFormatter();
    }

    // Constructor for testing with injected SES client
    public ContactFormHandler(SesClient sesClient) {
        this.sesClient = sesClient;
        this.gson = new Gson();
        this.validator = new InputValidator();
        this.formatter = new EmailFormatter();
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            FormData form;
            try {
                form = gson.fromJson(event.getBody(), FormData.class);
                if (form == null) {
                    return buildResponse(400, "{\"error\":\"Invalid request body\"}");
                }
            } catch (JsonSyntaxException e) {
                logger.info("Malformed JSON in request body");
                return buildResponse(400, "{\"error\":\"Invalid request body\"}");
            }

            // Sanitize all fields first
            validator.sanitizeAll(form);

            // Validate required fields
            List<String> invalidFields = validator.validate(form);
            if (!invalidFields.isEmpty()) {
                logger.info("Validation failed for fields: " + invalidFields);
                String fieldsJson = gson.toJson(invalidFields);
                return buildResponse(400, "{\"error\":\"Validation failed\",\"fields\":" + fieldsJson + "}");
            }

            // Format and send email
            String recipientEmail = System.getenv("RECIPIENT_EMAIL");
            String ccEmail = System.getenv("CC_EMAIL");
            String subject = formatter.buildSubject(form);
            String body = formatter.buildBody(form);

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(recipientEmail)
                    .destination(Destination.builder()
                            .toAddresses(recipientEmail)
                            .ccAddresses(ccEmail)
                            .build())
                    .replyToAddresses(form.getEmail())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .text(Content.builder().data(body).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build();

            sesClient.sendEmail(emailRequest);
            logger.info("Contact form submission processed successfully");
            logger.info("Email sent to " + recipientEmail + " with CC to " + ccEmail);
            logger.info("Email body: " + body);

            return buildResponse(200,
                    "{\"message\":\"Your submission has been received. We will be in touch within one business day.\"}");

        } catch (Exception e) {
            System.err.println("ERROR: Failed to process contact form submission");
            e.printStackTrace();
            return buildResponse(500,
                    "{\"error\":\"An error occurred processing your request. Please try again or email us directly.\"}");
        }
    }

    private APIGatewayV2HTTPResponse buildResponse(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type");
        headers.put("Access-Control-Allow-Methods", "POST,OPTIONS");
        headers.put("Content-Type", "application/json");

        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body)
                .build();
    }
}
