package edu.cit.gako.brainbox.notebook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotebookRepository extends JpaRepository<Notebook, Long> {
    Optional<Notebook> findByUuid(String uuid);
    Optional<Notebook> findByUuidAndUserId(String uuid, Long userId);
    List<Notebook> findByUserId(Long userId);
    List<Notebook> findByCategoryIdAndUserId(Long categoryId, Long userId);
    List<Notebook> findTop6ByUserIdOrderByUpdatedAtDesc(Long userId);
    List<Notebook> findTop3ByUserIdAndLastReviewedAtNotNullOrderByLastReviewedAtDesc(Long userId);
    boolean existsByUuid(String uuid);

    @Modifying
    @Query("UPDATE Notebook n SET n.lastReviewedAt = :reviewedAt WHERE n.uuid = :uuid AND n.user.id = :userId")
    int updateLastReviewedAt(@Param("uuid") String uuid, @Param("userId") Long userId, @Param("reviewedAt") Instant reviewedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Notebook n
        SET n.category = null
        WHERE n.category.id = :categoryId
          AND n.user.id = :userId
    """)
    int clearCategoryByCategoryIdAndUserId(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId
    );
}
