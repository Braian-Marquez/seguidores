package com.seguidores.mvc.models.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChargeInfo {
    private String charge;
    private String startCount;
    private String status;
    private String remains;
    private String currency;
}
