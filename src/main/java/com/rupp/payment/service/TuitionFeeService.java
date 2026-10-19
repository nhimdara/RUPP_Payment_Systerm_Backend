package com.rupp.payment.service;

import com.rupp.payment.dto.request.CreateTuitionFeeRequest;
import com.rupp.payment.dto.request.UpdateTuitionFeeRequest;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.dto.response.TuitionCalculationResponse;
import com.rupp.payment.dto.response.TuitionFeeResponse;
import com.rupp.payment.entity.*;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ConflictException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.AcademicYearRepository;
import com.rupp.payment.repository.FacultyRepository;
import com.rupp.payment.repository.StudentRepository;
import com.rupp.payment.repository.TuitionFeeStructureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TuitionFeeService {

    private final TuitionFeeStructureRepository tuitionFeeRepository;
    private final FacultyRepository facultyRepository;
    private final AcademicYearRepository academicYearRepository;
    private final StudentRepository studentRepository;
    private final com.rupp.payment.repository.PaymentRepository paymentRepository;

    public TuitionFeeService(TuitionFeeStructureRepository tuitionFeeRepository,
                             FacultyRepository facultyRepository,
                             AcademicYearRepository academicYearRepository,
                             StudentRepository studentRepository,
                             com.rupp.payment.repository.PaymentRepository paymentRepository) {
        this.tuitionFeeRepository = tuitionFeeRepository;
        this.facultyRepository = facultyRepository;
        this.academicYearRepository = academicYearRepository;
        this.studentRepository = studentRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public List<TuitionFeeResponse> getAllTuitionFees(Long facultyId, Long academicYearId) {
        List<TuitionFeeStructure> list;
        if (facultyId != null && academicYearId != null) {
            list = tuitionFeeRepository.findByFacultyIdAndAcademicYearId(facultyId, academicYearId)
                    .map(List::of)
                    .orElse(List.of());
        } else if (facultyId != null) {
            list = tuitionFeeRepository.findByFacultyId(facultyId);
        } else if (academicYearId != null) {
            list = tuitionFeeRepository.findByAcademicYearId(academicYearId);
        } else {
            list = tuitionFeeRepository.findAll();
        }

        return list.stream()
                .map(TuitionFeeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TuitionFeeResponse getTuitionFeeById(Long id) {
        TuitionFeeStructure fee = tuitionFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TuitionFeeStructure", "id", id));
        return TuitionFeeResponse.fromEntity(fee);
    }

    @Transactional(readOnly = true)
    public TuitionFeeResponse getMyTuitionFee(User currentUser) {
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile found for user: " + currentUser.getUsername()));

        if (student.getFaculty() == null || student.getAcademicYear() == null) {
            throw new BadRequestException("You have not selected your faculty or academic year yet. Please select your academic info first.");
        }

        TuitionFeeStructure fee = tuitionFeeRepository.findByFacultyIdAndAcademicYearId(
                student.getFaculty().getId(),
                student.getAcademicYear().getId()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "No tuition fee structure found for faculty: " + student.getFaculty().getFacultyName() +
                        " and academic year: " + student.getAcademicYear().getYearLabel()
        ));

        return TuitionFeeResponse.fromEntity(fee);
    }

    @Transactional(readOnly = true)
    public TuitionCalculationResponse calculateMyTuition(User currentUser) {
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile found for user: " + currentUser.getUsername()));
        return calculateTuitionForStudent(student);
    }

    @Transactional(readOnly = true)
    public TuitionCalculationResponse calculateStudentTuition(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        return calculateTuitionForStudent(student);
    }

    private TuitionCalculationResponse calculateTuitionForStudent(Student student) {
        if (student.getFaculty() == null || student.getAcademicYear() == null) {
            throw new BadRequestException("You have not selected your faculty or academic year yet. Please select your academic info first.");
        }

        TuitionFeeStructure fee = tuitionFeeRepository.findByFacultyIdAndAcademicYearId(
                student.getFaculty().getId(),
                student.getAcademicYear().getId()
        ).orElseThrow(() -> new ResourceNotFoundException(
                "No tuition fee structure found for faculty: " + student.getFaculty().getFacultyName() +
                        " and academic year: " + student.getAcademicYear().getYearLabel()
        ));

        List<Payment> payments = paymentRepository.findByStudentIdAndFeeStructureId(student.getId(), fee.getId());

        java.math.BigDecimal totalTuitionFee = fee.getTotalAmount();
        java.math.BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.CONFIRMED)
                .map(Payment::getTotalPaid)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal remainingBalance = totalTuitionFee.subtract(totalPaid);
        if (remainingBalance.compareTo(java.math.BigDecimal.ZERO) < 0) {
            remainingBalance = java.math.BigDecimal.ZERO;
        }

        boolean fullyPaid = remainingBalance.compareTo(java.math.BigDecimal.ZERO) == 0 && totalPaid.compareTo(java.math.BigDecimal.ZERO) > 0;
        String status;
        if (totalPaid.compareTo(java.math.BigDecimal.ZERO) == 0) {
            status = "UNPAID";
        } else if (remainingBalance.compareTo(java.math.BigDecimal.ZERO) == 0) {
            status = "PAID";
        } else {
            status = "PARTIAL";
        }

        List<PaymentResponse> paymentHistory = payments.stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());

        String studentName = (student.getFirstName() != null ? student.getFirstName() : "") +
                (student.getLastName() != null ? " " + student.getLastName() : "");
        String email = student.getUser() != null ? student.getUser().getEmail() : "";
        String facultyName = student.getFaculty() != null ? student.getFaculty().getFacultyName() : "";
        String academicYear = student.getAcademicYear() != null ? student.getAcademicYear().getYearLabel() : "";
        String majorName = student.getMajor() != null ? student.getMajor().getMajorName() : "General";

        return new TuitionCalculationResponse(
                student.getId(),
                studentName.trim(),
                email,
                facultyName,
                academicYear,
                majorName,
                totalTuitionFee,
                totalPaid,
                remainingBalance,
                fullyPaid,
                status,
                fee.getDueDate(),
                paymentHistory
        );
    }

    @Transactional
    public TuitionFeeResponse createTuitionFee(CreateTuitionFeeRequest request) {
        if (tuitionFeeRepository.existsByFacultyIdAndAcademicYearId(request.getFacultyId(), request.getAcademicYearId())) {
            throw new ConflictException("A tuition fee structure already exists for the selected faculty and academic year");
        }

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));

        AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));

        TuitionFeeStructure fee = new TuitionFeeStructure(
                faculty,
                academicYear,
                request.getTotalAmount(),
                request.getDueDate()
        );

        TuitionFeeStructure saved = tuitionFeeRepository.save(fee);
        return TuitionFeeResponse.fromEntity(saved);
    }

    @Transactional
    public TuitionFeeResponse updateTuitionFee(Long id, UpdateTuitionFeeRequest request) {
        TuitionFeeStructure fee = tuitionFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TuitionFeeStructure", "id", id));

        Long targetFacultyId = request.getFacultyId() != null ? request.getFacultyId() : fee.getFaculty().getId();
        Long targetYearId = request.getAcademicYearId() != null ? request.getAcademicYearId() : fee.getAcademicYear().getId();

        if (tuitionFeeRepository.existsByFacultyIdAndAcademicYearIdAndIdNot(targetFacultyId, targetYearId, id)) {
            throw new ConflictException("Another tuition fee structure already exists for the selected faculty and academic year");
        }

        if (request.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));
            fee.setFaculty(faculty);
        }

        if (request.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));
            fee.setAcademicYear(academicYear);
        }

        if (request.getTotalAmount() != null) {
            fee.setTotalAmount(request.getTotalAmount());
        }

        if (request.getDueDate() != null) {
            fee.setDueDate(request.getDueDate());
        }

        TuitionFeeStructure updated = tuitionFeeRepository.save(fee);
        return TuitionFeeResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteTuitionFee(Long id) {
        TuitionFeeStructure fee = tuitionFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TuitionFeeStructure", "id", id));

        if (paymentRepository.existsByFeeStructureId(id)) {
            throw new ConflictException("Cannot delete tuition fee structure ID " + id + " because payments have been recorded against it.");
        }

        tuitionFeeRepository.delete(fee);
    }
}
