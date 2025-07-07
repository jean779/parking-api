package com.estapar.parking.repository;

import com.estapar.parking.entity.GarageSector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GarageSectorRepository extends JpaRepository<GarageSector, String> {
    Optional<GarageSector> findBySector(String sector);
}
