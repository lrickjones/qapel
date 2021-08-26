package com.qapel.rfid.repository;

import com.qapel.rfid.entities.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JBA repository for station definitions stored in station table
 */
@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {
    List<Station> findByName(String station);
}
