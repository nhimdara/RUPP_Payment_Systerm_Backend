package com.rupp.payment.service;

import com.rupp.payment.dto.request.CreateAcademicYearRequest;
import com.rupp.payment.dto.request.UpdateAcademicYearRequest;
import com.rupp.payment.dto.response.AcademicYearResponse;
import com.rupp.payment.entity.AcademicYear;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.AcademicYearRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final com.rupp.payment.repository.SemesterRepository semesterRepository;
    private final com.rupp.payment.repository.StudentRepository studentRepository;

    public AcademicYearService(AcademicYearRepository academicYearRepository,
                               com.rupp.payment.repository.SemesterRepository semesterRepository,
                               com.rupp.payment.repository.StudentRepository studentRepository) {
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAllAcademicYears() {
        return academicYearRepository.findAll().stream()
                .map(AcademicYearResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademicYearResponse getAcademicYearById(Long id) {
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));
        return AcademicYearResponse.fromEntity(academicYear);
    }

    @Transactional
    public AcademicYearResponse createAcademicYear(CreateAcademicYearRequest request) {
        if (academicYearRepository.existsByYearLabel(request.getYearLabel().trim())) {
            throw new BadRequestException("Academic year label already exists: " + request.getYearLabel());
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        AcademicYear academicYear = new AcademicYear();
        academicYear.setYearLabel(request.getYearLabel().trim());
        academicYear.setStartDate(request.getStartDate());
        academicYear.setEndDate(request.getEndDate());

        AcademicYear saved = academicYearRepository.save(academicYear);
        return AcademicYearResponse.fromEntity(saved);
    }

    @Transactional
    public AcademicYearResponse updateAcademicYear(Long id, UpdateAcademicYearRequest request) {
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));

        if (request.getYearLabel() != null && !request.getYearLabel().isBlank()) {
            String trimmed = request.getYearLabel().trim();
            if (academicYearRepository.existsByYearLabelAndIdNot(trimmed, id)) {
                throw new BadRequestException("Academic year label already exists: " + trimmed);
            }
            academicYear.setYearLabel(trimmed);
        }

        if (request.getStartDate() != null) {
            academicYear.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            academicYear.setEndDate(request.getEndDate());
        }

        if (academicYear.getStartDate() != null && academicYear.getEndDate() != null
                && academicYear.getStartDate().isAfter(academicYear.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        AcademicYear updated = academicYearRepository.save(academicYear);
        return AcademicYearResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteAcademicYear(Long id) {
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));

        if (!semesterRepository.findByAcademicYearId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete academic year: it is currently referenced by existing semesters.");
        }

        if (!studentRepository.findByAcademicYearId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete academic year: it is currently referenced by enrolled students.");
        }

        academicYearRepository.delete(academicYear);
    }
}
