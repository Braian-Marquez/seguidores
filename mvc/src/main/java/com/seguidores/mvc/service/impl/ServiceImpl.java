package com.seguidores.mvc.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguidores.mvc.exceptions.NotFoundException;
import com.seguidores.mvc.models.response.ServiceResponse;
import com.seguidores.mvc.service.interfaces.ServiceAPI;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
public class ServiceImpl implements ServiceAPI {

    private final static String URL = "https://justanotherpanel.com/api/v2/";
    private final static String KEY = "c59bbeff5280075d08844613c639d69a";
    private final WebClient client;

    public ServiceImpl() {
        this.client = initializeWebClient();
    }
    private WebClient initializeWebClient() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        return webClientBuilder.baseUrl(URL).build();
    }

    @Override
    public List<ServiceResponse> getAll() {
        return client.post()
                .uri(uriBuilder -> uriBuilder.path("/")
                        .queryParam("key", KEY)
                        .queryParam("action", "services").build())
                .retrieve().bodyToFlux(ServiceResponse.class).collectList().block();
    }

    public boolean findBalance(Float totalPrice) {
        ResponseEntity<JsonNode> response = client.post()
                .uri(uriBuilder -> uriBuilder.path("/")
                        .queryParam("key", KEY)
                        .queryParam("action", "balance").build())
                .retrieve().toEntity(JsonNode.class)
                .block();
        if (response != null && response.getBody() != null) {
            JsonNode balanceNode = response.getBody().get("balance");
            if (balanceNode != null) {
                String balanceStr = balanceNode.asText();
                try {
                    Float balance = Float.parseFloat(balanceStr.replace(",", "."));
                    return totalPrice <= balance;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return false;
    }

    public int add(int serviceId, String link, int quantity) {
        ResponseEntity<JsonNode> response = client.post()
                .uri(uriBuilder -> uriBuilder.path("/")
                        .queryParam("key", KEY)
                        .queryParam("action", "add")
                        .queryParam("service", serviceId)
                        .queryParam("link", link)
                        .queryParam("quantity", quantity).build())
                .retrieve().toEntity(JsonNode.class)
                .block();
        if (response != null && response.getBody() != null) {
            JsonNode orderNode = response.getBody().get("order");
            if (orderNode != null) {
                return orderNode.asInt();
            }
        }
        throw new NotFoundException("There has been a problem with the payment.");
    }


    public String status(int orderId) {
        ResponseEntity<String> response = client.post()
                .uri(uriBuilder -> uriBuilder.path("/")
                        .queryParam("key", KEY)
                        .queryParam("action", "status")
                        .queryParam("order", orderId).build())
                .retrieve()
                .toEntity(String.class)
                .block();

        if (response != null && response.getBody() != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode balanceNode = jsonNode.get("status");
                if (balanceNode != null) {
                    return balanceNode.asText();
                }else {
                    return "NO_STATUS";
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing JSON response", e);
            }
        }
        throw new NotFoundException("Problem searching for status.");
    }
}
