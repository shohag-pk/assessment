package com.shohag.assessment.controller;

import com.shohag.assessment.model.CustomException;
import com.shohag.assessment.service.ContentProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/content")
public class ContentProcessController {
    private final ContentProcessService contentProcessService;

    public ContentProcessController(ContentProcessService contentProcessService) {
        this.contentProcessService = contentProcessService;
    }

    @GetMapping("/process")
    public ResponseEntity<Boolean> doProcess() throws CustomException {
        return ResponseEntity.ok(contentProcessService.doContentProcess());
    }
}
