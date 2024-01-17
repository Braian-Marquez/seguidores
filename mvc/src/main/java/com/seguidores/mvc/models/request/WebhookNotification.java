package com.seguidores.mvc.models.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@ToString
public class WebhookNotification {
    private Long id;
    @JsonProperty("live_mode")
    private boolean liveMode;
    private String type;
    @JsonProperty("date_created")
    private String dateCreated;
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("api_version")
    private String apiVersion;
    private String action;
    private WebhookNotificationData data;

    public WebhookNotification(Long id, boolean liveMode, String type, String dateCreated, Long userId, String apiVersion, String action, WebhookNotificationData data) {
        this.id = id;
        this.liveMode = liveMode;
        this.type = type;
        this.dateCreated = dateCreated;
        this.userId = userId;
        this.apiVersion = apiVersion;
        this.action = action;
        this.data = data;
    }

    public WebhookNotification() {
    }
}
