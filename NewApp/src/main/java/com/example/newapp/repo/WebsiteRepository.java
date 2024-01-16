package com.example.newapp.repo;

import com.example.newapp.model.Website;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebsiteRepository extends JpaRepository<Website,Long> {
    Optional<Website> getWebsiteById(Long id);
}
