package com.shohag.assessment.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargeResponseModel {
    private int statusCode;
    private String message;
    private String transactionId;
    private String operator;
    private String shortCode;
    private String msisdn;
    private String chargeCode;
}
