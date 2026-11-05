package com.rupp.payment.dto.response;

public class CampusAnnouncementResponse {

    private String id;
    private String title;
    private String description;
    private String date;
    private String badgeTag;
    private boolean isUrgent;

    public CampusAnnouncementResponse() {
    }

    public CampusAnnouncementResponse(String id, String title, String description, String date, String badgeTag, boolean isUrgent) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.badgeTag = badgeTag;
        this.isUrgent = isUrgent;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBadgeTag() {
        return badgeTag;
    }

    public void setBadgeTag(String badgeTag) {
        this.badgeTag = badgeTag;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }
}
