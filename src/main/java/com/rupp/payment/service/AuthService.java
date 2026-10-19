package com.rupp.payment.service;

import com.rupp.payment.dto.request.LoginRequest;
import com.rupp.payment.dto.request.RefreshTokenRequest;
import com.rupp.payment.dto.request.RegisterRequest;
import com.rupp.payment.dto.response.AuthResponse;
import com.rupp.payment.dto.response.UserResponse;
import com.rupp.payment.entity.AcademicYear;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Role;
import com.rupp.payment.entity.Student;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.DuplicateResourceException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.AcademicYearRepository;
import com.rupp.payment.repository.FacultyRepository;
import com.rupp.payment.repository.StudentRepository;
import com.rupp.payment.repository.UserRepository;
import com.rupp.payment.security.CustomUserDetails;
import com.rupp.payment.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final AcademicYearRepository academicYearRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       StudentRepository studentRepository,
                       FacultyRepository facultyRepository,
                       AcademicYearRepository academicYearRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.academicYearRepository = academicYearRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
<<<<<<< HEAD
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already in use");
=======
        String normalizedUsername = request.getUsername().trim();
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email is already in use");
>>>>>>> origin/feature/academic-structure-crud
        }

        // Public registration always assigns ROLE_STUDENT to prevent privilege escalation.
        // Administrative accounts (ROLE_ADMIN, ROLE_FINANCE_STAFF) must be created by an Admin via /api/v1/users.
        Role role = Role.ROLE_STUDENT;

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        // Create student profile
        Student student = new Student();
        student.setUser(savedUser);
        student.setFirstName(request.getFirstName() != null && !request.getFirstName().isBlank()
                ? request.getFirstName().trim() : savedUser.getUsername());
        student.setLastName(request.getLastName() != null ? request.getLastName().trim() : "");

        if (request.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new BadRequestException("Faculty not found with id: " + request.getFacultyId()));
            student.setFaculty(faculty);
        }

        if (request.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new BadRequestException("Academic year not found with id: " + request.getAcademicYearId()));
            student.setAcademicYear(academicYear);
        }

        studentRepository.save(student);

        String accessToken = tokenProvider.generateAccessToken(
                savedUser.getUsername(),
                savedUser.getRole().name(),
                savedUser.getId()
        );
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getUsername());

        return new AuthResponse(
                accessToken,
                refreshToken,
                tokenProvider.getExpirationMs(),
                UserResponse.fromEntity(savedUser)
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return new AuthResponse(
                accessToken,
                refreshToken,
                tokenProvider.getExpirationMs(),
                UserResponse.fromEntity(user)
        );
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!tokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        String newAccessToken = tokenProvider.generateAccessToken(
                user.getUsername(),
                user.getRole().name(),
                user.getId()
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                tokenProvider.getExpirationMs(),
                UserResponse.fromEntity(user)
        );
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BadRequestException("No authenticated user found");
        }

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));
    }
}
