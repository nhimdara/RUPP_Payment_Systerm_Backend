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
}
