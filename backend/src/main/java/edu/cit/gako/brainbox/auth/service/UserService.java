package edu.cit.gako.brainbox.auth.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import edu.cit.gako.brainbox.auth.entity.User;
import edu.cit.gako.brainbox.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public User create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        return userRepository.save(user);
    }

    public User update(Long userId, User updatedUser) {
        User user = findById(userId);
        if (updatedUser.getUsername() != null)
            user.setUsername(updatedUser.getUsername());
        if (updatedUser.getEmail() != null)
            user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null)
            user.setPassword(updatedUser.getPassword());
        if (updatedUser.getRole() != null)
            user.setRole(updatedUser.getRole());
        return userRepository.save(user);
    }

    public void updatePassword(Long userId, String hashedPassword){
        User user = findById(userId);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    public void verify(User user) {
        user.setVerified(true);
        userRepository.save(user);
    }

    public User findById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " does not exist"));
    }

    public User findByUsernameOrEmail(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Username or email must not be null or empty");
        }

        return userRepository.findByEmail(value)
                .or(() -> userRepository.findByUsername(value))
                .orElseThrow(() -> new NoSuchElementException("User with this username or email does not exist"));
    }

    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User with this email does not exist"));
    }

    public User findByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User with this username does not exist"));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void validateUniqueness(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    public Optional<User> findByEmailOptional(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
