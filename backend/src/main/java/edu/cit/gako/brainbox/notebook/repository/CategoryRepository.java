package com.example.brainbox_api.notebook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.brainbox_api.notebook.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
