package com.shohag.assessment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentModel {
    private String transactionId;
    private String operator;
    private String shortCode;
    private String msisdn;
    private String sms;
}
