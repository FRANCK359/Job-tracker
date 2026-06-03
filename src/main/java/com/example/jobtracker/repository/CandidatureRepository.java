package com.example.jobtracker.repository;

import com.example.jobtracker.model.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    List<Candidature> findByNomEntrepriseContainingIgnoreCase(String nom);

    List<Candidature> findByDateCandidature(LocalDate date);
}
