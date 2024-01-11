package com.seguidores.mvc.models.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebhookNotificationData {
    private String id;

    public WebhookNotificationData(String id) {
        this.id = id;
    }

    public WebhookNotificationData() {
    }
}

