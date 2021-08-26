package com.qapel.rfid.repository;

import com.qapel.rfid.entities.ReaderConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JBA repository for Reader Configurations (stations table)
 */
@Repository
public interface ReaderConfigurationRepository extends JpaRepository<ReaderConfiguration, Integer> {
}
