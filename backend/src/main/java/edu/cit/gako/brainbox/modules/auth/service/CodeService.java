package edu.cit.gako.brainbox.modules.auth.service;

import edu.cit.gako.brainbox.modules.auth.entity.Code;
import edu.cit.gako.brainbox.modules.auth.repository.CodeRepository;
import edu.cit.gako.brainbox.modules.user.entity.User;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
