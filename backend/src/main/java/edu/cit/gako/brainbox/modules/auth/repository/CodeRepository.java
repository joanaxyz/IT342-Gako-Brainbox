package edu.cit.gako.brainbox.modules.auth.repository;

import edu.cit.gako.brainbox.modules.auth.entity.Code;
import edu.cit.gako.brainbox.modules.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long>{
    Optional<Code> findByUser(User user);

    @Modifying
    @Transactional
    void deleteByUser(User user);
}
