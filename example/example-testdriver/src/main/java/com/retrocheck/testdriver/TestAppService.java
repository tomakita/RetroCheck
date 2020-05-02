package com.retrocheck.testdriver;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TestAppService {
    private final RestTemplate restTemplate;

    public TestAppService() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    public String get(Integer requestId) {
        return restTemplate.getForObject(
                "http://localhost:8080/userstatus?userId=" + requestId, String.class);
    }
}
