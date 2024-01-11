package com.seguidores.mvc.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewPreferenceRequest implements Serializable {

    private PreferenceItem items;
    private PayerInfo payerInfo;

}
