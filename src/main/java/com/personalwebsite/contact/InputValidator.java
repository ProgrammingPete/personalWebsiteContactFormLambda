package com.personalwebsite.contact;

import java.util.ArrayList;
import java.util.List;

public class InputValidator {

    public List<String> validate(FormData form) {
        List<String> invalidFields = new ArrayList<>();

        if (isBlank(form.getName())) {
            invalidFields.add("name");
        }
        if (isBlank(form.getEmail())) {
            invalidFields.add("email");
        }
        if (isBlank(form.getCompany())) {
            invalidFields.add("company");
        }

        return invalidFields;
    }

    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\r", "").replace("\n", "");
    }

    public void sanitizeAll(FormData form) {
        form.setName(sanitize(form.getName()));
        form.setEmail(sanitize(form.getEmail()));
        form.setBudget(sanitize(form.getBudget()));
        form.setProjectType(sanitize(form.getProjectType()));
        form.setCompany(sanitize(form.getCompany()));
        form.setMessage(sanitize(form.getMessage()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
