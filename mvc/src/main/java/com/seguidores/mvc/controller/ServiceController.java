package com.seguidores.mvc.controller;

import com.seguidores.mvc.exceptions.NotFoundException;
import com.seguidores.mvc.models.entity.OrderPayment;
import com.seguidores.mvc.models.response.ServiceResponse;
import com.seguidores.mvc.service.impl.OrderService;
import com.seguidores.mvc.service.interfaces.ServiceAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("service")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceAPI serviceAPI;
    private final OrderService orderService;


    @GetMapping("find-all-services")
    public List<ServiceResponse> getEventAll() {
        return serviceAPI.getAll();
    }

    @GetMapping("calculated-price")
    public ResponseEntity<?> calculatedPrice(@RequestParam("id-service") Integer idService, @RequestParam("quantity") Integer quantity) {
        List<ServiceResponse> serviceList = serviceAPI.getAll();
        ServiceResponse foundServiceResponse = null;
        for (ServiceResponse serviceResponse : serviceList) {
            if (serviceResponse.getService().equals(idService)) {
                foundServiceResponse = serviceResponse;
                break;
            }
        }
        if (foundServiceResponse == null) {
            return null;
        }
        int maxLikes = Integer.parseInt(foundServiceResponse.getMax());
        double costPorMaxLikes = Double.parseDouble(foundServiceResponse.getRate());
        if (quantity > maxLikes) {
            throw new NotFoundException("The requested amount exceeds the maximum allowed, please enter a smaller number.");
        }
        double costPorLike = (costPorMaxLikes / maxLikes);
        double priceTotal = quantity * costPorLike;
        Map<String,Double>response=new HashMap<>();
        response.put("price",priceTotal);
        return ResponseEntity.ok(response);
    }


    @GetMapping("find-status-order")
    public OrderPayment findStatus(
            @RequestParam(name = "id-order", required = false) Integer idOrder,
            @RequestParam(name = "email", required = false) String email) {
        if (idOrder == null && email == null) {
            throw new IllegalArgumentException("Require one param 'idOrder' o 'email'.");
        }
        return orderService.updateEventPagoStatus(idOrder, email);
    }

}
