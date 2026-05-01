package edu.cit.gako.brainbox.modules.auth.repository;

import edu.cit.gako.brainbox.modules.auth.entity.RefreshToken;
import edu.cit.gako.brainbox.modules.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByToken(String token);
}
