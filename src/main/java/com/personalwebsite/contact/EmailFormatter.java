package com.personalwebsite.contact;

public class EmailFormatter {

    public String buildSubject(FormData form) {
        return "Contact Form Submission from " + form.getCompany();
    }

    public String buildBody(FormData form) {
        StringBuilder sb = new StringBuilder();
        sb.append("New Contact Form Submission\n");
        sb.append("===========================\n");
        sb.append("\n");
        sb.append("Name: ").append(valueOrEmpty(form.getFirstName()))
          .append(" ").append(valueOrEmpty(form.getLastName())).append("\n");
        sb.append("Email: ").append(valueOrEmpty(form.getEmail())).append("\n");
        sb.append("Phone: ").append(valueOrEmpty(form.getPhone())).append("\n");
        sb.append("Company: ").append(valueOrEmpty(form.getCompany())).append("\n");
        sb.append("Title: ").append(valueOrEmpty(form.getTitle())).append("\n");
        sb.append("Industry: ").append(valueOrEmpty(form.getIndustry())).append("\n");
        sb.append("Product Lines: ").append(valueOrEmpty(form.getProductLines())).append("\n");
        sb.append("Quoting Process: ").append(valueOrEmpty(form.getQuotingProcess())).append("\n");
        sb.append("\n");
        sb.append("Message:\n");
        sb.append(valueOrEmpty(form.getMessage())).append("\n");
        return sb.toString();
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
