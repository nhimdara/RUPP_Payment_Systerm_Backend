package com.rupp.payment.service;

import com.rupp.payment.dto.request.CreateSemesterRequest;
import com.rupp.payment.dto.request.UpdateSemesterRequest;
import com.rupp.payment.dto.response.SemesterResponse;
import com.rupp.payment.entity.AcademicYear;
import com.rupp.payment.entity.Semester;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.AcademicYearRepository;
import com.rupp.payment.repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final AcademicYearRepository academicYearRepository;

    public SemesterService(SemesterRepository semesterRepository, AcademicYearRepository academicYearRepository) {
        this.semesterRepository = semesterRepository;
        this.academicYearRepository = academicYearRepository;
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getAllSemesters(Long academicYearId) {
        List<Semester> semesters;
        if (academicYearId != null) {
            semesters = semesterRepository.findByAcademicYearId(academicYearId);
        } else {
            semesters = semesterRepository.findAll();
        }
        return semesters.stream()
                .map(SemesterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SemesterResponse getSemesterById(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", id));
        return SemesterResponse.fromEntity(semester);
    }

    @Transactional(readOnly = true)
    public SemesterResponse getActiveSemester() {
        Semester semester = semesterRepository.findFirstByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active semester currently set"));
        return SemesterResponse.fromEntity(semester);
    }

    @Transactional
    public SemesterResponse createSemester(CreateSemesterRequest request) {
        AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));

        if (semesterRepository.existsByAcademicYearIdAndSemesterNumber(academicYear.getId(), request.getSemesterNumber())) {
            throw new BadRequestException("Semester number " + request.getSemesterNumber()
                    + " already exists for academic year " + academicYear.getYearLabel());
        }

        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        // If this semester is set as active, deactivate other semesters to keep a single active semester
        if (Boolean.TRUE.equals(request.getIsActive())) {
            deactivateAllSemesters();
        }

        Semester semester = new Semester();
        semester.setSemesterName(request.getSemesterName().trim());
        semester.setSemesterNumber(request.getSemesterNumber());
        semester.setAcademicYear(academicYear);
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setIsActive(Boolean.TRUE.equals(request.getIsActive()));

        Semester saved = semesterRepository.save(semester);
        return SemesterResponse.fromEntity(saved);
    }

    @Transactional
    public SemesterResponse updateSemester(Long id, UpdateSemesterRequest request) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", id));

        if (request.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));
            semester.setAcademicYear(academicYear);
        }

        if (request.getSemesterNumber() != null) {
            if (semesterRepository.existsByAcademicYearIdAndSemesterNumberAndIdNot(
                    semester.getAcademicYear().getId(), request.getSemesterNumber(), id)) {
                throw new BadRequestException("Semester number " + request.getSemesterNumber()
                        + " already exists for academic year " + semester.getAcademicYear().getYearLabel());
            }
            semester.setSemesterNumber(request.getSemesterNumber());
        }

        if (request.getSemesterName() != null && !request.getSemesterName().isBlank()) {
            semester.setSemesterName(request.getSemesterName().trim());
        }

        if (request.getStartDate() != null) {
            semester.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            semester.setEndDate(request.getEndDate());
        }

        if (semester.getStartDate() != null && semester.getEndDate() != null
                && semester.getStartDate().isAfter(semester.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        if (Boolean.TRUE.equals(request.getIsActive())) {
            deactivateAllSemesters();
            semester.setIsActive(true);
        } else if (Boolean.FALSE.equals(request.getIsActive())) {
            semester.setIsActive(false);
        }

        Semester updated = semesterRepository.save(semester);
        return SemesterResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteSemester(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester", "id", id));
        semesterRepository.delete(semester);
    }

    private void deactivateAllSemesters() {
        List<Semester> activeSemesters = semesterRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .collect(Collectors.toList());
        for (Semester s : activeSemesters) {
            s.setIsActive(false);
        }
        semesterRepository.saveAll(activeSemesters);
    }
}
