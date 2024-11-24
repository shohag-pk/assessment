package com.shohag.assessment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="inbox")
public class Inbox {

    @Id
    @SequenceGenerator(name="INBOX_ID_GEN", sequenceName="INBOX_ID_GEN", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="INBOX_ID_GEN")
    private long id;
    @Column(name="transaction_id")
    private String transactionId;
    @Column(name="operator")
    private String operator;
    @Column(name="short_code")
    private String shortCode;
    @Column(name="msisdn")
    private String msisdn;
    @Column(name="keyword")
    private String keyword;
    @Column(name="game_name")
    private String gameName;
    @Column(name="sms")
    private String sms;
    @Column(name="status")
    private String status;
    @Column(name="created_at")
    private LocalDateTime createdAt;
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
