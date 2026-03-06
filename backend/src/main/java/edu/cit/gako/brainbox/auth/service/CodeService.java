package com.example.brainbox_api.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.brainbox_api.auth.entity.Code;
import com.example.brainbox_api.auth.entity.User;
import com.example.brainbox_api.auth.repository.CodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CodeService {
    private static final String DIGITS = "0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    private final PasswordEncoder passwordEncoder;
    private final CodeRepository codeRepository;

    public Code findByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        return codeRepository.findByUser(user)
                .orElseThrow(() -> new NoSuchElementException("Code with this user does not exist"));
    }

    public String generateCode(User user, int length, int expiration) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }

        codeRepository.findByUser(user).ifPresent(existingCode -> {
            codeRepository.delete(existingCode);
            codeRepository.flush();
        });

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = RNG.nextInt(DIGITS.length());
            sb.append(DIGITS.charAt(idx));
        }

        String rawCode = sb.toString();
        String hashedCode = passwordEncoder.encode(rawCode);
        Code code = Code.builder()
                .user(user)
                .code(hashedCode)
                .expiryDate(Instant.now().plusMillis(expiration))
                .build();
        codeRepository.save(code);
        return rawCode;
    }

    public void delete(Code code) {
        codeRepository.delete(code);
    }
}
