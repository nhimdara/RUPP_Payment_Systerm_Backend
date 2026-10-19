package com.rupp.payment.service;

import com.rupp.payment.dto.request.CreateMajorRequest;
import com.rupp.payment.dto.response.MajorResponse;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Major;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.FacultyRepository;
import com.rupp.payment.repository.MajorRepository;
import com.rupp.payment.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MajorServiceTest {

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private MajorService majorService;

    private Major major;
    private Faculty faculty;

    @BeforeEach
    void setUp() {
        faculty = new Faculty("Computer Science", "Faculty of Science");
        faculty.setId(1L);

        major = new Major("Computer Science", "CS", faculty, "Department of CS");
        major.setId(10L);
    }

    @Test
    @DisplayName("createMajor succeeds with valid data")
    void testCreateMajor_Success() {
        CreateMajorRequest request = new CreateMajorRequest("Data Science", "DS", 1L, "Data Science description");

        when(majorRepository.existsByMajorCode("DS")).thenReturn(false);
        when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(majorRepository.save(any(Major.class))).thenAnswer(i -> {
            Major m = i.getArgument(0);
            m.setId(20L);
            return m;
        });

        MajorResponse response = majorService.createMajor(request);

        assertNotNull(response);
        assertEquals(20L, response.getId());
        assertEquals("Data Science", response.getMajorName());
        assertEquals("DS", response.getMajorCode());
        verify(majorRepository).save(any(Major.class));
    }

    @Test
    @DisplayName("createMajor throws BadRequestException when majorCode already exists")
    void testCreateMajor_DuplicateCode() {
        CreateMajorRequest request = new CreateMajorRequest("Computer Science", "CS", 1L, "Duplicate CS");
        when(majorRepository.existsByMajorCode("CS")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                majorService.createMajor(request)
        );

        assertTrue(ex.getMessage().contains("Major code already exists: CS"));
        verify(majorRepository, never()).save(any());
    }

    @Test
    @DisplayName("getMajorById returns major when found")
    void testGetMajorById_Success() {
        when(majorRepository.findById(10L)).thenReturn(Optional.of(major));

        MajorResponse response = majorService.getMajorById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("CS", response.getMajorCode());
    }

    @Test
    @DisplayName("getMajorById throws ResourceNotFoundException when not found")
    void testGetMajorById_NotFound() {
        when(majorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> majorService.getMajorById(99L));
    }
}
