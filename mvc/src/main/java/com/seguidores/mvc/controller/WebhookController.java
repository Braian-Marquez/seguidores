package com.seguidores.mvc.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguidores.mvc.exceptions.NotFoundException;
import com.seguidores.mvc.models.entity.OrderPayment;
import com.seguidores.mvc.models.request.WebhookNotification;
import com.seguidores.mvc.service.impl.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.Objects;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
public class WebhookController {

    @Value("${mp.MP_ACCESS_TOKEN}")
    private String MP_ACCESS_TOKEN;

    private final OrderService service;

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhookNotification(@RequestBody WebhookNotification webhookNotification) {
        if (webhookNotification.getData() == null) {
            return ResponseEntity.badRequest().body("Data is missing in the webhook notification");
        }

        String paymentId = webhookNotification.getData().getId();
        String idPay = null;
        long orderId = 0;
        int unitPrice=0;
        String paymentStatus = getPaymentStatusFromMercadoPago(paymentId);
        String token;



        if ("approved".equalsIgnoreCase(paymentStatus)) {
            try {

                String accessToken = MP_ACCESS_TOKEN;
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                String apiUrl = "https://api.mercadopago.com/v1/payments/" + paymentId;
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);


                String jsonResponse = response.getBody();
                System.out.printf(Objects.requireNonNull(response.getBody()));
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(jsonResponse);


                JsonNode metadataNode = jsonNode.get("metadata");
                JsonNode itemsNode = jsonNode.path("additional_info").path("items");

                for (JsonNode itemNode : itemsNode) {
                    idPay = itemNode.path("id").asText();
                    int quantity = itemNode.path("quantity").asInt();
                    String title = itemNode.path("title").asText();
                    unitPrice = itemNode.path("unit_price").asInt();

                    System.out.println("Item ID: " + idPay);
                    System.out.println("Item Quantity: " + quantity);
                    System.out.println("Item Title: " + title);
                    System.out.println("Item Unit Price: " + unitPrice);
                }


                String dateApproved = jsonNode.get("date_approved").asText();
                String dateCreated = jsonNode.get("date_created").asText();
                System.out.println("Cash: " + dateCreated + " " + dateApproved);

                JsonNode transactionDetails = jsonNode.get("transaction_details");
                String installmentAmount = transactionDetails.path("installment_amount").asText();
                String net_received_amount = transactionDetails.path("net_received_amount").asText();
                System.out.println("Transaction Details : " + installmentAmount + " " + net_received_amount);

                orderId = metadataNode.get("order-id").asLong();
                token = metadataNode.get("token").asText();

                if (!token.equals(System.getenv("WEBHOOK_TOKEN"))) {
                    return ResponseEntity.badRequest().body("Invalid payment user not authorized.");
                }

            } catch (Exception e) {
                throw new NotFoundException(e.getMessage());
            }
            OrderPayment isUpdated = service.updateEventPagoStatus(orderId, null);
            return isUpdated!=null ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Event not found or not updated.");

        }
        return ResponseEntity.badRequest().body("Invalid payment status or event not found.");
    }

    private String getPaymentStatusFromMercadoPago(String paymentId) {
        return "approved";
    }
}
