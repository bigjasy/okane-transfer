# RECETTES DE TEST — OkaneTransfer

**Version:** 1.0  
**Date:** Juin 2026  
**Application:** OkaneTransfer — Plateforme de transfert d'argent  
**Type de document:** Cahier de recettes / Scénarios de test fonctionnels  

---

## 1. Objet du document

Ce document décrit les scénarios de test à exécuter par l'équipe pour valider l'ensemble des fonctionnalités d'OkaneTransfer (frontend Angular + API Java + PostgreSQL).

Chaque recette indique :
- **Qui** teste (rôle)
- **Quoi** saisir (données d'entrée)
- **Quoi** observer (résultat attendu à l'écran ou en réponse API)

---

## 2. Environnements de test

### 2.1 Production Render (déploiement équipe)

| Composant | URL |
|-----------|-----|
| Application web | https://okane-frontend.onrender.com |
| API REST | https://okane-api.onrender.com/api/v1 |
| Swagger (API) | https://okane-api.onrender.com/swagger-ui/index.html |

**Note :** Sur le plan gratuit Render, la première requête après inactivité peut prendre ~1 minute (démarrage à froid).

### 2.2 Environnement local (optionnel)

| Composant | URL |
|-----------|-----|
| Frontend | http://localhost:4300 |
| API | http://localhost:8080/okane_transfer_war/api/v1 |

---

## 3. Comptes de test par défaut

**Mot de passe pour tous les comptes :** `Password@123`

| Rôle | Email | Redirection après login |
|------|-------|-------------------------|
| Administrateur | admin@okane.ma | /admin/dashboard |
| Manager agence | manager@okane.ma | /manager/dashboard |
| Agent | agent@okane.ma | /agent/dashboard |
| Client | client@okane.ma | /client/dashboard |

**Agence de test liée à agent/manager :** Agence Casablanca Centre (AGY001)

---

## 4. Données de référence (seed base)

| Élément | Valeur exemple |
|---------|----------------|
| Pays actifs | MA, SN, CI, FR, US, GB, CM |
| Devises | MAD, EUR, USD, XOF, XOF, GBP, XAF |
| Corridor | MA -> SN, MA -> FR, FR -> MA |
| Seuil AML | 10 000 (devise source) |
| Agence AGY001 | Casablanca, plafond journalier 500 000 MAD |

---

## 5. Légende des statuts de test

| Statut | Signification |
|--------|---------------|
| OK | Test réussi |
| KO | Test échoué — noter capture + message erreur |
| N/A | Non applicable dans cet environnement |
| BLOQUÉ | Impossible à tester (dépendance manquante) |

---

# MODULE A — AUTHENTIFICATION & SÉCURITÉ

## TC-A01 — Connexion administrateur réussie

| Champ | Valeur |
|-------|--------|
| **Rôle** | Tous |
| **Prérequis** | Compte admin actif |
| **URL** | /auth/login |

**Saisir :**
- Email : `admin@okane.ma`
- Mot de passe : `Password@123`

**Résultat attendu :**
- Redirection vers `/admin/dashboard`
- Menu latéral visible : Utilisateurs, Agences, Pays, Devises, Taux, Corridors, Grilles, Conformité, Rapports, Audit
- Nom utilisateur affiché dans la barre supérieure
- Aucune erreur dans la console navigateur (F12)

---

## TC-A02 — Connexion agent réussie

**Saisir :** `agent@okane.ma` / `Password@123`

**Résultat attendu :**
- Redirection `/agent/dashboard`
- Menu : Transferts, Créer transfert, Simulation frais, Payout, Caisse, Mobile Money

---

## TC-A03 — Connexion client réussie

**Saisir :** `client@okane.ma` / `Password@123`

**Résultat attendu :**
- Redirection `/client/dashboard`
- Menu : Profil, Bénéficiaires, Suivi transfert, Chatbot, Notifications

---

## TC-A04 — Échec connexion mot de passe incorrect

**Saisir :** `admin@okane.ma` / `WrongPassword`

**Résultat attendu :**
- Message d'erreur visible (identifiants invalides)
- Pas de redirection dashboard
- Pas de token stocké (rester sur page login)

---

## TC-A05 — Échec connexion email inconnu

**Saisir :** `inconnu@test.ma` / `Password@123`

**Résultat attendu :**
- Message d'erreur authentification
- Pas d'accès aux pages sécurisées

---

## TC-A06 — Inscription nouveau client

| Champ | Valeur |
|-------|--------|
| **URL** | /auth/register |

**Saisir (exemple) :**
- Prénom : `Test`
- Nom : `Recette`
- Email : `test.recette.2026@example.com`
- Téléphone : `+212612345678`
- Mot de passe : `Password@123`
- Confirmation : `Password@123`

**Résultat attendu :**
- Message succès ou redirection login
- Connexion possible avec le nouveau compte
- Rôle CLIENT attribué
- KYC : statut `NOT_SUBMITTED`

---

## TC-A07 — Activation 2FA (OTP email)

| Champ | Valeur |
|-------|--------|
| **Rôle** | Client ou Admin |
| **Prérequis** | Email SMTP configuré sur le serveur |

**Étapes :**
1. Se connecter
2. Aller dans Profil (client) ou paramètres OTP
3. Activer la double authentification (canal EMAIL)

**Résultat attendu :**
- OTP reçu par email OU message indiquant envoi
- 2FA activée en base
- Déconnexion puis reconnexion demande page `/auth/verify-otp`

---

## TC-A08 — Vérification OTP après login 2FA

**Prérequis :** TC-A07 réussi

**Saisir :** Code OTP reçu par email

**Résultat attendu :**
- Accès application débloqué
- Token JWT retourné
- Redirection selon rôle

---

## TC-A09 — Accès page protégée sans login

**Étapes :** Ouvrir directement `https://okane-frontend.onrender.com/admin/users` sans être connecté

**Résultat attendu :**
- Redirection vers `/auth/login`

---

## TC-A10 — Accès refusé rôle incorrect

**Étapes :**
1. Se connecter en `client@okane.ma`
2. Tenter d'ouvrir `/admin/dashboard`

**Résultat attendu :**
- Accès refusé ou redirection (guard rôle)
- Pas d'affichage données admin

---

## TC-A11 — Déconnexion

**Étapes :** Cliquer Déconnexion dans le menu

**Résultat attendu :**
- Retour page login
- Token supprimé
- Navigation arrière ne restaure pas la session

---

# MODULE B — ADMINISTRATEUR

## TC-B01 — Dashboard admin KPIs

| Champ | Valeur |
|-------|--------|
| **Rôle** | Admin |
| **URL** | /admin/dashboard |

**Résultat attendu :**
- Cartes KPI visibles (transferts, volumes, alertes, etc.)
- Graphiques Chart.js chargés sans erreur
- Données numériques affichées (même à 0)

---

## TC-B02 — Liste utilisateurs

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/users |

**Résultat attendu :**
- Tableau avec admin, manager, agent, client
- Colonnes : email, nom, rôle, statut
- Filtre/recherche fonctionnel si présent

---

## TC-B03 — Création utilisateur agent

**Saisir :**
- Email : `nouvel.agent@test.ma`
- Prénom : `Nouvel`
- Nom : `Agent`
- Rôle : AGENT
- Mot de passe : `Password@123`

**Résultat attendu :**
- Utilisateur créé et visible dans la liste
- Connexion possible avec ce compte (après liaison agence si requis)

---

## TC-B04 — Désactivation utilisateur

**Étapes :**
1. Choisir un utilisateur test
2. Passer statut à INACTIVE / SUSPENDED

**Résultat attendu :**
- Statut mis à jour dans la liste
- Login de cet utilisateur refusé ensuite

---

## TC-B05 — Liste agences

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/agencies |

**Résultat attendu :**
- AGY001 Casablanca, AGY002 Rabat, etc.
- Statut ACTIVE visible
- Plafonds journaliers affichés

---

## TC-B06 — Création agence

**Saisir :**
- Code : `AGY-TEST-01`
- Nom : `Agence Test Recette`
- Ville : `Marrakech`
- Pays : Maroc
- Plafond journalier : `100000`

**Résultat attendu :**
- Agence créée dans la liste
- Code unique (erreur si doublon)

---

## TC-B07 — Affectation agent à agence

**Étapes :** Depuis détail agence AGY001, affecter un agent

**Résultat attendu :**
- Agent listé dans le staff de l'agence
- Agent voit données de cette agence

---

## TC-B08 — Gestion pays — liste

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/countries |

**Résultat attendu :**
- Maroc, Sénégal, Côte d'Ivoire, France visibles
- Code ISO et devise associée

---

## TC-B09 — Création pays

**Saisir :**
- ISO : `TN`
- Nom : `Tunisie`
- Indicatif : `+216`
- Devise : TND (créer devise avant si absente)

**Résultat attendu :**
- Pays ajouté à la liste
- Disponible dans corridors

---

## TC-B10 — Activation / désactivation pays

**Étapes :** Désactiver puis réactiver un pays test

**Résultat attendu :**
- Statut mis à jour
- Pays inactif non proposé dans nouveaux corridors

---

## TC-B11 — Gestion devises

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/currencies |

**Résultat attendu :**
- MAD, EUR, USD, XOF listés
- Création devise : code `TND`, nom `Dinar tunisien`, décimales 3

---

## TC-B12 — Taux de change — consultation

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/exchange-rates |

**Résultat attendu :**
- Taux MAD/XOF, EUR/MAD, etc.
- Source MANUAL ou API visible

---

## TC-B13 — Synchronisation taux externes (simulé)

**Étapes :** Cliquer synchroniser taux externes

**Résultat attendu :**
- Message succès
- Nouveaux taux ou historique mis à jour
- Provider : OkaneFX-Simulated

---

## TC-B14 — Convertisseur devises

**Saisir :**
- Montant : `1000`
- De : MAD
- Vers : EUR

**Résultat attendu :**
- Montant converti affiché selon taux actif
- Calcul cohérent avec taux listés

---

## TC-B15 — Gestion corridors

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/corridors |

**Résultat attendu :**
- Corridors MA->SN, MA->FR visibles
- Plafonds journalier/mensuel affichés

---

## TC-B16 — Création corridor

**Saisir :**
- Pays source : Maroc
- Pays destination : Cameroun
- Plafond journalier : `50000`
- Plafond mensuel : `500000`
- Actif : oui

**Résultat attendu :**
- Corridor créé
- Utilisable dans grilles de frais

---

## TC-B17 — Grilles de frais — liste

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/fee-grids |

**Résultat attendu :**
- Grilles par corridor et tranches montant
- Frais fixe + pourcentage visibles

---

## TC-B18 — Création grille de frais

**Saisir :**
- Corridor : MA -> SN
- Min : `0`, Max : `3000` MAD
- Frais fixe : `25`
- Pourcentage : `2.5%`
- Commission agence : `35%`, centrale : `65%`

**Résultat attendu :**
- Grille enregistrée
- Utilisée dans simulation agent

---

## TC-B19 — Simulation frais admin

**Saisir :**
- Montant : `2000` MAD
- Corridor : MA -> SN

**Résultat attendu :**
- Frais calculés affichés
- Montant net / total cohérent

---

## TC-B20 — Export grilles CSV

**Étapes :** Exporter grilles de frais

**Résultat attendu :**
- Fichier CSV téléchargé
- Colonnes lisibles dans Excel

---

## TC-B21 — Conformité — résumé KYC/AML

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/compliance |

**Résultat attendu :**
- Compteurs documents KYC en attente
- Alertes AML ouvertes
- Watchlist accessible

---

## TC-B22 — Revue document KYC

**Prérequis :** Client a uploadé une pièce d'identité (TC-E03)

**Étapes :**
1. Ouvrir document en attente
2. Approuver ou rejeter avec commentaire

**Résultat attendu :**
- Statut KYC client mis à jour (APPROVED / REJECTED)
- Notification client si configurée

---

## TC-B23 — Alerte AML — revue

**Prérequis :** Transfert > seuil 10 000 ou match watchlist

**Étapes :** Ouvrir alerte, marquer REVIEWED / CLEARED / ESCALATED

**Résultat attendu :**
- Statut alerte mis à jour
- Historique visible

---

## TC-B24 — Watchlist OFAC — ajout entrée

**Saisir :**
- Nom : `TEST WATCHLIST`
- Type : PERSON
- Pays : MA
- Actif : oui

**Résultat attendu :**
- Entrée dans la liste
- Prochain transfert avec ce nom déclenche alerte

---

## TC-B25 — Rapports admin — transferts

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/reports |

**Saisir filtres :**
- Période : mois en cours
- Format : PDF ou CSV

**Résultat attendu :**
- Rapport généré et téléchargeable
- PDF lisible avec en-tête OkaneTransfer

---

## TC-B26 — Rapports commissions

**Étapes :** Générer rapport commissions sur période

**Résultat attendu :**
- Totaux agence / centrale
- Export PDF ou CSV OK

---

## TC-B27 — Rapports par agence

**Étapes :** Sélectionner AGY001, générer rapport

**Résultat attendu :**
- Données filtrées agence Casablanca
- KPI cohérents avec transferts créés

---

## TC-B28 — Journal d'audit global

| Champ | Valeur |
|-------|--------|
| **URL** | /admin/audit |

**Résultat attendu :**
- Logs : LOGIN, TRANSFER_CREATE, KYC_REVIEW, etc.
- Filtres par utilisateur, action, date
- Horodatage correct

---

# MODULE C — MANAGER

## TC-C01 — Dashboard manager

| Champ | Valeur |
|-------|--------|
| **Rôle** | manager@okane.ma |
| **URL** | /manager/dashboard |

**Résultat attendu :**
- KPI agence du manager (AGY001)
- Pas de données autres agences

---

## TC-C02 — Liste agents de l'agence

| Champ | Valeur |
|-------|--------|
| **URL** | /manager/agents |

**Résultat attendu :**
- agent@okane.ma visible
- Informations agence uniquement

---

## TC-C03 — Suivi caisses agence

| Champ | Valeur |
|-------|--------|
| **URL** | /manager/cash-registers |

**Résultat attendu :**
- Caisses ouvertes/fermées de l'agence
- Soldes affichés

---

## TC-C04 — Rapports manager

| Champ | Valeur |
|-------|--------|
| **URL** | /manager/reports |

**Résultat attendu :**
- Rapports limités à l'agence du manager
- Export fonctionnel

---

# MODULE D — AGENT (OPÉRATIONS)

## TC-D01 — Dashboard agent

| Champ | Valeur |
|-------|--------|
| **Rôle** | agent@okane.ma |
| **URL** | /agent/dashboard |

**Résultat attendu :**
- Résumé opérationnel : transferts du jour, caisse
- Liens rapides vers création transfert

---

## TC-D02 — Simulation frais agent

| Champ | Valeur |
|-------|--------|
| **URL** | /agent/fee-simulation |

**Saisir :**
- Montant : `1500`
- Devise source : MAD
- Devise cible : XOF
- Pays destination : Sénégal

**Résultat attendu :**
- Frais, taux, montant reçu estimé
- Corridor détecté automatiquement (MA->SN)
- Pas d'erreur si grille existe

---

## TC-D03 — Création transfert — flux complet

| Champ | Valeur |
|-------|--------|
| **URL** | /agent/create-transfer |

**Saisir (exemple) :**
- Expéditeur : client existant ou walk-in
  - Nom : `Ahmed Benali`
  - Téléphone : `+212612000111`
  - Pièce : `AB123456`
- Bénéficiaire :
  - Nom : `Moussa Diop`
  - Téléphone : `+221771234567`
  - Pays : Sénégal
- Montant : `3000` MAD
- Mode : CASH
- Agence destination : Dakar si demandé

**Résultat attendu :**
- Référence transfert générée (ex: OK-XXXXXXXX)
- Statut initial : PENDING_PAYMENT ou équivalent
- Frais et montant net affichés
- Récapitulatif avant validation

---

## TC-D04 — Confirmation paiement transfert

**Prérequis :** TC-D03 créé

**Étapes :** Depuis liste transferts, confirmer encaissement

**Résultat attendu :**
- Statut passe à PAID / IN_TRANSIT
- Mouvement caisse ENCAISSEMENT enregistré
- Commission calculée

---

## TC-D05 — Liste transferts agent

| Champ | Valeur |
|-------|--------|
| **URL** | /agent/transfers |

**Résultat attendu :**
- Transferts de l'agence listés
- Filtres statut / date
- Actions : voir détail, confirmer, annuler, reçu

---

## TC-D06 — Téléchargement reçu transfert PDF

**Étapes :** Ouvrir transfert payé, télécharger reçu

**Résultat attendu :**
- PDF téléchargé
- Référence, montants, bénéficiaire sur le document

---

## TC-D07 — Annulation transfert

**Prérequis :** Transfert annulable (non payé ou règle métier)

**Étapes :** Annuler avec motif

**Résultat attendu :**
- Statut CANCELLED
- Caisse ajustée si applicable

---

## TC-D08 — Transfert déclenchant alerte AML

**Saisir :** Montant `15000` MAD ( > seuil 10000 )

**Résultat attendu :**
- Transfert créé ou bloqué selon règle
- Alerte AML visible côté admin conformité
- Message agent si blocage

---

## TC-D09 — Dépassement plafond agence

**Saisir :** Montant très élevé proche plafond journalier agence

**Résultat attendu :**
- Message erreur plafond journalier agence dépassé
- Transfert refusé

---

## TC-D10 — Ouverture caisse

| Champ | Valeur |
|-------|--------|
| **URL** | /agent/cash-register |

**Saisir :**
- Solde initial : `5000` MAD

**Résultat attendu :**
- Caisse ouverte
- Statut OPEN
- Solde initial enregistré

---

## TC-D11 — Mouvement caisse manuel

**Saisir :**
- Type : APPROVISIONNEMENT ou RETRAIT
- Montant : `500`
- Motif : `Test recette caisse`

**Résultat attendu :**
- Mouvement dans l'historique
- Solde mis à jour

---

## TC-D12 — Fermeture caisse

**Étapes :** Fermer caisse ouverte, saisir solde compté

**Résultat attendu :**
- Caisse CLOSED
- Écart éventuel signalé
- Rapport clôture si disponible

---

## TC-D13 — Payout — recherche transfert

| Champ | Valeur |
|-------|--------|
| **URL** | /agent/payout |

**Saisir :**
- Référence du transfert payé TC-D04
- OU nom bénéficiaire + téléphone

**Résultat attendu :**
- Transfert trouvé
- Montant et statut READY_FOR_PAYOUT ou équivalent

---

## TC-D14 — Payout — validation identité bénéficiaire

**Saisir :**
- Type pièce : CNI
- Numéro : même que création transfert

**Résultat attendu :**
- Validation OK
- Étape confirmation débloquée

---

## TC-D15 — Payout — confirmation remise fonds

**Étapes :** Confirmer remise au bénéficiaire

**Résultat attendu :**
- Statut COMPLETED / DELIVERED
- Mouvement caisse DECAISSEMENT
- Reçu payout téléchargeable

---

## TC-D16 — Payout — identité incorrecte

**Saisir :** Numéro pièce erroné

**Résultat attendu :**
- Validation refusée
- Message erreur clair
- Pas de décaissement

---

# MODULE E — CLIENT

## TC-E01 — Dashboard client

| Champ | Valeur |
|-------|--------|
| **Rôle** | client@okane.ma |
| **URL** | /client/dashboard |

**Résultat attendu :**
- Résumé profil et transferts récents
- Lien vers bénéficiaires et suivi

---

## TC-E02 — Mise à jour profil

| Champ | Valeur |
|-------|--------|
| **URL** | /client/profile |

**Saisir :**
- Téléphone : `+212612999888`
- Langue : FR / EN / AR

**Résultat attendu :**
- Profil sauvegardé
- Langue interface change si sélectionnée

---

## TC-E03 — Upload document KYC

**Étapes :**
1. Profil client -> section KYC
2. Uploader fichier JPG ou PDF (< 5 Mo)
3. Type : CNI ou PASSEPORT

**Résultat attendu :**
- Document listé statut PENDING
- Visible côté admin conformité (TC-B22)

---

## TC-E04 — Création bénéficiaire

| Champ | Valeur |
|-------|--------|
| **URL** | /client/beneficiaries |

**Saisir :**
- Prénom : `Fatou`
- Nom : `Sow`
- Téléphone : `+221701112233`
- Pays : Sénégal
- Relation : Famille

**Résultat attendu :**
- Bénéficiaire dans la liste
- Modifiable et supprimable

---

## TC-E05 — Modification bénéficiaire

**Étapes :** Modifier téléphone bénéficiaire

**Résultat attendu :**
- Données mises à jour
- Pas de doublon incohérent

---

## TC-E06 — Suppression bénéficiaire

**Étapes :** Supprimer bénéficiaire test

**Résultat attendu :**
- Disparu de la liste
- Non proposé dans nouveaux transferts

---

## TC-E07 — Suivi transfert par référence

| Champ | Valeur |
|-------|--------|
| **URL** | /client/transfer-tracking |

**Saisir :** Référence transfert créé en TC-D03

**Résultat attendu :**
- Statut actuel affiché
- Historique statuts / timeline
- Montants et bénéficiaire (masqués partiellement si règle confidentialité)

---

## TC-E08 — Suivi référence invalide

**Saisir :** `FAKE-REF-000`

**Résultat attendu :**
- Message transfert introuvable
- Pas d'erreur technique brute

---

# MODULE F — MOBILE MONEY

## TC-F01 — Accès module (rôles autorisés)

| Champ | Valeur |
|-------|--------|
| **URL** | /mobile-money |

**Tester avec :** admin, manager, agent -> OK  
**Tester avec :** client -> accès refusé

---

## TC-F02 — Envoi Mobile Money

**Saisir :**
- Opérateur : ORANGE_MONEY ou WAVE (selon liste)
- Téléphone : `+221771234567`
- Montant : `5000` XOF
- Référence externe : `MM-TEST-001`

**Résultat attendu :**
- Transaction créée statut PENDING
- ID transaction affiché

---

## TC-F03 — Liste transactions Mobile Money

**Résultat attendu :**
- Historique avec statuts
- Filtres date / statut

---

## TC-F04 — Simulation callback opérateur

**Étapes :** Sur transaction PENDING, simuler callback SUCCESS

**Résultat attendu :**
- Statut SUCCESS / COMPLETED
- Horodatage callback

---

## TC-F05 — Réconciliation Mobile Money

**Étapes :** Lancer réconciliation sur période

**Résultat attendu :**
- Nombre transactions réconciliées
- Écarts signalés si mismatch

---

# MODULE G — CHATBOT

## TC-G01 — Question FAQ transfert

| Champ | Valeur |
|-------|--------|
| **URL** | /chatbot |
| **Prérequis** | OPENROUTER_API_KEY configurée (sinon message fallback) |

**Saisir :** `Quels sont les frais pour un transfert vers le Sénégal ?`

**Résultat attendu :**
- Réponse texte du chatbot
- Pas d'erreur 500

---

## TC-G02 — Chatbot sans clé API

**Prérequis :** Clé OpenRouter absente

**Résultat attendu :**
- Message : configuration manquante ou service indisponible
- Application reste stable

---

## TC-G03 — Changement langue chatbot

**Étapes :** Poser question en sélectionnant langue AR ou EN

**Résultat attendu :**
- Réponse dans la langue demandée (selon modèle IA)

---

# MODULE H — NOTIFICATIONS

## TC-H01 — Liste notifications

| Champ | Valeur |
|-------|--------|
| **URL** | /notifications |

**Résultat attendu :**
- Boîte notifications utilisateur
- Compteur non-lus si applicable

---

## TC-H02 — Marquer notification lue

**Étapes :** Cliquer notification non lue

**Résultat attendu :**
- Statut READ
- Compteur décrémenté

---

## TC-H03 — Préférences notifications

**Étapes :** Activer/désactiver email et SMS

**Résultat attendu :**
- Préférences sauvegardées
- Reflétées au prochain envoi

---

## TC-H04 — Notification après transfert

**Prérequis :** Client avec email activé

**Étapes :** Créer et payer transfert pour ce client

**Résultat attendu :**
- Notification in-app créée
- Email si NOTIFICATION_EMAIL_ENABLED=true

---

# MODULE I — INTERNATIONALISATION (i18n)

## TC-I01 — Interface française

**Étapes :** Langue FR dans profil

**Résultat attendu :**
- Menus et libellés en français

---

## TC-I02 — Interface anglaise

**Étapes :** Passer langue EN

**Résultat attendu :**
- Menus traduits en anglais

---

## TC-I03 — Interface arabe

**Étapes :** Passer langue AR

**Résultat attendu :**
- Textes en arabe
- Mise en page RTL si configurée

---

# MODULE J — TESTS API (Swagger / optionnel)

## TC-J01 — Health API OpenAPI

**URL :** `/v3/api-docs`

**Résultat attendu :**
- JSON OpenAPI valide
- Endpoints /api/v1/* listés

---

## TC-J02 — Login via Swagger

**Endpoint :** POST `/api/v1/auth/login`

**Body :**
```json
{"email":"admin@okane.ma","password":"Password@123"}
```

**Résultat attendu :**
- 200 + accessToken + refreshToken

---

## TC-J03 — Appel protégé sans token

**Endpoint :** GET `/api/v1/users`

**Résultat attendu :**
- 401 Unauthorized

---

## TC-J04 — Appel protégé avec token

**Étapes :** Authorize Bearer token puis GET `/api/v1/users`

**Résultat attendu :**
- 200 + liste utilisateurs

---

# MODULE K — TESTS NON FONCTIONNELS

## TC-K01 — Temps réponse page dashboard

**Résultat attendu :**
- Chargement < 5 secondes (hors cold start Render)

---

## TC-K02 — Responsive mobile

**Étapes :** Réduire fenêtre navigateur ou mode mobile F12

**Résultat attendu :**
- Menu et tableaux utilisables
- Pas de chevauchement bloquant

---

## TC-K03 — CORS frontend/API

**Prérequis :** CORS_ALLOWED_ORIGINS configuré sur API

**Résultat attendu :**
- Login depuis frontend Render sans erreur CORS console

---

# ANNEXE — MATRICE RÉCAPITULATIVE

| Module | Nb recettes | Rôles principaux |
|--------|-------------|------------------|
| A Auth | 11 | Tous |
| B Admin | 28 | Admin |
| C Manager | 4 | Manager |
| D Agent | 16 | Agent |
| E Client | 8 | Client |
| F Mobile Money | 5 | Admin, Manager, Agent |
| G Chatbot | 3 | Tous |
| H Notifications | 4 | Tous |
| I i18n | 3 | Tous |
| J API | 4 | Testeur technique |
| K Non fonctionnel | 3 | Tous |
| **TOTAL** | **89** | |

---

# ANNEXE — ORDRE DE TEST RECOMMANDÉ (parcours bout-en-bout)

1. TC-A01 Connexion admin  
2. TC-B08 à TC-B18 Référentiel (pays, devises, corridors, grilles)  
3. TC-A03 Connexion client  
4. TC-E03 Upload KYC  
5. TC-B22 Approbation KYC admin  
6. TC-E04 Création bénéficiaire  
7. TC-A02 Connexion agent  
8. TC-D10 Ouverture caisse  
9. TC-D02 Simulation frais  
10. TC-D03 Création transfert  
11. TC-D04 Confirmation paiement  
12. TC-E07 Suivi client  
13. TC-D13 à TC-D15 Payout complet  
14. TC-D12 Fermeture caisse  
15. TC-B25 Rapport admin  
16. TC-B28 Vérification audit  
17. TC-F02 Mobile Money  
18. TC-G01 Chatbot  
19. TC-H01 Notifications  

---

# FICHE DE COMPTAGE RENDU (à remplir par le testeur)

| Testeur | Date | Environnement | OK | KO | BLOQUÉ | Commentaires |
|---------|------|---------------|----|----|--------|--------------|
| | | Render / Local | | | | |

---

**Fin du document — OkaneTransfer Recettes de Test v1.0**
