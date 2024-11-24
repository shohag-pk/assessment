package com.shohag.assessment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="charge_success_log")
public class ChargeSuccessLog {

    @Id
    @SequenceGenerator(name="CHARGE_SUC_ID", sequenceName="CHARGE_SUC_ID", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="CHARGE_SUC_ID")
    private Long id;
    private Long smsId;
    private String transactionId;
    private String operator;
    private String shortCode;
    private String msisdn;
    private String keyword;
    private String gameName;
}
