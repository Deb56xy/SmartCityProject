package com.smartcity.complaint_management_system.service;

import com.smartcity.complaint_management_system.dto.MlPredictionRequest;
import com.smartcity.complaint_management_system.dto.MlPredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
public class MlPredictionService {

    @Value("${ML_VALIDATION_API_URL}")
    private String mlValidationApiUrl;

    @Value("${ML_PRIORITY_API_URL}")
    private String mlPriorityApiUrl;

    @Value("${ML_CATEGORY_API_URL}")
    private String mlCategoryApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public MlPredictionResponse predictComplaintValidity(String description) {
        MlPredictionRequest request = new MlPredictionRequest(description);
        MlPredictionResponse response = restTemplate.postForObject(
                mlValidationApiUrl,
                request,
                MlPredictionResponse.class
        );
        return response;
    }

    public MlPredictionResponse predictComplaintPriority(String description) {
        MlPredictionRequest request = new MlPredictionRequest(description);
        MlPredictionResponse response = restTemplate.postForObject(
                mlPriorityApiUrl,
                request,
                MlPredictionResponse.class
        );
        return response;
    }

    public MlPredictionResponse predictComplaintCategory(String description) {
        MlPredictionRequest request = new MlPredictionRequest(description);
        MlPredictionResponse response = restTemplate.postForObject(
                mlCategoryApiUrl,
                request,
                MlPredictionResponse.class
        );
        return response;
    }
}
