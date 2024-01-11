package com.seguidores.mvc.exceptions;

public class ErrorDto {
    private ErrorResponse error;
    public ErrorResponse getError() {
        return error;
    }
    public void setError(ErrorResponse error) {
        this.error = error;
    }

}