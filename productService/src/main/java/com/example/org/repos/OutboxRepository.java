package com.example.org.repos;

import com.example.org.entities.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntity, Long> {

    List<OutboxEntity> findByProcessedFalse();

}
