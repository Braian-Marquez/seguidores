package com.seguidores.mvc.controller;

import com.mercadopago.exceptions.MPException;
import com.seguidores.mvc.models.request.NewPreferenceRequest;
import com.seguidores.mvc.service.impl.PreferenceServiceMP;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("order")
@RequiredArgsConstructor
public class PaymentController {

    private final PreferenceServiceMP preferenceService;

    @GetMapping("/generic")
    public RedirectView success(
            HttpServletRequest request,
            @RequestParam("collection_id") String collectionId,
            @RequestParam("collection_status") String collectionStatus,
            @RequestParam("external_reference") String externalReference,
            @RequestParam("payment_type") String paymentType,
            @RequestParam("merchant_order_id") String merchantOrderId,
            @RequestParam("preference_id") String preferenceId,
            @RequestParam("site_id") String siteId,
            @RequestParam("processing_mode") String processingMode,
            @RequestParam("merchant_account_id") String merchantAccountId,
            RedirectAttributes attributes)
            throws MPException {
        attributes.addFlashAttribute("genericResponse", true);
        attributes.addFlashAttribute("collection_id", collectionId);
        attributes.addFlashAttribute("collection_status", collectionStatus);
        attributes.addFlashAttribute("external_reference", externalReference);
        attributes.addFlashAttribute("payment_type", paymentType);
        attributes.addFlashAttribute("merchant_order_id", merchantOrderId);
        attributes.addFlashAttribute("preference_id", preferenceId);
        attributes.addFlashAttribute("site_id", siteId);
        attributes.addFlashAttribute("processing_mode", processingMode);
        attributes.addFlashAttribute("merchant_account_id", merchantAccountId);
        return new RedirectView("/");
    }

    @PostMapping(value = "/add-service-mp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPreference(@RequestBody NewPreferenceRequest preferenceDTO) throws MPException {
        return preferenceService.createPaymentMP(preferenceDTO);
    }
    @PostMapping(value = "/add-service-binance", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createPreferenceBinance(@RequestBody NewPreferenceRequest preferenceDTO){
        return preferenceService.createPaymentBinance(preferenceDTO);
    }
}