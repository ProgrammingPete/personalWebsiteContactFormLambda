package com.personalwebsite.contact;

import java.util.ArrayList;
import java.util.List;

public class InputValidator {

    public List<String> validate(FormData form) {
        List<String> invalidFields = new ArrayList<>();

        if (isBlank(form.getFirstName())) {
            invalidFields.add("firstName");
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
        form.setFirstName(sanitize(form.getFirstName()));
        form.setLastName(sanitize(form.getLastName()));
        form.setEmail(sanitize(form.getEmail()));
        form.setPhone(sanitize(form.getPhone()));
        form.setCompany(sanitize(form.getCompany()));
        form.setTitle(sanitize(form.getTitle()));
        form.setIndustry(sanitize(form.getIndustry()));
        form.setProductLines(sanitize(form.getProductLines()));
        form.setQuotingProcess(sanitize(form.getQuotingProcess()));
        form.setMessage(sanitize(form.getMessage()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
