package com.seguidores.mvc.models.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ServiceResponse {
    private Integer service;
    private String name;
    private String type;
    private String category;
    private String rate;
    private String min;
    private String max;
    private boolean refill;
    private boolean cancel;

}
