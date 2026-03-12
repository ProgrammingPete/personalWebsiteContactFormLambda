package com.personalwebsite.contact;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FormData {
    private String name;
    private String email;
    private String company;
    private String projectType;
    private String budget;
    private String message;

}
