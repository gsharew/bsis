package org.jembi.bsis.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpService {
    private final RestTemplate restTemplate;
    @Autowired
    public HttpService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendDonationInformationToPolyTechSpring(String donation) {
        String url = "http://localhost:8081/api/create-bsis";

        // Set the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(donation, headers);

        // Send the POST request
        String responseBody = restTemplate.postForObject(url, requestEntity, String.class);

        System.out.println("Response body is: " + responseBody);
    }
}
