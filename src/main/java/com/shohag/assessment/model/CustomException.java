package com.shohag.assessment.model;

import lombok.Data;

@Data
public class CustomException extends Throwable {
    private final ApiResponse apiResponse;

    public CustomException(ApiResponse apiResponse) {
        super(apiResponse.getMessage());
        this.apiResponse = apiResponse;
    }

    public ApiResponse getApiResponse() {
        return apiResponse;
    }
}
