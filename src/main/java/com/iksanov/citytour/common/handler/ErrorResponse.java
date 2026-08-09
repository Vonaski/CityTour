package com.iksanov.citytour.common.handler;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
    private List<FieldError> errors;

    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String message;
    }
}