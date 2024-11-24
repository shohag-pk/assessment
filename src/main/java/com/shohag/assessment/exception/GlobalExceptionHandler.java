package com.shohag.assessment.exception;

import com.shohag.assessment.model.ApiResponse;
import com.shohag.assessment.model.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(CustomException ex) {
        ApiResponse response = ex.getApiResponse();
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCode()));
    }

}

