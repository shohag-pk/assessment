package com.shohag.assessment.async;

import com.shohag.assessment.domain.Inbox;
import com.shohag.assessment.model.UnlockCodeRequest;
import com.shohag.assessment.service.UnlockService;
import com.shohag.assessment.service.implementation.UnlockServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class SmsProcessService {

    private final UnlockService unlockService;

    public SmsProcessService(UnlockServiceImpl unlockService) {
        this.unlockService = unlockService;
    }


    @Async
    public void smsProcessWithTaskExecutor(Inbox inbox){
        unlockService.getUnlockCode(inbox);
    }
}
