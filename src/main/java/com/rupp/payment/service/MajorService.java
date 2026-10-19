package com.rupp.payment.service;

import com.rupp.payment.dto.request.CreateMajorRequest;
import com.rupp.payment.dto.request.UpdateMajorRequest;
import com.rupp.payment.dto.response.MajorResponse;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Major;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.FacultyRepository;
import com.rupp.payment.repository.MajorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MajorService {

    private final MajorRepository majorRepository;
    private final FacultyRepository facultyRepository;
    private final com.rupp.payment.repository.StudentRepository studentRepository;

    public MajorService(MajorRepository majorRepository,
                        FacultyRepository facultyRepository,
                        com.rupp.payment.repository.StudentRepository studentRepository) {
        this.majorRepository = majorRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<MajorResponse> getAllMajors(Long facultyId) {
        List<Major> majors;
        if (facultyId != null) {
            majors = majorRepository.findByFacultyId(facultyId);
        } else {
            majors = majorRepository.findAll();
        }
        return majors.stream()
                .map(MajorResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MajorResponse getMajorById(Long id) {
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Major", "id", id));
        return MajorResponse.fromEntity(major);
    }

    @Transactional
    public MajorResponse createMajor(CreateMajorRequest request) {
        String code = request.getMajorCode().trim().toUpperCase();
        if (majorRepository.existsByMajorCode(code)) {
            throw new BadRequestException("Major code already exists: " + code);
        }

        Major major = new Major();
        major.setMajorName(request.getMajorName().trim());
        major.setMajorCode(code);
        major.setDescription(request.getDescription());

        if (request.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));
            major.setFaculty(faculty);
        }

        Major saved = majorRepository.save(major);
        return MajorResponse.fromEntity(saved);
    }

    @Transactional
    public MajorResponse updateMajor(Long id, UpdateMajorRequest request) {
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Major", "id", id));

        if (request.getMajorName() != null && !request.getMajorName().isBlank()) {
            major.setMajorName(request.getMajorName().trim());
        }

        if (request.getMajorCode() != null && !request.getMajorCode().isBlank()) {
            String code = request.getMajorCode().trim().toUpperCase();
            if (majorRepository.existsByMajorCodeAndIdNot(code, id)) {
                throw new BadRequestException("Major code already exists: " + code);
            }
            major.setMajorCode(code);
        }

        if (request.getDescription() != null) {
            major.setDescription(request.getDescription());
        }

        if (request.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));
            major.setFaculty(faculty);
        }

        Major updated = majorRepository.save(major);
        return MajorResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteMajor(Long id) {
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Major", "id", id));

        if (!studentRepository.findByMajorId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete major: it is currently referenced by enrolled students.");
        }

        majorRepository.delete(major);
    }
}
