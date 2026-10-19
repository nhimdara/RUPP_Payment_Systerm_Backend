package com.rupp.payment.controller;

import com.rupp.payment.dto.response.ApiResponse;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faculties")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Faculty Management", description = "Endpoints for retrieving academic faculties")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    @Operation(summary = "List all faculties", description = "Retrieve all academic faculties.")
    public ResponseEntity<ApiResponse<List<Faculty>>> getAllFaculties() {
        List<Faculty> faculties = facultyService.getAllFaculties();
        return ResponseEntity.ok(ApiResponse.success("Faculties fetched successfully", faculties));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get faculty by ID", description = "Retrieve a specific faculty by its ID.")
    public ResponseEntity<ApiResponse<Faculty>> getFacultyById(@PathVariable Long id) {
        Faculty faculty = facultyService.getFacultyById(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty fetched successfully", faculty));
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create faculty", description = "Admin creates a new academic faculty.")
    public ResponseEntity<ApiResponse<Faculty>> createFaculty(@jakarta.validation.Valid @RequestBody com.rupp.payment.dto.request.CreateFacultyRequest request) {
        Faculty created = facultyService.createFaculty(request);
        return new ResponseEntity<>(ApiResponse.success("Faculty created successfully", created), org.springframework.http.HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update faculty", description = "Admin updates an existing faculty's name or department.")
    public ResponseEntity<ApiResponse<Faculty>> updateFaculty(@PathVariable Long id,
                                                              @jakarta.validation.Valid @RequestBody com.rupp.payment.dto.request.UpdateFacultyRequest request) {
        Faculty updated = facultyService.updateFaculty(id, request);
        return ResponseEntity.ok(ApiResponse.success("Faculty updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete faculty", description = "Admin deletes an existing faculty.")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty deleted successfully", null));
    }
}
