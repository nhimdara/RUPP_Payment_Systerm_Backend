package com.rupp.payment.dto.response;

import java.util.List;

/**
 * Composite response payload for the Student Home Dashboard.
 */
public class StudentDashboardResponse {

    private String greeting;
    private StudentResponse student;
    private StudentProfileSummaryCardResponse summaryCard;
    private String academicTermLabel;
    private String studyShift;
    private int unreadNotificationsCount;
    private List<QuickServiceActionResponse> quickServices;
    private List<PaymentResponse> recentPayments;
    private List<CampusAnnouncementResponse> announcements;

    public StudentDashboardResponse() {
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public StudentResponse getStudent() {
        return student;
    }

    public void setStudent(StudentResponse student) {
        this.student = student;
    }

    public StudentProfileSummaryCardResponse getSummaryCard() {
        return summaryCard;
    }

    public void setSummaryCard(StudentProfileSummaryCardResponse summaryCard) {
        this.summaryCard = summaryCard;
    }

    public String getAcademicTermLabel() {
        return academicTermLabel;
    }

    public void setAcademicTermLabel(String academicTermLabel) {
        this.academicTermLabel = academicTermLabel;
    }

    public String getStudyShift() {
        return studyShift;
    }

    public void setStudyShift(String studyShift) {
        this.studyShift = studyShift;
    }

    public int getUnreadNotificationsCount() {
        return unreadNotificationsCount;
    }

    public void setUnreadNotificationsCount(int unreadNotificationsCount) {
        this.unreadNotificationsCount = unreadNotificationsCount;
    }

    public List<QuickServiceActionResponse> getQuickServices() {
        return quickServices;
    }

    public void setQuickServices(List<QuickServiceActionResponse> quickServices) {
        this.quickServices = quickServices;
    }

    public List<PaymentResponse> getRecentPayments() {
        return recentPayments;
    }

    public void setRecentPayments(List<PaymentResponse> recentPayments) {
        this.recentPayments = recentPayments;
    }

    public List<CampusAnnouncementResponse> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<CampusAnnouncementResponse> announcements) {
        this.announcements = announcements;
    }
}
