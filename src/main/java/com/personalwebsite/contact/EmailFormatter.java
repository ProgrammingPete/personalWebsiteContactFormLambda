package com.personalwebsite.contact;

public class EmailFormatter {

    public String buildSubject(FormData form) {
        return "peterparianos.comContact Form Submission from " + form.getCompany();
    }

    public String buildBody(FormData form) {
        StringBuilder sb = new StringBuilder();
        sb.append("New peterparianos.com Contact Form Submission\n");
        sb.append("===========================\n");
        sb.append("\n");
        sb.append("Name: ").append(valueOrEmpty(form.getName())).append("\n");
        sb.append("Email: ").append(valueOrEmpty(form.getEmail())).append("\n");
        sb.append("Company: ").append(valueOrEmpty(form.getCompany())).append("\n");
        sb.append("Project Type: ").append(valueOrEmpty(form.getProjectType())).append("\n");
        sb.append("Budget: ").append(valueOrEmpty(form.getBudget())).append("\n");
        sb.append("Message: ").append(valueOrEmpty(form.getMessage())).append("\n");
        sb.append("\n");
        sb.append("Message:\n");
        sb.append(valueOrEmpty(form.getMessage())).append("\n");
        return sb.toString();
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
