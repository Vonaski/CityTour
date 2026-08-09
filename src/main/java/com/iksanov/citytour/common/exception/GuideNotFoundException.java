package com.iksanov.citytour.common.exception;

public class GuideNotFoundException extends RuntimeException {

    public GuideNotFoundException(Long id) {
        super("Guide with id " + id + " not found");
    }
}