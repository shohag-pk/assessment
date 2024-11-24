package com.shohag.assessment.service.implementation;

import com.shohag.assessment.async.SmsProcessService;
import com.shohag.assessment.domain.Inbox;
import com.shohag.assessment.domain.KeywordDetails;
import com.shohag.assessment.model.ApiResponse;
import com.shohag.assessment.model.CustomException;
import com.shohag.assessment.model.ContentRetrievalApiResponse;
import com.shohag.assessment.repository.InboxRepository;
import com.shohag.assessment.repository.KeywordDetailsRepository;
import com.shohag.assessment.service.ContentProcessService;
import com.shohag.assessment.service.UnlockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContentProcessServiceImpl implements ContentProcessService {
    @Value("${restclient.base-url}")
    private String baseUrl;

    private final RestClient restClient;
    private final InboxRepository inboxRepository;
    private final SmsProcessService smsProcessService;
    private final KeywordDetailsRepository keywordDetailsRepository;
    private final UnlockService unlockService;

    public ContentProcessServiceImpl(RestClient.Builder restClient,
                                     InboxRepository inboxRepository,
                                     SmsProcessService smsProcessService,
                                     KeywordDetailsRepository keywordDetailsRepository,
                                     UnlockServiceImpl unlockService) {
        this.restClient = restClient.baseUrl(baseUrl).build();
        this.inboxRepository = inboxRepository;
        this.smsProcessService = smsProcessService;
        this.keywordDetailsRepository = keywordDetailsRepository;
        this.unlockService = unlockService;
    }


    @Override
    public boolean doContentProcess() throws CustomException {

        ContentRetrievalApiResponse response = restClient
                .get()
                .uri(baseUrl +"/a55dbz923ace647v/api/v1.0/services/content")
                .retrieve()
                .onStatus(status -> status.value() != 200,
                        ((request, response1) -> {
                            response1.getStatusCode();
                }))
                .body(ContentRetrievalApiResponse.class);


        if (response!= null && response.getStatusCode() == 500) {
            throw new CustomException(new ApiResponse(response.getStatusCode(),response.getMessage()));
        } else if (response!= null && response.getStatusCode() == 503) {
            throw new CustomException(new ApiResponse(response.getStatusCode(),response.getMessage()));
        } else if (response!= null && response.getStatusCode() == 504) {
            throw new CustomException(new ApiResponse(response.getStatusCode(),response.getMessage()));
        }

        if (response != null && response.getStatusCode() == 200 && response.getContents() != null) {
            List<Inbox> inboxList = response.getContents().stream()
                    .map(content -> {
                        Inbox inbox = new Inbox();
                        inbox.setTransactionId(content.getTransactionId());
                        inbox.setOperator(content.getOperator());
                        inbox.setShortCode(content.getShortCode());
                        inbox.setMsisdn(content.getMsisdn());
                        inbox.setSms(content.getSms());
                        inbox.setStatus("N");
                        inbox.setCreatedAt(LocalDateTime.now());

                        String[] sms = content.getSms().split(" ");
                        if (sms.length >= 2) {
                            inbox.setKeyword(sms[0]);
                            inbox.setGameName(sms[1]);
                        }
                        return inbox;
                    }).collect(Collectors.toList());

            inboxRepository.saveAll(inboxList);
        }


        List<Inbox> inboxList = inboxRepository.findAll();

        log.info("Inbox list size is {} ", inboxList.size());

        inboxList.forEach(inbox -> {
            Optional<KeywordDetails> optionalKeywordDetails = keywordDetailsRepository.findById(inbox.getKeyword() != null? inbox.getKeyword() : "None");
            if (optionalKeywordDetails.isPresent()) {
                smsProcessService.smsProcessWithTaskExecutor(inbox);
               // unlockService.getUnlockCode(inbox);
            }
        });

        return true;
    }


}
