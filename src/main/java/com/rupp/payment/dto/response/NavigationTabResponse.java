package com.rupp.payment.dto.response;

public class NavigationTabResponse {

    private String tabKey;
    private String label;
    private String route;
    private Integer badgeCount;
    private String status;
    private boolean isDefault;

    public NavigationTabResponse() {
    }

    public NavigationTabResponse(String tabKey, String label, String route, Integer badgeCount, String status, boolean isDefault) {
        this.tabKey = tabKey;
        this.label = label;
        this.route = route;
        this.badgeCount = badgeCount;
        this.status = status;
        this.isDefault = isDefault;
    }

    public String getTabKey() {
        return tabKey;
    }

    public void setTabKey(String tabKey) {
        this.tabKey = tabKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Integer getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(Integer badgeCount) {
        this.badgeCount = badgeCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
