package com.rupp.payment.dto.response;

import java.time.LocalDateTime;

public class SystemMonitoringResponse {

    private String systemStatus;
    private String databaseStatus;
    private LocalDateTime serverTime;
    private long uptimeSeconds;
    private long totalMemoryMb;
    private long freeMemoryMb;
    private long usedMemoryMb;
    private int availableProcessors;
    private long totalUsers;
    private long totalStudents;
    private long totalFaculties;
    private long totalMajors;
    private long totalAcademicYears;
    private long totalSemesters;
    private long totalPayments;
    private long totalReceipts;

    public SystemMonitoringResponse() {
    }

    public SystemMonitoringResponse(String systemStatus, String databaseStatus, LocalDateTime serverTime,
                                    long uptimeSeconds, long totalMemoryMb, long freeMemoryMb, long usedMemoryMb,
                                    int availableProcessors, long totalUsers, long totalStudents,
                                    long totalFaculties, long totalMajors, long totalAcademicYears,
                                    long totalSemesters, long totalPayments, long totalReceipts) {
        this.systemStatus = systemStatus;
        this.databaseStatus = databaseStatus;
        this.serverTime = serverTime;
        this.uptimeSeconds = uptimeSeconds;
        this.totalMemoryMb = totalMemoryMb;
        this.freeMemoryMb = freeMemoryMb;
        this.usedMemoryMb = usedMemoryMb;
        this.availableProcessors = availableProcessors;
        this.totalUsers = totalUsers;
        this.totalStudents = totalStudents;
        this.totalFaculties = totalFaculties;
        this.totalMajors = totalMajors;
        this.totalAcademicYears = totalAcademicYears;
        this.totalSemesters = totalSemesters;
        this.totalPayments = totalPayments;
        this.totalReceipts = totalReceipts;
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public LocalDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public long getTotalMemoryMb() {
        return totalMemoryMb;
    }

    public void setTotalMemoryMb(long totalMemoryMb) {
        this.totalMemoryMb = totalMemoryMb;
    }

    public long getFreeMemoryMb() {
        return freeMemoryMb;
    }

    public void setFreeMemoryMb(long freeMemoryMb) {
        this.freeMemoryMb = freeMemoryMb;
    }

    public long getUsedMemoryMb() {
        return usedMemoryMb;
    }

    public void setUsedMemoryMb(long usedMemoryMb) {
        this.usedMemoryMb = usedMemoryMb;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalFaculties() {
        return totalFaculties;
    }

    public void setTotalFaculties(long totalFaculties) {
        this.totalFaculties = totalFaculties;
    }

    public long getTotalMajors() {
        return totalMajors;
    }

    public void setTotalMajors(long totalMajors) {
        this.totalMajors = totalMajors;
    }

    public long getTotalAcademicYears() {
        return totalAcademicYears;
    }

    public void setTotalAcademicYears(long totalAcademicYears) {
        this.totalAcademicYears = totalAcademicYears;
    }

    public long getTotalSemesters() {
        return totalSemesters;
    }

    public void setTotalSemesters(long totalSemesters) {
        this.totalSemesters = totalSemesters;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public long getTotalReceipts() {
        return totalReceipts;
    }

    public void setTotalReceipts(long totalReceipts) {
        this.totalReceipts = totalReceipts;
    }
}
