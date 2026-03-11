# personalwebsite Contact Form Lambda

Java 21 Lambda function that processes contact form submissions from the personalwebsite landing site. Receives JSON via API Gateway HTTP API, validates input, formats an email, and sends it through Amazon SES.

## Prerequisites

- Java 21 (Corretto)
- Gradle 8.12+ (wrapper included)

## Setup

### Generate Gradle Wrapper

If the wrapper files (`gradlew`, `gradlew.bat`, `gradle/`) are missing, generate them with a locally installed Gradle:

```bash
gradle wrapper
```

### Generate Eclipse Project Files

```bash
./gradlew eclipse
```

This creates `.classpath` and `.project` files so you can import the project into Eclipse via **File → Import → Existing Projects into Workspace**.

## Build

```bash
./gradlew shadowJar
```

The fat JAR is produced at `build/libs/contact-form-handler-all.jar`. This is the artifact the CDK stack references for Lambda deployment.

## Run Tests

```bash
./gradlew test
```

## Project Structure

```
src/main/java/com/personalwebsite/contact/
├── ContactFormHandler.java   # Lambda entry point
├── FormData.java             # Request POJO
├── InputValidator.java       # Required field validation & sanitization
└── EmailFormatter.java       # Email subject & body formatting
```

## Handler

`com.personalwebsite.contact.ContactFormHandler::handleRequest`

## Environment Variables

| Variable | Description |
|---|---|
| `RECIPIENT_EMAIL` | Email address for both sender (SES verified identity) and recipient |

## Request / Response

POST JSON with fields: `firstName` (required), `lastName`, `email` (required), `phone`, `company` (required), `title`, `industry`, `productLines`, `quotingProcess`, `message`.

- 200: `{"message": "Your submission has been received. We will be in touch within one business day."}`
- 400: `{"error": "Validation failed", "fields": ["firstName", "email"]}` or `{"error": "Invalid request body"}`
- 500: `{"error": "An error occurred processing your request. Please try again or email us directly."}`
