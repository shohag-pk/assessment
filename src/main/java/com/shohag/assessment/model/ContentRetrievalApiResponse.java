package com.shohag.assessment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentRetrievalApiResponse {
    private int statusCode;
    private String message;
    private int contentCount;
    private List<ContentModel> contents;
}
