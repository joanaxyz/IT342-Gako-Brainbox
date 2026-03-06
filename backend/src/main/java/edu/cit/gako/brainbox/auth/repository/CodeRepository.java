package com.example.brainbox_api.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import com.example.brainbox_api.auth.entity.Code;
import com.example.brainbox_api.auth.entity.User;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long>{
    Optional<Code> findByUser(User user);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
}
