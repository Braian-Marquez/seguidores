package com.seguidores.mvc.service.interfaces;

import com.seguidores.mvc.models.response.ServiceResponse;

import java.util.List;

public interface ServiceAPI {
    List<ServiceResponse> getAll();
}
