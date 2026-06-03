package com.example.jobtracker.service;

import com.example.jobtracker.model.Candidature;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface CandidatureService {
    List<Candidature> ObtenirToutesLesCandidatures();
    Candidature sauvegarderCandidature(Candidature candidature, MultipartFile lettreFile, MultipartFile cvFile);
    Candidature modifierCandidature(Long id, Candidature candidatureDetails, MultipartFile lettreFile, MultipartFile cvFile);
    void supprimerCandidature(Long id);
    List<Candidature> rechercherParNomEntreprise(String nom);
    List<Candidature> rechercherParDateCandidature(LocalDate date);
}
