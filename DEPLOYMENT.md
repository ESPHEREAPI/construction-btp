# Déploiement

Application conteneurisée (backend Spring Boot + frontend Angular servi par nginx), déployée aux côtés de la stack easycom existante sur le même serveur, sans y toucher.

- Port public app : **8082** (tout le trafic, frontend + `/api/*`, passe par le nginx du conteneur `frontend`).
- Port public phpMyAdmin dédié : **8083** (pointe sur le MySQL natif — voir plus bas, ne pas confondre avec le phpMyAdmin existant d'easycom sur 8090 qui pointe sur une autre base).
- Base de données : MySQL **natif** du serveur (port 3306, différent du MySQL dockerisé d'easycom sur 3307).

## Premier déploiement (une seule fois)

1. **Créer la base et l'utilisateur dédiés** sur le MySQL natif :
   ```bash
   mysql -u root -p < deploy/mysql-bootstrap.sql
   ```
   (éditez le mot de passe dans le fichier avant de l'exécuter, ou changez-le juste après avec `ALTER USER`).

2. **Cloner le repo** sur le serveur :
   ```bash
   git clone https://github.com/ESPHEREAPI/construction-btp.git
   cd construction-btp
   ```

3. **Créer le vrai `.env`** à partir du gabarit :
   ```bash
   cp .env.example .env
   ```
   Puis éditez `.env` et remplacez chaque `CHANGE_ME` par une vraie valeur :
   - `DB_PASSWORD` : celui choisi à l'étape 1.
   - `JWT_SECRET`, `LICENSE_SECRET` : deux valeurs aléatoires **distinctes**, ex. `openssl rand -base64 64` pour chacune. Ne jamais réutiliser les valeurs de dev.
   - `SUPERADMIN_INITIAL_PASSWORD` : mot de passe temporaire du premier compte Super Admin (changement forcé à la première connexion).
   - `CORS_ALLOWED_ORIGINS` : `http://169.58.128.44:8082` (ou l'IP réelle du serveur).

4. **Construire et démarrer** :
   ```bash
   docker compose up -d --build
   ```

5. **Vérifier** :
   ```bash
   docker compose ps
   curl http://localhost:8082
   curl http://localhost:8082/api/auth/login
   ```
   Puis se connecter depuis un navigateur sur `http://169.58.128.44:8082` avec `superadmin` / le mot de passe défini dans `.env`.

6. **Accès à la base via phpMyAdmin** : `http://169.58.128.44:8083`, se connecter avec `construction_btp` / le mot de passe défini à l'étape 1 (ou `root` / le mot de passe root du MySQL natif pour un accès complet). Ce phpMyAdmin est dédié à `construction_material_db` — distinct de celui d'easycom sur le port 8090.

## Mises à jour suivantes

Une fois `.github/workflows/deploy.yml` en place (secrets GitHub configurés — voir ce fichier), chaque push sur `main` redéploie automatiquement.

En manuel si besoin :
```bash
cd construction-btp
git pull
docker compose up -d --build
```

## Ne jamais faire

- Ne pas commiter `.env` (il est dans `.gitignore`).
- Ne pas réutiliser les secrets de `application-local.properties` (dev) en production.
- Ne pas exposer le backend directement sur un port de l'hôte — tout passe par le proxy nginx du conteneur `frontend`.
