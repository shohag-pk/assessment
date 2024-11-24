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


    @Override
    public boolean getUnlockCode(Inbox inbox) {
        UnlockCodeRequest unlockCodeRequest = toUnlockCodeRequest(inbox);
        UnlockCodeResponse unlockCodeResponse = restClient
                .post()
                .uri(baseUrl + "/a55dbz923ace647v/api/v1.0/services/unlockCode")
                .body(unlockCodeRequest)
                .retrieve()
                .onStatus(status -> status.value() != 200,
                        ((request, response) -> {
                            response.getStatusCode();
                        }))
                .body(UnlockCodeResponse.class);

        if (unlockCodeResponse != null && unlockCodeResponse.getStatusCode() == 200) {
            log.info("unlockCodeResponse is  : "+unlockCodeResponse.getStatusCode());
            Optional<ChargeConfig> optionalChargeConfig = chargeConfigRepository.findById(unlockCodeResponse.getOperator());

            if (optionalChargeConfig.isPresent()) {
                ChargeRequestModel chargeRequestModel = mapToChargeRequest(unlockCodeResponse, optionalChargeConfig.get().getChargeCode());


                ChargeResponseModel chargeResponseModel = restClient
                        .post()
                        .uri(baseUrl + "/a55dbz923ace647v/api/v1.0/services/charge")
                        .body(chargeRequestModel)
                        .retrieve()
                        .onStatus(status -> status.value() != 200,
                                ((request, response) -> {
                                    response.getStatusCode();
                                }))
                        .body(ChargeResponseModel.class);

                if (chargeResponseModel != null && chargeResponseModel.getStatusCode() == 200) {

                    log.info("chargeResponseModel is  : "+chargeResponseModel.getStatusCode());

                    ChargeSuccessLog successLog = new ChargeSuccessLog();
                    successLog.setSmsId(inbox.getId());
                    successLog.setTransactionId(chargeResponseModel.getTransactionId());
                    successLog.setMsisdn(chargeResponseModel.getMsisdn());
                    successLog.setKeyword(inbox.getKeyword());
                    successLog.setShortCode(chargeResponseModel.getShortCode());
                    successLog.setGameName(inbox.getGameName());
                    successLog.setOperator(inbox.getOperator());

                    chargeSuccessLogRepository.save(successLog);

                    inbox.setStatus("S");
                    inbox.setUpdatedAt(LocalDateTime.now());
                    inboxRepository.save(inbox);

                } else {

                    ChargeFailureLog failureLog = new ChargeFailureLog();
                    failureLog.setSmsId(inbox.getId());
                    failureLog.setTransactionId(inbox.getTransactionId());
                    failureLog.setMsisdn(inbox.getMsisdn());
                    failureLog.setKeyword(inbox.getKeyword());
                    failureLog.setShortCode(inbox.getShortCode());
                    failureLog.setGameName(inbox.getGameName());
                    failureLog.setOperator(inbox.getOperator());

                    chargeFailureLogRepository.save(failureLog);

                    inbox.setStatus("F");
                    inbox.setUpdatedAt(LocalDateTime.now());
                    inboxRepository.save(inbox);
                }
            }
        }


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
