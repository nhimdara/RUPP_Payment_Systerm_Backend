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

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
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
}
