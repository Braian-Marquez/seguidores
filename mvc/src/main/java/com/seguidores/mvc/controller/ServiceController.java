package com.seguidores.mvc.controller;

import com.seguidores.mvc.exceptions.NotFoundException;
import com.seguidores.mvc.models.entity.OrderPayment;
import com.seguidores.mvc.models.entity.StatusPayment;
import com.seguidores.mvc.models.repository.OrderPaymentRepository;
import com.seguidores.mvc.models.response.ServiceResponse;
import com.seguidores.mvc.service.impl.ServiceImpl;
import com.seguidores.mvc.service.interfaces.ServiceAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("service")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceAPI serviceAPI;
    private final OrderPaymentRepository orderPaymentRepository;
    private final ServiceImpl service;

    @GetMapping("find-all-services")
    public List<ServiceResponse> getServiceAll() {
        return serviceAPI.getAll();
    }

    @GetMapping("/find-all-services-category")
    public List<ServiceResponse> getServiceCategory(@RequestParam("category") String category) {
        String lowercaseCategory = category.toLowerCase();
        List<ServiceResponse> allServices = serviceAPI.getAll();
        return allServices.stream()
                .filter(service -> service.getCategory().toLowerCase().contains(lowercaseCategory))
                .collect(Collectors.toList());
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

    @GetMapping("find-status-order-id")
    public OrderPayment findStatus(
            @RequestParam(name = "id-order") Long idOrder) {
       OrderPayment orderPayment= orderPaymentRepository.findById(idOrder).orElseThrow(()->new NotFoundException("The order not exist."));
       String status= service.status(orderPayment.getOrderId());
       orderPayment.setStatusOrder(status);
       orderPaymentRepository.save(orderPayment);
       return orderPayment;
    }
    @GetMapping("find-status-order-email")
    public List<OrderPayment> findStatusEmail(@RequestParam(name = "email") String email) {
        List<OrderPayment> orderPayments = orderPaymentRepository.findByEmail(email);
        for (OrderPayment order : orderPayments) {
            try {
                String status = service.status(order.getOrderId());
                order.setStatusOrder(status);
                orderPaymentRepository.save(order);
            } catch (NotFoundException ignored) {
            }
        }
        return orderPayments.stream().filter(a->a.getStatusPayment().equals(StatusPayment.PAGO_REALIZADO)).collect(Collectors.toList());
    }
}
