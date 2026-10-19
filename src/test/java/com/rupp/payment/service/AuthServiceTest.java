package com.rupp.payment.service;

import com.rupp.payment.dto.request.RegisterRequest;
import com.rupp.payment.dto.response.AuthResponse;
import com.rupp.payment.entity.AcademicYear;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Role;
import com.rupp.payment.entity.Student;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.DuplicateResourceException;
import com.rupp.payment.repository.AcademicYearRepository;
import com.rupp.payment.repository.FacultyRepository;
import com.rupp.payment.repository.StudentRepository;
import com.rupp.payment.repository.UserRepository;
import com.rupp.payment.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newstudent");
        registerRequest.setEmail("student@rupp.edu.kh");
        registerRequest.setPassword("secret123");
        registerRequest.setFirstName("Sok");
        registerRequest.setLastName("Chan");
    }

    @Test
    @DisplayName("register creates user with ROLE_STUDENT and creates student profile")
    void testRegister_Success() {
        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("student@rupp.edu.kh")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_pass");

        User savedUser = new User();
        savedUser.setId(5L);
        savedUser.setUsername("newstudent");
        savedUser.setEmail("student@rupp.edu.kh");
        savedUser.setRole(Role.ROLE_STUDENT);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));
        when(tokenProvider.generateAccessToken(any(), any(), any())).thenReturn("mock_access_token");
        when(tokenProvider.generateRefreshToken(any())).thenReturn("mock_refresh_token");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock_access_token", response.getAccessToken());
        assertEquals("mock_refresh_token", response.getRefreshToken());
        assertEquals("newstudent", response.getUser().getUsername());
        assertEquals(Role.ROLE_STUDENT, response.getUser().getRole());

        verify(userRepository).save(any(User.class));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("register throws DuplicateResourceException when username exists")
    void testRegister_DuplicateUsername() {
        when(userRepository.existsByUsername("newstudent")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                authService.register(registerRequest)
        );

        assertTrue(ex.getMessage().contains("Username 'newstudent' is already taken"));
        verify(userRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("register throws DuplicateResourceException when email exists")
    void testRegister_DuplicateEmail() {
        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("student@rupp.edu.kh")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                authService.register(registerRequest)
        );

        assertTrue(ex.getMessage().contains("Email 'student@rupp.edu.kh' is already in use"));
        verify(userRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
    }
}
