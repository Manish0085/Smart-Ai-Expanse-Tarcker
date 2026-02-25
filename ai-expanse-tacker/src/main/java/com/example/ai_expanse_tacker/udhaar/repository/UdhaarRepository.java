package com.example.ai_expanse_tacker.udhaar.repository;

import com.example.ai_expanse_tacker.udhaar.entity.Udhaar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface UdhaarRepository extends JpaRepository<Udhaar, Long> {
    List<Udhaar> findByUserId(UUID userId);
}
