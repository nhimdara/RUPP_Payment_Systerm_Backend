package com.rupp.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.rupp.payment.dto.request.CreateStudentRequest;
import com.rupp.payment.dto.request.UpdateStudentRequest;
import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.dto.response.StudentResponse;
import com.rupp.payment.entity.User;
import com.rupp.payment.service.AuthService;
import com.rupp.payment.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Student Management", description = "Student CRUD operations and profile retrieval")
public class StudentController {

    private final StudentService studentService;
    private final AuthService authService;

    public StudentController(StudentService studentService, AuthService authService) {
        this.studentService = studentService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "List all students", description = "Admin & Finance Staff can list students, with optional filtering by faculty or academic year.")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents(
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long academicYearId) {
        List<StudentResponse> students = studentService.getAllStudents(facultyId, academicYearId);
        return ResponseEntity.ok(ApiResponse.success("Students fetched successfully", students));
    }

    @GetMapping("/my-profile")
    @Operation(summary = "Get current student profile", description = "Allows logged-in students to view their own student profile.")
    public ResponseEntity<ApiResponse<StudentResponse>> getMyProfile() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        StudentResponse response = studentService.getMyStudentProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Student profile fetched successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FINANCE_STAFF')")
    @Operation(summary = "Get student by ID", description = "Retrieve a student record by its student ID.")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success("Student fetched successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create student record", description = "Admin creates a student record linked to an existing user.")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return new ResponseEntity<>(ApiResponse.success("Student created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update student record", description = "Admin updates student details such as name, faculty, or academic year.")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(@PathVariable Long id,
                                                                      @Valid @RequestBody UpdateStudentRequest request) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete student record", description = "Admin deletes a student record.")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully", null));
    }
}
