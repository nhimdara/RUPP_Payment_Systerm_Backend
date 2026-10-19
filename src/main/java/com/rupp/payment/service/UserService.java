package com.rupp.payment.service;

import com.rupp.payment.dto.request.CreateUserRequest;
import com.rupp.payment.dto.request.UpdateUserRequest;
import com.rupp.payment.dto.response.UserResponse;
import com.rupp.payment.entity.Role;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.DuplicateResourceException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.StudentRepository;
import com.rupp.payment.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Role role) {
        List<User> users;
        if (role != null) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(UserResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
<<<<<<< HEAD
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already in use");
=======
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already in use");
>>>>>>> origin/feature/academic-structure-crud
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String username = request.getUsername().trim();
            if (!user.getUsername().equalsIgnoreCase(username) && userRepository.existsByUsername(username)) {
                throw new BadRequestException("Username is already taken");
            }
            user.setUsername(username);
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
                throw new BadRequestException("Email is already in use");
            }
            user.setEmail(email);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // If a linked student exists, remove it first
        studentRepository.findByUser(user).ifPresent(studentRepository::delete);

        userRepository.delete(user);
    }
}
