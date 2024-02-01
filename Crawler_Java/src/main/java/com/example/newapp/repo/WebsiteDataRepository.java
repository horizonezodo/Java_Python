package com.example.newapp.repo;

import com.example.newapp.model.WebsiteData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebsiteDataRepository extends JpaRepository<WebsiteData,Long> {
    Optional<WebsiteData> findByWebsiteName(String websiteName);
}
