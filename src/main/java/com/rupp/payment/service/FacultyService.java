package com.rupp.payment.service;

import com.rupp.payment.entity.Faculty;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.FacultyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    private final com.rupp.payment.repository.StudentRepository studentRepository;
    private final com.rupp.payment.repository.MajorRepository majorRepository;
    private final com.rupp.payment.repository.TuitionFeeStructureRepository tuitionFeeRepository;

    public FacultyService(FacultyRepository facultyRepository,
                          com.rupp.payment.repository.StudentRepository studentRepository,
                          com.rupp.payment.repository.MajorRepository majorRepository,
                          com.rupp.payment.repository.TuitionFeeStructureRepository tuitionFeeRepository) {
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.majorRepository = majorRepository;
        this.tuitionFeeRepository = tuitionFeeRepository;
    }

    @Transactional(readOnly = true)
    public List<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Faculty getFacultyById(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", id));
    }

    @Transactional
    public Faculty createFaculty(com.rupp.payment.dto.request.CreateFacultyRequest request) {
        if (facultyRepository.findByFacultyName(request.getFacultyName().trim()).isPresent()) {
            throw new com.rupp.payment.exception.ConflictException("Faculty with name '" + request.getFacultyName().trim() + "' already exists");
        }
        Faculty faculty = new Faculty(request.getFacultyName().trim(), request.getDepartment() != null ? request.getDepartment().trim() : null);
        return facultyRepository.save(faculty);
    }

    @Transactional
    public Faculty updateFaculty(Long id, com.rupp.payment.dto.request.UpdateFacultyRequest request) {
        Faculty faculty = getFacultyById(id);

        if (request.getFacultyName() != null && !request.getFacultyName().isBlank()) {
            String newName = request.getFacultyName().trim();
            facultyRepository.findByFacultyName(newName).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new com.rupp.payment.exception.ConflictException("Faculty with name '" + newName + "' already exists");
                }
            });
            faculty.setFacultyName(newName);
        }

        if (request.getDepartment() != null) {
            faculty.setDepartment(request.getDepartment().trim());
        }

        return facultyRepository.save(faculty);
    }

    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = getFacultyById(id);

        if (studentRepository.existsByFacultyId(id)) {
            throw new com.rupp.payment.exception.ConflictException("Cannot delete faculty ID " + id + " because students are enrolled in it.");
        }
        if (majorRepository.existsByFacultyId(id)) {
            throw new com.rupp.payment.exception.ConflictException("Cannot delete faculty ID " + id + " because majors are associated with it.");
        }
        if (tuitionFeeRepository.existsByFacultyId(id)) {
            throw new com.rupp.payment.exception.ConflictException("Cannot delete faculty ID " + id + " because tuition fee structures are configured for it.");
        }

        facultyRepository.delete(faculty);
    }
}
