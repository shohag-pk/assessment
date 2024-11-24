package com.shohag.assessment.repository;

import com.shohag.assessment.domain.ChargeFailureLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeFailureLogRepository extends JpaRepository<ChargeFailureLog, Long> {
}
