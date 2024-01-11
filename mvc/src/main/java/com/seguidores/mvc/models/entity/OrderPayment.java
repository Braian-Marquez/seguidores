package com.seguidores.mvc.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String link;
    private String idPayment;
    private int orderId;
    private Integer quantity;
    private Float price;
    @Enumerated(EnumType.STRING)
    private StatusPayment statusPayment;
    private String statusOrder;
    private int idService;
}
