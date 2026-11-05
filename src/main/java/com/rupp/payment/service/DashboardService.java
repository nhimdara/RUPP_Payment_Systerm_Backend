package com.rupp.payment.service;

import com.rupp.payment.dto.response.*;
import com.rupp.payment.entity.*;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.PaymentRepository;
import com.rupp.payment.repository.SemesterRepository;
import com.rupp.payment.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final TuitionFeeService tuitionFeeService;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;
    private final SemesterRepository semesterRepository;

    public DashboardService(StudentRepository studentRepository,
                            TuitionFeeService tuitionFeeService,
                            NotificationService notificationService,
                            PaymentRepository paymentRepository,
                            SemesterRepository semesterRepository) {
        this.studentRepository = studentRepository;
        this.tuitionFeeService = tuitionFeeService;
        this.notificationService = notificationService;
        this.paymentRepository = paymentRepository;
        this.semesterRepository = semesterRepository;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard(User currentUser) {
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile found for user: " + currentUser.getUsername()));
        return buildDashboardForStudent(student, currentUser);
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboardById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        return buildDashboardForStudent(student, student.getUser());
    }

    @Transactional(readOnly = true)
    public StudentProfileSummaryCardResponse getProfileSummaryCard(User currentUser) {
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile found for user: " + currentUser.getUsername()));
        return buildSummaryCard(student, currentUser);
    }

    private StudentDashboardResponse buildDashboardForStudent(Student student, User user) {
        StudentDashboardResponse response = new StudentDashboardResponse();

        String greetingName = student.getFirstName() != null ? student.getFirstName() : student.getLastName();
        response.setGreeting("Good morning, " + greetingName + " 👋");
        response.setStudent(StudentResponse.fromEntity(student));
        response.setStudyShift("Morning");

        String academicYearLabel = student.getAcademicYear() != null ? student.getAcademicYear().getYearLabel() : "2026-2027";
        response.setAcademicTermLabel("Term: " + academicYearLabel + " • Semester 1");

        // Profile summary card
        StudentProfileSummaryCardResponse summaryCard = buildSummaryCard(student, user);
        response.setSummaryCard(summaryCard);

        // Notifications unread count
        int unreadCount = user != null ? (int) notificationService.getUnreadCount(user) : 0;
        response.setUnreadNotificationsCount(unreadCount);

        // Quick services hub
        response.setQuickServices(Arrays.asList(
                new QuickServiceActionResponse("pay", "Pay Tuition", "KHQR & ABA", "/api/v1/payments", "TUITION", null),
                new QuickServiceActionResponse("fees", "Fee Breakdown", "Tuition rates", "/api/v1/tuition-fees/calculate", "TUITION", null),
                new QuickServiceActionResponse("history", "Payment History", "Past records", "/api/v1/payments/my-payments", "HISTORY", null),
                new QuickServiceActionResponse("receipts", "Official Receipts", "Download PDF", "/api/v1/receipts", "HISTORY", null)
        ));

        // Recent transactions
        List<Payment> recentPaymentList = paymentRepository.findByStudentId(
                student.getId(),
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "paymentDate"))
        ).getContent();

        List<PaymentResponse> recentPaymentResponses = recentPaymentList.stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
        response.setRecentPayments(recentPaymentResponses);

        // Campus bulletins / announcements
        response.setAnnouncements(Arrays.asList(
                new CampusAnnouncementResponse(
                        "ann1",
                        "Academic Year 2026-2027 Semester 1 Enrollment",
                        "Tuition payment via KHQR Bakong and ABA PayWay is open until 30 November 2026. Official receipts will be generated automatically upon bank clearance.",
                        "Nov 02, 2026",
                        "Important",
                        true
                ),
                new CampusAnnouncementResponse(
                        "ann2",
                        "Department of Telecommunication Lab Upgrades",
                        "Room 304 STEM Building is scheduled for fiber optic and network spectrum analyzer maintenance from Friday to Sunday.",
                        "Oct 25, 2026",
                        "Notice",
                        false
                )
        ));

        return response;
    }

    private StudentProfileSummaryCardResponse buildSummaryCard(Student student, User user) {
        StudentProfileSummaryCardResponse card = new StudentProfileSummaryCardResponse();
        card.setStudentId(student.getId());
        card.setStudentIdCode("e" + (20230000 + student.getId()));
        String first = student.getFirstName() != null ? student.getFirstName() : "";
        String last = student.getLastName() != null ? student.getLastName() : "";
        String fullName = (first + " " + last).trim();
        card.setFullName(fullName);
        card.setKhmerName(getKhmerName(fullName));
        card.setInitials(getInitials(fullName));
        card.setEmail(user != null ? user.getEmail() : "");
        card.setFaculty(student.getFaculty() != null ? student.getFaculty().getFacultyName() : "Faculty of Engineering");
        card.setMajor(student.getMajor() != null ? student.getMajor().getMajorName() : "Telecommunication Engineering");
        card.setAcademicYear(student.getAcademicYear() != null ? student.getAcademicYear().getYearLabel() : "2026-2027");
        card.setYear(3);
        card.setSemester(1);
        card.setShift("Morning");
        card.setEnrollmentStatus("ACTIVE");

        // Calculate tuition balance snapshot
        try {
            TuitionCalculationResponse calc = tuitionFeeService.calculateStudentTuition(student.getId());
            card.setTotalTuitionFee(calc.getTotalTuitionFee());
            card.setTotalPaid(calc.getTotalPaid());
            card.setRemainingBalance(calc.getRemainingBalance());
            card.setFullyPaid(calc.isFullyPaid());
            card.setPaymentStatus(calc.getPaymentStatus());
            card.setDueDate(calc.getDueDate());
        } catch (Exception e) {
            card.setTotalTuitionFee(new BigDecimal("600.00"));
            card.setTotalPaid(BigDecimal.ZERO);
            card.setRemainingBalance(new BigDecimal("600.00"));
            card.setFullyPaid(false);
            card.setPaymentStatus("UNPAID");
            card.setDueDate(LocalDate.now().plusMonths(1));
        }

        return card;
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "ST";
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        return sb.length() > 0 ? sb.toString() : "ST";
    }

    private String getKhmerName(String fullName) {
        if (fullName.toLowerCase().contains("leangkhai")) return "តាំង លាងខាយ";
        if (fullName.toLowerCase().contains("dara")) return "ញឹម ដារ៉ា";
        if (fullName.toLowerCase().contains("ayutaya")) return "តាប់ អាយុធ្យា";
        return "";
    }
}
