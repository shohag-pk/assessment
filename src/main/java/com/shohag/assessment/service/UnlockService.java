package com.shohag.assessment.service;

import com.shohag.assessment.domain.Inbox;
import com.shohag.assessment.model.UnlockCodeRequest;

public interface UnlockService {

    boolean getUnlockCode(Inbox inbox);
}
