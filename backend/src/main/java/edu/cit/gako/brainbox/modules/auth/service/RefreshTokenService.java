package edu.cit.gako.brainbox.modules.auth.service;

import edu.cit.gako.brainbox.modules.auth.entity.RefreshToken;
import edu.cit.gako.brainbox.modules.auth.repository.RefreshTokenRepository;
import edu.cit.gako.brainbox.modules.user.entity.User;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.expiration.refresh:604800000}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(User user, String userAgent, String ipAddress) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new IllegalStateException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
