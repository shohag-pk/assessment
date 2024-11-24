package com.shohag.assessment.service;

import com.shohag.assessment.model.CustomException;

public interface ContentProcessService {
    boolean doContentProcess() throws CustomException;
}
