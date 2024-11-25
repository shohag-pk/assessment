package com.shohag.assessment.async;

import com.shohag.assessment.domain.Inbox;
import com.shohag.assessment.service.UnlockService;
import com.shohag.assessment.service.implementation.UnlockServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


// This class provides asynchronous SMS processing functionality.
@Component
@Slf4j
public class SmsProcessService {

    private final UnlockService unlockService;

    public SmsProcessService(UnlockServiceImpl unlockService) {
        this.unlockService = unlockService;
    }


    // Asynchronously processes an Inbox entity to retrieve an unlock code.
    // This method allows the task to be executed in a separate thread without blocking the main execution flow.
    @Async
    public void smsProcessWithTaskExecutor(Inbox inbox){
        log.info("Processing SMS for Inbox with Transaction ID: {}", inbox.getTransactionId());

        // Call the UnlockService to retrieve the unlock code and do charge for the provided Inbox entity.
        unlockService.getUnlockCode(inbox);

        log.info("Completed processing SMS for Inbox with Transaction ID: {}", inbox.getTransactionId());
    }
}
