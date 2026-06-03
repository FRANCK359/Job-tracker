package com.example.jobtracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "candidatures")
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomEntreprise;

    private LocalDate dateCandidature;

    private String lienOffre;

    @Column(columnDefinition = "TEXT")
    private String lettreMotivation;

    private String cvJoint;

    @Column(nullable = false)
    private String etat;

    // --- CONSTRUCTEURS ---

    // Constructeur vide obligatoire pour JPA
    public Candidature() {
    }

    // Constructeur complet pour nous aider à créer des objets plus tard
    public Candidature(String nomEntreprise, LocalDate dateCandidature, String lienOffre, String lettreMotivation, String cvJoint, String etat) {
        this.nomEntreprise = nomEntreprise;
        this.dateCandidature = dateCandidature;
        this.lienOffre = lienOffre;
        this.lettreMotivation = lettreMotivation;
        this.cvJoint = cvJoint;
        this.etat = etat;
    }

    // --- GETTERS ET SETTERS ---
    // (Ils permettent de lire et modifier les variables de manière sécurisée)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }

    public LocalDate getDateCandidature() { return dateCandidature; }
    public void setDateCandidature(LocalDate dateCandidature) { this.dateCandidature = dateCandidature; }

    public String getLienOffre() { return lienOffre; }
    public void setLienOffre(String lienOffre) { this.lienOffre = lienOffre; }

    public String getLettreMotivation() { return lettreMotivation; }
    public void setLettreMotivation(String lettreMotivation) { this.lettreMotivation = lettreMotivation; }

    public String getCvJoint() { return cvJoint; }
    public void setCvJoint(String cvJoint) { this.cvJoint = cvJoint; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
}