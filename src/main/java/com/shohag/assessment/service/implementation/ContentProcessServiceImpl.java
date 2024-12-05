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

    // Base URL for the RestClient used to retrieve content. Get from the application.properties file
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


        // Retrieve content from the content provider service API.
        ContentRetrievalApiResponse response = restClient
                .get()
                .uri(baseUrl +"/a55dbz923ace647v/api/v1.0/services/content")
                .retrieve()
                .onStatus(status -> status.value() != 200,  // Handle non-200 HTTP statuses.
                        ((request, response1) -> {
                            response1.getStatusCode();
                }))
                .body(ContentRetrievalApiResponse.class); // Parse response into the expected model.


        // Handle error responses with specific HTTP status codes.
        if (response != null) {
            if (response.getStatusCode() == 500 || response.getStatusCode() == 503 || response.getStatusCode() == 504) {
                throw new CustomException(new ApiResponse(response.getStatusCode(), response.getMessage()));
            }
        }

        // Process valid content if response status is 200.
        if (response != null && response.getStatusCode() == 200 && response.getContents() != null) {
            // Map API content to Inbox entities and prepare for saving.
            List<Inbox> inboxList = response.getContents().stream()
                    .map(content -> {
                        Inbox inbox = new Inbox();
                        inbox.setTransactionId(content.getTransactionId());
                        inbox.setOperator(content.getOperator());
                        inbox.setShortCode(content.getShortCode());
                        inbox.setMsisdn(content.getMsisdn());
                        inbox.setSms(content.getSms());
                        inbox.setStatus("N"); // Set default status to "N" according to instruction
                        inbox.setCreatedAt(LocalDateTime.now());

                        // Extract keyword and game name from the SMS content.
                        String[] sms = content.getSms().split(" ");
                        if (sms.length >= 2) {
                            inbox.setKeyword(sms[0]);
                            inbox.setGameName(sms[1]);
                        }
                        return inbox;
                    }).collect(Collectors.toList());

            // Save all Inbox entities to the database.
            inboxRepository.saveAll(inboxList);
        }


        // Fetch all inbox entries for further processing.
        List<Inbox> inboxList = inboxRepository.findAll();

        log.info("Inbox list size is {} ", inboxList.size());

        // Process each inbox entry.
        inboxList.forEach(inbox -> {
            // Retrieve keyword details based on the keyword.
            Optional<KeywordDetails> optionalKeywordDetails = keywordDetailsRepository.findById(inbox.getKeyword() != null? inbox.getKeyword() : "None");

            // If keyword details exist, trigger SMS processing.
            if (optionalKeywordDetails.isPresent()) {
                smsProcessService.smsProcessWithTaskExecutor(inbox);
               // unlockService.getUnlockCode(inbox);
            }
        });

        // test case

        return true;
    }


}
