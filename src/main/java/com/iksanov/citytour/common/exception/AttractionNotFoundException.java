package com.iksanov.citytour.common.exception;

public class AttractionNotFoundException extends RuntimeException {

    public AttractionNotFoundException(Long id) {
        super("Attraction with id " + id + " not found");
    }
}