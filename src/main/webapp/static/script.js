// Adresse de notre API Backend Spring Boot
const API_URL = `${window.location.origin}/api/candidatures`;

// Attendre que toute la page index.html soit chargée avant de lancer le code
document.addEventListener("DOMContentLoaded", async () => {
    await chargerComposants();
    configurerEvenements();
    chargerCandidatures();
});

// 1. FONCTION : Assemble les morceaux de HTML comme un puzzle
async function chargerComposants() {
    try {
        // Les fichiers sont dans le même dossier que index.html
        document.getElementById('navbar-placeholder').innerHTML = await fetch('navbar.html').then(res => res.text());
        document.getElementById('form-placeholder').innerHTML = await fetch('formulaire.html').then(res => res.text());
        document.getElementById('liste-placeholder').innerHTML = await fetch('liste.html').then(res => res.text());
    } catch (erreur) {
        console.error("Erreur lors du chargement des composants HTML :", erreur);
    }
}

// 2. FONCTION : Récupère les candidatures depuis PostgreSQL et les affiche
async function chargerCandidatures(urlCustom = API_URL) {
    try {
        const reponse = await fetch(urlCustom);
        const candidatures = await reponse.json(); // Transforme la réponse brute en tableau d'objets JavaScript

        const tableBody = document.getElementById("candidaturesTableBody");
        const totalSpan = document.getElementById("totalCandidatures");

        // Sécurité : si les composants ne sont pas encore injectés à l'écran, on attend
        if (!tableBody) return;

        // On vide le tableau avant de le remplir pour éviter les doublons
        tableBody.innerHTML = "";
        totalSpan.innerText = candidatures.length;

        // Boucle sur chaque candidature reçue du backend
        candidatures.forEach(cand => {
            const ligne = document.createElement("tr");

            // On adapte le design du badge en fonction du statut
            let badgeClass = "badge-encours";
            let statutTexte = "En cours";
            if (cand.etat === "entretien") { badgeClass = "badge-entretien"; statutTexte = "Entretien"; }
            else if (cand.etat === "retenu") { badgeClass = "badge-retenu"; statutTexte = "Retenu 🎉"; }
            else if (cand.etat === "echec") { badgeClass = "badge-echec"; statutTexte = "Échec ❌"; }

            // Construction des colonnes de la ligne
            ligne.innerHTML = `
                <td><strong>${cand.nomEntreprise}</strong></td>
                <td>${cand.dateCandidature ? cand.dateCandidature : '-'}</td>
                <td>${cand.lienOffre ? `<a href="${cand.lienOffre}" target="_blank" class="link-btn"><i class="fas fa-external-link-alt"></i> Voir</a>` : '-'}</td>
                <td>${cand.lettreMotivation ? `<a href="${API_URL}/fichiers/${cand.lettreMotivation}" target="_blank" class="file-btn"><i class="fas fa-file-pdf"></i></a>` : '-'}</td>
                <td>${cand.cvJoint ? `<a href="${API_URL}/fichiers/${cand.cvJoint}" target="_blank" class="file-btn"><i class="fas fa-id-card"></i></a>` : '-'}</td>
                <td><span class="badge ${badgeClass}">${statutTexte}</span></td>
                <td>
                    <div class="actions-group">
                        <button class="btn-edit" onclick="chargerPourEdition(${JSON.stringify(cand).replace(/"/g, '&quot;')})" title="Modifier">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn-delete" onclick="supprimerCandidature(${cand.id})" title="Supprimer">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    </div>
                </td>
            `;
            tableBody.appendChild(ligne);
        });
    } catch (erreur) {
        console.error("Erreur lors de la récupération des candidatures :", erreur);
    }
}
// 3. FONCTION : Configure les écouteurs d'événements sur nos éléments HTML
function configurerEvenements() {
    const formulaire = document.getElementById("candidatureForm");
    const inputRecherche = document.getElementById("searchEntreprise");
    const dateRecherche = document.getElementById("searchDate");

    // Écouter la soumission du formulaire d'ajout
    if (formulaire) {
        formulaire.addEventListener("submit", ajouterCandidature);
    }

    // Écouter la saisie dans la barre de recherche (Recherche en temps réel par Nom)
    if (inputRecherche) {
        inputRecherche.addEventListener("input", (e) => {
            const valeur = e.target.value;
            if (valeur.trim() !== "") {
                chargerCandidatures(`${API_URL}/recherche/entreprise?nom=${valeur}`);
            } else {
                chargerCandidatures(); // Si on efface tout, on recharge la liste complète
            }
        });
    }

    // Écouter le changement de la date de recherche
    if (dateRecherche) {
        dateRecherche.addEventListener("change", (e) => {
            const date = e.target.value;
            if (date) {
                chargerCandidatures(`${API_URL}/recherche/date?date=${date}`);
            }
        });
    }
}

// 4. FONCTION EN SECOURS : Envoie une nouvelle candidature ou modifie une existante
async function ajouterCandidature(evenement) {
    evenement.preventDefault();

    const id = document.getElementById("candidatureId").value;
    const estEdition = id !== "";

    // 1. On crée le conteneur FormData
    const formData = new FormData();

    // 2. On prépare l'objet texte de notre candidature
    const donnéesCandidature = {
        nomEntreprise: document.getElementById("nomEntreprise").value,
        dateCandidature: document.getElementById("dateCandidature").value || null,
        lienOffre: document.getElementById("lienOffre").value,
        etat: document.getElementById("etat").value
    };

    // On transforme cet objet texte en un "Blob" JSON pour que Spring Boot l'intercepte dans @RequestPart("candidature")
    formData.append("candidature", new Blob([JSON.stringify(donnéesCandidature)], { type: "application/json" }));

    // 3. On récupère les fichiers physiques sélectionnés par l'utilisateur
    const lettreInput = document.getElementById("lettreFile");
    const cvInput = document.getElementById("cvFile");

    if (lettreInput && lettreInput.files[0]) {
        formData.append("lettreFile", lettreInput.files[0]);
    }

    if (cvInput && cvInput.files[0]) {
        formData.append("cvFile", cvInput.files[0]);
    }

    try {
        const url = estEdition ? `${API_URL}/${id}` : API_URL;
        const methode = estEdition ? "PUT" : "POST";

        const reponse = await fetch(url, {
            method: methode,
            body: formData
        });

        if (reponse.ok) {
            resetForm();
            chargerCandidatures();
        } else {
            alert(`Erreur lors de ${estEdition ? "la modification" : "l'enregistrement"} de la candidature.`);
        }
    } catch (erreur) {
        console.error("Erreur réseau :", erreur);
    }
}

// 4b. FONCTION : Remplit le formulaire pour modification
function chargerPourEdition(cand) {
    document.getElementById("formTitle").innerHTML = '<i class="fas fa-edit"></i> Modifier la candidature';
    document.getElementById("candidatureId").value = cand.id;
    document.getElementById("nomEntreprise").value = cand.nomEntreprise;
    document.getElementById("dateCandidature").value = cand.dateCandidature || "";
    document.getElementById("lienOffre").value = cand.lienOffre || "";
    document.getElementById("etat").value = cand.etat;

    document.getElementById("btnSubmit").innerText = "Mettre à jour";
    document.getElementById("btnAnnuler").style.display = "inline-block";

    // Scroll vers le formulaire
    document.querySelector(".form-section").scrollIntoView({ behavior: 'smooth' });
}

// 4c. FONCTION : Réinitialise le formulaire
function resetForm() {
    const form = document.getElementById("candidatureForm");
    if (form) form.reset();
    
    document.getElementById("formTitle").innerHTML = '<i class="fas fa-plus-circle"></i> Ajouter une candidature';
    document.getElementById("candidatureId").value = "";
    document.getElementById("btnSubmit").innerText = "Enregistrer la candidature";
    document.getElementById("btnAnnuler").style.display = "none";
}

// 5. FONCTION : Supprime une candidature (Requête DELETE)
async function supprimerCandidature(id) {
    if (confirm("Es-tu sûr de vouloir supprimer cette candidature ?")) {
        try {
            const reponse = await fetch(`${API_URL}/${id}`, {
                method: "DELETE"
            });

            if (reponse.ok) {
                chargerCandidatures(); // Rafraîchit le tableau après suppression
            } else {
                alert("Erreur lors de la suppression.");
            }
        } catch (erreur) {
            console.error("Erreur réseau lors de la suppression :", erreur);
        }
    }
}

// 6. FONCTION : Réinitialise les filtres de recherche
function reinitialiserRecherche() {
    document.getElementById("searchEntreprise").value = "";
    document.getElementById("searchDate").value = "";
    chargerCandidatures(); // Recharge la liste complète
}