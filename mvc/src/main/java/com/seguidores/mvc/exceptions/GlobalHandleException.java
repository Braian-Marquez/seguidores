package com.seguidores.mvc.exceptions;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalHandleException {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorDto> handleInvalidCredentialsException(InvalidCredentialsException e) {
        ErrorDto response = new ErrorDto();
        ErrorResponse error = buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        response.setError(error);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(NotFoundException e) {
        ErrorDto response = new ErrorDto();
        ErrorResponse error = buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        response.setError(error);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach((violation) -> {
            errors.add(getAttributeName(violation.getPropertyPath().toString())+" : "+violation.getMessage());
        });

        ErrorDto response = new ErrorDto();
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        errorResponse.setDescription(errors);
        response.setError(errorResponse);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private String getAttributeName(String propertyPath) {
        String[] pathNodes = propertyPath.split("\\.");
        return pathNodes[pathNodes.length - 1];
    }

    private ErrorResponse buildErrorResponse(HttpStatus httpStatus, String message) {
        ErrorResponse error = new ErrorResponse();
        error.setHttpStatusCode(httpStatus.value());
        error.getDescription().add(message);
        error.setTimestamp(LocalDateTime.now());
        return error;
    }

}