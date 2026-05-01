package edu.cit.gako.brainbox.modules.category.repository;

import edu.cit.gako.brainbox.modules.category.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
}
