package com.example.jobtracker.service;

import com.example.jobtracker.model.Candidature;
import com.example.jobtracker.repository.CandidatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class candidatureServiceImpl implements CandidatureService {
    @Autowired
    private CandidatureRepository candidatureRepository;

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir:uploads/}")
    private String dossierStockage;

    @Override
    public List<Candidature> ObtenirToutesLesCandidatures() {
        return candidatureRepository.findAll();
    }

    @Override
    public Candidature sauvegarderCandidature(Candidature candidature, MultipartFile lettreFile, MultipartFile cvFile) {
        try {
            // Créer le dossier s'il n'existe pas encore
            File dossier = new File(dossierStockage);
            if (!dossier.exists()) {
                dossier.mkdirs();
            }

            // Enregistrement de la Lettre de motivation si elle existe
            if (lettreFile != null && !lettreFile.isEmpty()) {
                String nomUniqueLettre = UUID.randomUUID() + "_" + lettreFile.getOriginalFilename();
                Path chemin = Paths.get(dossierStockage, nomUniqueLettre);
                Files.write(chemin, lettreFile.getBytes());
                candidature.setLettreMotivation(nomUniqueLettre); // On stocke le nom du fichier dans Postgres
            }

            // Enregistrement du CV si il existe
            if (cvFile != null && !cvFile.isEmpty()) {
                String nomUniqueCv = UUID.randomUUID() + "_" + cvFile.getOriginalFilename();
                Path chemin = Paths.get(dossierStockage, nomUniqueCv);
                Files.write(chemin, cvFile.getBytes());
                candidature.setCvJoint(nomUniqueCv); // On stocke le nom du fichier dans Postgres
            }

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement des fichiers PDF", e);
        }

        return candidatureRepository.save(candidature);
    }

    @Override
    public Candidature modifierCandidature(Long id, Candidature candidatureDetails, MultipartFile lettreFile, MultipartFile cvFile) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature n'existe pas avec l'id :" + id));

        candidature.setNomEntreprise(candidatureDetails.getNomEntreprise());
        candidature.setDateCandidature(candidatureDetails.getDateCandidature());
        candidature.setLienOffre(candidatureDetails.getLienOffre());
        candidature.setEtat(candidatureDetails.getEtat());

        try {
            // Créer le dossier s'il n'existe pas encore
            File dossier = new File(dossierStockage);
            if (!dossier.exists()) {
                dossier.mkdirs();
            }

            // Mise à jour de la Lettre de motivation si un nouveau fichier est fourni
            if (lettreFile != null && !lettreFile.isEmpty()) {
                String nomUniqueLettre = UUID.randomUUID() + "_" + lettreFile.getOriginalFilename();
                Path chemin = Paths.get(dossierStockage, nomUniqueLettre);
                Files.write(chemin, lettreFile.getBytes());
                candidature.setLettreMotivation(nomUniqueLettre);
            }

            // Mise à jour du CV si un nouveau fichier est fourni
            if (cvFile != null && !cvFile.isEmpty()) {
                String nomUniqueCv = UUID.randomUUID() + "_" + cvFile.getOriginalFilename();
                Path chemin = Paths.get(dossierStockage, nomUniqueCv);
                Files.write(chemin, cvFile.getBytes());
                candidature.setCvJoint(nomUniqueCv);
            }

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la mise à jour des fichiers PDF", e);
        }

        return candidatureRepository.save(candidature);
    }

    @Override
    public void supprimerCandidature(Long id) {
        candidatureRepository.deleteById(id);

    }

    @Override
    public List<Candidature> rechercherParNomEntreprise(String nom) {
        return candidatureRepository.findByNomEntrepriseContainingIgnoreCase(nom);
    }

    @Override
    public List<Candidature> rechercherParDateCandidature(LocalDate date) {
        return candidatureRepository.findByDateCandidature(date);
    }
}
