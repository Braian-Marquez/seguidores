package com.seguidores.mvc.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.datastructures.preference.*;
import com.seguidores.mvc.exceptions.NotFoundException;
import com.seguidores.mvc.models.entity.OrderPayment;
import com.seguidores.mvc.models.entity.StatusPayment;
import com.seguidores.mvc.models.repository.OrderPaymentRepository;
import com.seguidores.mvc.models.request.NewPreferenceRequest;
import com.seguidores.mvc.models.request.PayerInfo;
import com.seguidores.mvc.models.request.PreferenceItem;
import com.seguidores.mvc.models.response.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import com.mercadopago.resources.Preference;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PreferenceServiceMP {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final OrderPaymentRepository orderPaymentRepository;
    private final ServiceImpl service;

    @Value("${mp.URL_REDIRECT}")
    private String URL_REDIRECT;

    @Value("${mp.WEBHOOK_TOKEN}")
    private String WEBHOOK_TOKEN;
    private static final String MP_ACCESS_TOKEN="TEST-7016753486857480-072713-710a367a06447a8489b6d34094bafb4d-433977872";

    public ResponseEntity<?> createPaymentMP(NewPreferenceRequest preferenceDTO) throws MPException {

        List<ServiceResponse> serviceList = service.getAll();
        Integer idServiceToFind = preferenceDTO.getItems().getIdService();
        Integer quantity = preferenceDTO.getItems().getQuantity();

        ServiceResponse foundServiceResponse = null;

        for (ServiceResponse serviceResponse : serviceList) {
            if (serviceResponse.getService().equals(idServiceToFind)) {
                foundServiceResponse = serviceResponse;
                break;
            }
        }

        if (foundServiceResponse == null) {
            throw new NotFoundException("The service with id: " + idServiceToFind + " not exist.");
        }

        int maxLikes = Integer.parseInt(foundServiceResponse.getMax());
        double costPorMaxLikes = Double.parseDouble(foundServiceResponse.getRate());
        if (quantity > maxLikes) {
            throw new NotFoundException("The requested amount exceeds the maximum allowed, please enter a smaller number.");
        }
        double costPorLike = (costPorMaxLikes / maxLikes);
        float totalPrice = (float) (quantity * costPorLike);
        boolean balance = service.findBalance(totalPrice);
        /*
        if (!balance) {
            throw new NotFoundException("The request cannot be processed at this time, please try again later.");
        }*/

        if (StringUtils.isEmpty(MP_ACCESS_TOKEN)) {
            return ResponseEntity.badRequest().body("Access token is mandatory");
        }
        if (preferenceDTO.getItems() == null) {
            return ResponseEntity.badRequest().body("Items empty");
        }

        MercadoPago.SDK.setAccessToken(MP_ACCESS_TOKEN);
        String notificationUrl = URL_REDIRECT;

        Preference p = new Preference();
        p.setBackUrls(
                new BackUrls().setSuccess(notificationUrl)
                        .setPending("https://smm-mocha.vercel.app/")
                        .setFailure("https://smm-mocha.vercel.app/")

        );

        PayerInfo payerInfo = preferenceDTO.getPayerInfo();
        Payer payer = new Payer();
        payer.setEmail(payerInfo.getEmail());


        p.setPayer(payer);

        String randomKey = UUID.randomUUID().toString();

        List<OrderPayment> orderPayments = orderPaymentRepository.findByExist(preferenceDTO.getItems().getIdService(), preferenceDTO.getPayerInfo().getEmail(), preferenceDTO.getPayerInfo().getLink());
        OrderPayment orderPayment = null;
        if (!orderPayments.isEmpty()) {
            orderPayment = orderPayments.get(orderPayments.size() - 1);
            if (!orderPayment.getStatusPayment().equals(StatusPayment.EN_PROCESO)) {
                orderPayment = new OrderPayment();
            }
        } else {
            orderPayment = new OrderPayment();
        }

        orderPayment.setIdPayment(randomKey);
        orderPayment.setLink(preferenceDTO.getPayerInfo().getLink());
        orderPayment.setQuantity(preferenceDTO.getItems().getQuantity());
        orderPayment.setPrice(totalPrice);
        orderPayment.setEmail(payerInfo.getEmail());
        orderPayment.setStatusOrder("NO_STATUS");
        orderPayment.setIdService(preferenceDTO.getItems().getIdService());
        orderPayment.setStatusPayment(StatusPayment.EN_PROCESO);
        orderPaymentRepository.save(orderPayment);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("service-id", preferenceDTO.getItems().getIdService());
        jsonObject.addProperty("order-id", orderPayment.getId());
        jsonObject.addProperty("email", preferenceDTO.getPayerInfo().getEmail());
        jsonObject.addProperty("token", WEBHOOK_TOKEN);
        p.setMetadata(jsonObject);

        final ServiceResponse finalFoundServiceResponse = foundServiceResponse;

        List<PreferenceItem> preferenceItemList = new ArrayList<>();
        preferenceItemList.add(preferenceDTO.getItems());
        OrderPayment finalOrderPayment = orderPayment;
        p.setItems(preferenceItemList.stream()
                .map(i -> {
                    Item item = new Item();
                    item.setId(finalOrderPayment.getIdPayment());
                    BigDecimal unitPrice = BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
                    item.setUnitPrice(unitPrice.floatValue());
                    item.setTitle(finalFoundServiceResponse.getCategory());
                    item.setDescription(finalFoundServiceResponse.getName());
                    item.setCategoryId(finalFoundServiceResponse.getType());
                    item.setCurrencyId("USD");
                    item.setQuantity(1);
                    return item;
                })
                .collect(Collectors.toCollection(ArrayList::new)));

        try {

            p.save();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }

        if (StringUtils.isEmpty(p.getId())) {
            return ResponseEntity.status(404).body(
                    Collections.singletonMap("Message",
                            "Preference was not created. Check if Access Token is valid")
            );
        }


        return ResponseEntity.ok(gson.toJson(p));
    }

    public String createPaymentBinance(NewPreferenceRequest preferenceDTO) {
        return "This service has in production.";
    }
}