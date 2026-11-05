package com.rupp.payment.service;

import com.rupp.payment.dto.response.StudentDashboardResponse;
import com.rupp.payment.dto.response.StudentProfileSummaryCardResponse;
import com.rupp.payment.dto.response.TuitionCalculationResponse;
import com.rupp.payment.entity.*;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.PaymentRepository;
import com.rupp.payment.repository.SemesterRepository;
import com.rupp.payment.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TuitionFeeService tuitionFeeService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User studentUser;
    private Student student;
    private Faculty faculty;
    private AcademicYear academicYear;
    private Major major;

    @BeforeEach
    void setUp() {
        studentUser = new User();
        studentUser.setId(5L);
        studentUser.setUsername("leangkhai");
        studentUser.setEmail("leangkhai@rupp.edu.kh");
        studentUser.setRole(Role.ROLE_STUDENT);

        faculty = new Faculty("Telecommunication Engineering", "Faculty of Engineering");
        faculty.setId(2L);

        academicYear = new AcademicYear("2026-2027", LocalDate.of(2026, 10, 1), LocalDate.of(2027, 7, 31));
        academicYear.setId(3L);

        major = new Major("Telecommunication Engineering", "TE-01", faculty, "Telecommunication Engineering Program");
        major.setId(4L);

        student = new Student(10L, studentUser, "Taing", "Leangkhai", faculty, academicYear, major);
    }

    @Test
    @DisplayName("Get student dashboard successfully aggregates summary card, quick services, and bulletins")
    void getStudentDashboard_success() {
        when(studentRepository.findByUser(studentUser)).thenReturn(Optional.of(student));
        when(notificationService.getUnreadCount(studentUser)).thenReturn(3L);
        when(paymentRepository.findByStudentId(eq(10L), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        TuitionCalculationResponse calc = new TuitionCalculationResponse();
        calc.setTotalTuitionFee(new BigDecimal("600.00"));
        calc.setTotalPaid(new BigDecimal("600.00"));
        calc.setRemainingBalance(BigDecimal.ZERO);
        calc.setFullyPaid(true);
        calc.setPaymentStatus("PAID");
        calc.setDueDate(LocalDate.of(2026, 11, 30));

        when(tuitionFeeService.calculateStudentTuition(10L)).thenReturn(calc);

        StudentDashboardResponse response = dashboardService.getStudentDashboard(studentUser);

        assertNotNull(response);
        assertTrue(response.getGreeting().contains("Taing") || response.getGreeting().contains("Leangkhai"));
        assertEquals(3, response.getUnreadNotificationsCount());
        assertNotNull(response.getSummaryCard());
        assertEquals("TL", response.getSummaryCard().getInitials());
        assertEquals("PAID", response.getSummaryCard().getPaymentStatus());
        assertEquals(4, response.getQuickServices().size());
        assertEquals(2, response.getAnnouncements().size());
    }

    @Test
    @DisplayName("Get student dashboard by ID for admin/finance staff")
    void getStudentDashboardById_success() {
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(notificationService.getUnreadCount(studentUser)).thenReturn(1L);
        when(paymentRepository.findByStudentId(eq(10L), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        StudentDashboardResponse response = dashboardService.getStudentDashboardById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getStudent().getId());
    }

    @Test
    @DisplayName("Get profile summary card standalone")
    void getProfileSummaryCard_success() {
        when(studentRepository.findByUser(studentUser)).thenReturn(Optional.of(student));

        StudentProfileSummaryCardResponse card = dashboardService.getProfileSummaryCard(studentUser);

        assertNotNull(card);
        assertEquals("Taing Leangkhai", card.getFullName());
        assertEquals("TL", card.getInitials());
        assertEquals("Telecommunication Engineering", card.getFaculty());
        assertEquals("Telecommunication Engineering", card.getMajor());
        assertEquals("ACTIVE", card.getEnrollmentStatus());
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when student profile is missing")
    void getStudentDashboard_missingStudent_throwsException() {
        when(studentRepository.findByUser(studentUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dashboardService.getStudentDashboard(studentUser));
    }
}
