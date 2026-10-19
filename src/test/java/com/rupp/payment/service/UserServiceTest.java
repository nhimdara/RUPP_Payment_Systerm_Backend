package com.rupp.payment.service;

import com.rupp.payment.dto.request.CreateUserRequest;
import com.rupp.payment.dto.request.UpdateUserRequest;
import com.rupp.payment.dto.response.UserResponse;
import com.rupp.payment.entity.Role;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.DuplicateResourceException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.StudentRepository;
import com.rupp.payment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("test@rupp.edu.kh");
        sampleUser.setPassword("encoded_pass");
        sampleUser.setRole(Role.ROLE_STUDENT);
    }

    @Test
    @DisplayName("createUser succeeds when username and email are unique")
    void testCreateUser_Success() {
        CreateUserRequest request = new CreateUserRequest("newuser", "new@rupp.edu.kh", "secret123", Role.ROLE_STUDENT);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@rupp.edu.kh")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("newuser", response.getUsername());
        assertEquals("new@rupp.edu.kh", response.getEmail());
        assertEquals(Role.ROLE_STUDENT, response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser throws DuplicateResourceException when username is taken")
    void testCreateUser_DuplicateUsername() {
        CreateUserRequest request = new CreateUserRequest("testuser", "new@rupp.edu.kh", "secret123", Role.ROLE_STUDENT);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                userService.createUser(request)
        );

        assertTrue(ex.getMessage().contains("Username 'testuser' is already taken"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser throws DuplicateResourceException when email is taken")
    void testCreateUser_DuplicateEmail() {
        CreateUserRequest request = new CreateUserRequest("uniqueuser", "test@rupp.edu.kh", "secret123", Role.ROLE_STUDENT);
        when(userRepository.existsByUsername("uniqueuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@rupp.edu.kh")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                userService.createUser(request)
        );

        assertTrue(ex.getMessage().contains("Email 'test@rupp.edu.kh' is already in use"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserById returns user when found")
    void testGetUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    @DisplayName("getUserById throws ResourceNotFoundException when not found")
    void testGetUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    @DisplayName("getAllUsers returns filtered list by role")
    void testGetAllUsers_WithRole() {
        when(userRepository.findByRole(Role.ROLE_STUDENT)).thenReturn(List.of(sampleUser));

        List<UserResponse> list = userService.getAllUsers(Role.ROLE_STUDENT);

        assertEquals(1, list.size());
        assertEquals("testuser", list.get(0).getUsername());
    }

    @Test
    @DisplayName("updateUser successfully updates username and email")
    void testUpdateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest("updateduser", "updated@rupp.edu.kh", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@rupp.edu.kh")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.updateUser(1L, request);

        assertNotNull(response);
        verify(userRepository).save(sampleUser);
    }
}
