package com.seguidores.mvc.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguidores.mvc.exceptions.NotFoundException;
import com.seguidores.mvc.models.entity.OrderPayment;
import com.seguidores.mvc.models.entity.StatusPayment;
import com.seguidores.mvc.models.repository.OrderPaymentRepository;
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

    private static final String MP_ACCESS_TOKEN="TEST-7016753486857480-072713-710a367a06447a8489b6d34094bafb4d-433977872";
    @Value("${mp.WEBHOOK_TOKEN}")
    private String WEBHOOK_TOKEN;
    private final OrderService service;
    private final OrderPaymentRepository orderPaymentRepository;
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

                orderId = metadataNode.get("order-id").asLong();
                OrderPayment existOrder=orderPaymentRepository.findById(orderId).orElse(null);
                assert existOrder != null;
                if (existOrder.getStatusPayment().equals(StatusPayment.PAGO_REALIZADO)){
                     return ResponseEntity.ok().build();
                 }
                System.out.println(" Order ID: "+orderId);
                token = metadataNode.get("token").asText();

                if (!token.equals(WEBHOOK_TOKEN)) {
                    return ResponseEntity.badRequest().body("Invalid payment user not authorized.");
                }

            } catch (Exception e) {
                throw new NotFoundException(e.getMessage());
            }
            OrderPayment isUpdated = service.updateEventPagoStatus(orderId);
            return isUpdated!=null ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Order not found or not updated.");

        }
        return ResponseEntity.badRequest().body("Invalid payment status or event not found.");
    }

    private String getPaymentStatusFromMercadoPago(String paymentId) {
        return "approved";
    }
}
