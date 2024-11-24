package com.shohag.assessment.repository;

import com.shohag.assessment.domain.Inbox;
import com.shohag.assessment.model.UnlockCodeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InboxRepository extends JpaRepository<Inbox, Long> {

}
