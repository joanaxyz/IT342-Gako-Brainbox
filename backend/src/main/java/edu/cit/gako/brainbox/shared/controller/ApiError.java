package edu.cit.gako.brainbox.shared.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiError {
    private String code;
    private String message;
    private Object details;
}
