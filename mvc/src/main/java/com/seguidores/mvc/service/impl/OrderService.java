package com.seguidores.mvc.service.impl;

import com.seguidores.mvc.exceptions.NotFoundException;
import com.seguidores.mvc.models.entity.OrderPayment;
import com.seguidores.mvc.models.entity.StatusPayment;
import com.seguidores.mvc.models.repository.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderPaymentRepository orderPaymentRepository;
    private final ServiceImpl service;
    public OrderPayment updateEventPagoStatus(long orderId, String email) {
        OrderPayment orderPayment=orderPaymentRepository.findByIdAndEmail(orderId,email).orElse(null);
        if (orderPayment!=null){
            orderPayment.setStatusPayment(StatusPayment.PAGO_REALIZADO);
          //  orderPayment.setOrderId(service.add(orderPayment.getIdService(),orderPayment.getLink(),orderPayment.getQuantity()));
            //orderPayment.setStatusOrder(service.status(orderPayment.getOrderId()));
            orderPaymentRepository.save(orderPayment);
        }else {
            throw new NotFoundException("The order not exist.");
        }
        return orderPayment;
    }
}
