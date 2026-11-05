package com.rupp.payment.dto.response;

public class QuickServiceActionResponse {

    private String id;
    private String title;
    private String subtitle;
    private String endpoint;
    private String targetScreen;
    private String badge;

    public QuickServiceActionResponse() {
    }

    public QuickServiceActionResponse(String id, String title, String subtitle, String endpoint, String targetScreen, String badge) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.endpoint = endpoint;
        this.targetScreen = targetScreen;
        this.badge = badge;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getTargetScreen() {
        return targetScreen;
    }

    public void setTargetScreen(String targetScreen) {
        this.targetScreen = targetScreen;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }
}
