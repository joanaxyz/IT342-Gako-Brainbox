package edu.cit.gako.brainbox.notebook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.gako.brainbox.notebook.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
}
