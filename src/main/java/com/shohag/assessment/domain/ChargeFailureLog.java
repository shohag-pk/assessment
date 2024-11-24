package com.shohag.assessment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="charge_failure_log")
public class ChargeFailureLog {

    @Id
    @SequenceGenerator(name="CHARGE_FAIL_ID", sequenceName="CHARGE_FAIL_ID", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="CHARGE_FAIL_ID")
    private Long id;
    private Long smsId;
    private String transactionId;
    private String operator;
    private String shortCode;
    private String msisdn;
    private String keyword;
    private String gameName;
}
