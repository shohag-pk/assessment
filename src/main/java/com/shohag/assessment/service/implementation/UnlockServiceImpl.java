package com.shohag.assessment.service.implementation;

import com.shohag.assessment.domain.ChargeConfig;
import com.shohag.assessment.domain.ChargeFailureLog;
import com.shohag.assessment.domain.ChargeSuccessLog;
import com.shohag.assessment.domain.Inbox;
import com.shohag.assessment.model.ChargeRequestModel;
import com.shohag.assessment.model.ChargeResponseModel;
import com.shohag.assessment.model.UnlockCodeRequest;
import com.shohag.assessment.model.UnlockCodeResponse;
import com.shohag.assessment.repository.ChargeConfigRepository;
import com.shohag.assessment.repository.ChargeFailureLogRepository;
import com.shohag.assessment.repository.ChargeSuccessLogRepository;
import com.shohag.assessment.repository.InboxRepository;
import com.shohag.assessment.service.UnlockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UnlockServiceImpl implements UnlockService {
    @Value("${restclient.base-url}")
    private String baseUrl;

    private final RestClient restClient;
    private final InboxRepository inboxRepository;
    private final ChargeConfigRepository chargeConfigRepository;
    private final ChargeSuccessLogRepository chargeSuccessLogRepository;
    private final ChargeFailureLogRepository chargeFailureLogRepository;

    public UnlockServiceImpl(RestClient.Builder restClient,
                             InboxRepository inboxRepository,
                             ChargeConfigRepository chargeConfigRepository,
                             ChargeSuccessLogRepository chargeSuccessLogRepository,
                             ChargeFailureLogRepository chargeFailureLogRepository) {
        this.restClient = restClient.baseUrl(baseUrl).build();
        this.inboxRepository = inboxRepository;
        this.chargeConfigRepository = chargeConfigRepository;
        this.chargeSuccessLogRepository = chargeSuccessLogRepository;
        this.chargeFailureLogRepository = chargeFailureLogRepository;
    }


    // Processes an Inbox entry to retrieve an unlock code and perform a charge.
    @Override
    public boolean getUnlockCode(Inbox inbox) {
        // Map the Inbox entity to an UnlockCodeRequest model.
        UnlockCodeRequest unlockCodeRequest = toUnlockCodeRequest(inbox);

        // Call the external unlock code API.
        UnlockCodeResponse unlockCodeResponse = restClient
                .post()
                .uri(baseUrl + "/a55dbz923ace647v/api/v1.0/services/unlockCode")
                .body(unlockCodeRequest)
                .retrieve()
                .onStatus(status -> status.value() != 200,   // Handle non-200 HTTP responses.
                        ((request, response) -> {
                            log.warn("Unlock code request failed with status: {}", response.getStatusCode());
                            response.getStatusCode();
                        }))
                .body(UnlockCodeResponse.class);

        if (unlockCodeResponse != null && unlockCodeResponse.getStatusCode() == 200) {
            log.info("Unlock code API responded successfully for SMS ID: {}", inbox.getId());

            // Retrieve charge configuration for the operator.
            Optional<ChargeConfig> optionalChargeConfig = chargeConfigRepository.findById(unlockCodeResponse.getOperator());

            if (optionalChargeConfig.isPresent()) {

                // Map the response to a ChargeRequestModel.
                ChargeRequestModel chargeRequestModel = mapToChargeRequest(unlockCodeResponse, optionalChargeConfig.get().getChargeCode());


                // Call the external charge API.
                log.info("Sending charge request for SMS ID: {}", inbox.getId());
                ChargeResponseModel chargeResponseModel = restClient
                        .post()
                        .uri(baseUrl + "/a55dbz923ace647v/api/v1.0/services/charge")
                        .body(chargeRequestModel)
                        .retrieve()
                        .onStatus(status -> status.value() != 200,
                                ((request, response) -> {
                                    log.warn("Charge API returned non-200 status for SMS ID: {}. Status: {}", inbox.getId(), response.getStatusCode());
                                    response.getStatusCode();
                                }))
                        .body(ChargeResponseModel.class);

                if (chargeResponseModel != null && chargeResponseModel.getStatusCode() == 200) {

                    log.info("Charge API successful for SMS ID: {}. Response status: {}", inbox.getId(), chargeResponseModel.getStatusCode());

                    // Log success and update records for successful charges.
                    ChargeSuccessLog successLog = new ChargeSuccessLog();
                    successLog.setSmsId(inbox.getId());
                    successLog.setTransactionId(chargeResponseModel.getTransactionId());
                    successLog.setMsisdn(chargeResponseModel.getMsisdn());
                    successLog.setKeyword(inbox.getKeyword());
                    successLog.setShortCode(chargeResponseModel.getShortCode());
                    successLog.setGameName(inbox.getGameName());
                    successLog.setOperator(inbox.getOperator());

                    chargeSuccessLogRepository.save(successLog);
                    log.debug("Charge success log saved for SMS ID: {}", inbox.getId());

                    inbox.setStatus("S");
                    inbox.setUpdatedAt(LocalDateTime.now());
                    inboxRepository.save(inbox);

                } else {

                    log.warn("Charge API failed for SMS ID: {}", inbox.getId());

                    // Log failure and update records for failed charges.
                    ChargeFailureLog failureLog = new ChargeFailureLog();
                    failureLog.setSmsId(inbox.getId());
                    failureLog.setTransactionId(inbox.getTransactionId());
                    failureLog.setMsisdn(inbox.getMsisdn());
                    failureLog.setKeyword(inbox.getKeyword());
                    failureLog.setShortCode(inbox.getShortCode());
                    failureLog.setGameName(inbox.getGameName());
                    failureLog.setOperator(inbox.getOperator());

                    chargeFailureLogRepository.save(failureLog);
                    log.debug("Charge failure log saved for SMS ID: {}", inbox.getId());

                    inbox.setStatus("F");
                    inbox.setUpdatedAt(LocalDateTime.now());
                    inboxRepository.save(inbox);
                }
            }
        }


        log.info("Completed unlock code processing for SMS ID: {}", inbox.getId());
        return true;
    }


    public static ChargeRequestModel mapToChargeRequest(UnlockCodeResponse unlockCodeResponse, String chargeCode) {
        ChargeRequestModel chargeRequestModel = new ChargeRequestModel();
        chargeRequestModel.setTransactionId(unlockCodeResponse.getTransactionId());
        chargeRequestModel.setOperator(unlockCodeResponse.getOperator());
        chargeRequestModel.setShortCode(unlockCodeResponse.getShortCode());
        chargeRequestModel.setMsisdn(unlockCodeResponse.getMsisdn());
        chargeRequestModel.setChargeCode(chargeCode);
        return chargeRequestModel;
    }

    public static UnlockCodeRequest toUnlockCodeRequest(Inbox inbox) {
        UnlockCodeRequest request = new UnlockCodeRequest();
        request.setTransactionId(inbox.getTransactionId());
        request.setOperator(inbox.getOperator());
        request.setShortCode(inbox.getShortCode());
        request.setMsisdn(inbox.getMsisdn());
        request.setKeyword(inbox.getKeyword());
        request.setGameName(inbox.getGameName());
        return request;
    }
}
