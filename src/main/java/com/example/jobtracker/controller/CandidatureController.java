package com.example.jobtracker.controller;

import com.example.jobtracker.model.Candidature;
import com.example.jobtracker.service.CandidatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "*")
public class CandidatureController {

    @Autowired
    private CandidatureService candidatureService;

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir:uploads/}")
    private String dossierStockage;

    @GetMapping
    public List<Candidature> getAllCandidatures() {
        return candidatureService.ObtenirToutesLesCandidatures();
    }
    // Remplacer l'ancienne méthode de création par celle-ci :
    @PostMapping(consumes = {"multipart/form-data"})
    public Candidature createCandidature(
            @RequestPart("candidature") Candidature candidature,
            @RequestPart(value = "lettreFile", required = false) MultipartFile lettreFile,
            @RequestPart(value = "cvFile", required = false) MultipartFile cvFile) {

        return candidatureService.sauvegarderCandidature(candidature, lettreFile, cvFile);
    }
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public Candidature updateCandidature(
            @PathVariable Long id,
            @RequestPart("candidature") Candidature candidature,
            @RequestPart(value = "lettreFile", required = false) MultipartFile lettreFile,
            @RequestPart(value = "cvFile", required = false) MultipartFile cvFile) {
        return candidatureService.modifierCandidature(id, candidature, lettreFile, cvFile);
    }
    @DeleteMapping("/{id}")
    public String deleteCandidature(@PathVariable Long id) {
        candidatureService.supprimerCandidature(id);
        return "Candidature supprimée avec succès !";
    }
    @GetMapping("/recherche/entreprise")
    public List<Candidature> getCandidatureParEntreprise(@RequestParam String nom) {
        return candidatureService.rechercherParNomEntreprise(nom);
    }
    @GetMapping("/recherche/date")
    public List<Candidature> getCandidatureParDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return candidatureService.rechercherParDateCandidature(date);
    }
    @GetMapping("/fichiers/{nomFichier}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> getFichier(@PathVariable String nomFichier) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(dossierStockage).resolve(nomFichier);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                return org.springframework.http.ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }
}
