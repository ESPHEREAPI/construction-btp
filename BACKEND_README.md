# 🏗️ CONSTRUCTION MATERIAL MANAGEMENT SYSTEM - BACKEND

## 📋 Table des Matières
1. [Prérequis](#prérequis)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Lancement](#lancement)
5. [Structure du Projet](#structure-du-projet)
6. [API Endpoints](#api-endpoints)
7. [Tests](#tests)
8. [Comptes de Démonstration](#comptes-de-démonstration)

---

## 🔧 Prérequis

### Logiciels Requis
- **Java JDK 17 ou 21** (recommandé: OpenJDK 17)
- **Maven 3.8+**
- **MySQL 8.0+**
- **Git**
- **IDE** (recommandé: IntelliJ IDEA ou Eclipse)

### Vérification des Versions
```bash
java -version      # Doit afficher Java 17 ou 21
mvn -version       # Maven 3.8+
mysql --version    # MySQL 8.0+
```

---

## 📦 Installation

### 1. Cloner le Projet
```bash
cd /votre/repertoire/de/travail
# Le projet est déjà dans /home/claude/construction-material-management
```

### 2. Configuration MySQL

#### Démarrer MySQL
```bash
# Linux/Mac
sudo service mysql start

# Windows
net start MySQL80
```

#### Créer la Base de Données
```bash
# Se connecter à MySQL
mysql -u root -p

# Dans le prompt MySQL, exécuter:
CREATE DATABASE construction_material_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

#### Initialiser avec les Données de Démonstration
```bash
cd /home/claude/construction-material-management
mysql -u root -p construction_material_db < database/init_database.sql
```

### 3. Configuration du Backend

#### Éditer application.properties
```bash
cd backend/src/main/resources
nano application.properties
```

#### Modifier les Paramètres de Connexion MySQL
```properties
# Adapter selon votre configuration
spring.datasource.url=jdbc:mysql://localhost:3306/construction_material_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=VOTRE_MOT_DE_PASSE_MYSQL

# Clé JWT (CHANGER EN PRODUCTION!)
jwt.secret=VotreCléSecrèteTrèsLongueEtSécuriséePourJWTMinimum512BitsLong
```

### 4. Compiler le Projet
```bash
cd /home/claude/construction-material-management/backend
mvn clean install -DskipTests
```

Si des erreurs de compilation apparaissent, exécuter:
```bash
mvn clean install -U -DskipTests
```

---

## 🚀 Lancement

### Méthode 1: Maven (Développement)
```bash
cd /home/claude/construction-material-management/backend
mvn spring-boot:run
```

### Méthode 2: JAR Exécutable (Production)
```bash
cd /home/claude/construction-material-management/backend
mvn clean package -DskipTests
java -jar target/material-management-1.0.0.jar
```

### Méthode 3: Depuis l'IDE
1. Ouvrir le projet dans IntelliJ/Eclipse
2. Localiser `MaterialManagementApplication.java`
3. Clic droit → Run

### Vérification du Démarrage
Le serveur démarre sur **http://localhost:8080**

Logs de succès:
```
Started MaterialManagementApplication in X.XXX seconds
```

---

## 📚 API Documentation

### Swagger UI
Une fois le serveur démarré, accéder à:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/api-docs

---

## 🔑 Comptes de Démonstration

### Identifiants par Défaut
**Mot de passe pour TOUS les comptes: `password123`**

| Username | Email | Rôle | Description |
|----------|-------|------|-------------|
| `admin` | admin@construction.com | ADMIN | Accès complet |
| `jdupont` | j.dupont@construction.com | PROJECT_MANAGER | Chef de Projet |
| `mngono` | m.ngono@construction.com | SITE_MANAGER | Chef de Chantier |
| `asilva` | a.silva@construction.com | INVENTORY_MANAGER | Gestionnaire Stock |
| `jsmith` | j.smith@construction.com | READ_ONLY | Lecture Seule |

### Test d'Authentification
```bash
# Endpoint de login
POST http://localhost:8080/api/auth/login

# Body (JSON)
{
  "username": "admin",
  "password": "password123"
}

# Réponse attendue
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@construction.com",
  "roles": ["ROLE_ADMIN"],
  "preferredLanguage": "fr"
}
```

---

## 📁 Structure du Projet

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/construction/material/
│   │   │   ├── config/              # Configurations (CORS, i18n, Security)
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── request/         # DTOs de requête
│   │   │   │   └── response/        # DTOs de réponse
│   │   │   ├── entity/              # Entités JPA
│   │   │   ├── exception/           # Gestion des exceptions
│   │   │   ├── repository/          # Repositories JPA
│   │   │   ├── security/            # Sécurité JWT
│   │   │   │   └── jwt/            # Utilitaires JWT
│   │   │   ├── service/             # Interfaces de services
│   │   │   │   └── impl/           # Implémentations
│   │   │   └── MaterialManagementApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── i18n/                # Fichiers de traduction
│   │           ├── messages_fr.properties
│   │           ├── messages_en.properties
│   │           └── messages_pt.properties
│   └── test/                        # Tests unitaires
├── pom.xml                          # Configuration Maven
└── README.md
```

---

## 🔗 Principaux Endpoints API

### 🔐 Authentification
- `POST /api/auth/login` - Connexion utilisateur
- `POST /api/auth/register` - Inscription utilisateur

### 👥 Utilisateurs
- `GET /api/users` - Liste des utilisateurs
- `GET /api/users/{id}` - Détails utilisateur
- `POST /api/users` - Créer utilisateur
- `PUT /api/users/{id}` - Modifier utilisateur
- `DELETE /api/users/{id}` - Supprimer utilisateur

### 🏗️ Projets
- `GET /api/projects` - Liste des projets
- `GET /api/projects/{id}` - Détails projet
- `POST /api/projects` - Créer projet
- `PUT /api/projects/{id}` - Modifier projet
- `DELETE /api/projects/{id}` - Supprimer projet
- `GET /api/projects/status/{status}` - Projets par statut

### 🧱 Matériaux
- `GET /api/materials` - Liste des matériaux
- `GET /api/materials/{id}` - Détails matériau
- `POST /api/materials` - Créer matériau
- `PUT /api/materials/{id}` - Modifier matériau
- `DELETE /api/materials/{id}` - Supprimer matériau

### 📊 Quantifications
- `GET /api/quantifications/project/{projectId}` - Quantifications par projet
- `POST /api/quantifications` - Créer quantification
- `PUT /api/quantifications/{id}` - Modifier quantification

### 📦 Commandes
- `GET /api/orders` - Liste des commandes
- `GET /api/orders/{id}` - Détails commande
- `POST /api/orders` - Créer commande
- `PUT /api/orders/{id}` - Modifier commande
- `PUT /api/orders/{id}/status` - Changer statut

### 📈 Utilisation
- `GET /api/usages/project/{projectId}` - Utilisations par projet
- `POST /api/usages` - Enregistrer utilisation
- `GET /api/usages/comparison/{projectId}` - Comparaison vs quantification

### 📦 Stock
- `GET /api/stocks/project/{projectId}` - Stock par projet
- `POST /api/stocks/movement` - Enregistrer mouvement
- `GET /api/stocks/alerts` - Alertes de stock bas

### 🔔 Alertes
- `GET /api/alerts` - Liste des alertes
- `PUT /api/alerts/{id}/acknowledge` - Accuser réception

### 📊 Dashboard
- `GET /api/dashboard/stats` - Statistiques globales

---

## 🌐 Internationalisation

Le système supporte 3 langues:
- **Français (fr)** - Par défaut
- **English (en)**
- **Português (pt)**

### Changer la Langue
```bash
# Via paramètre
GET /api/projects?lang=en

# Via header
Accept-Language: pt
```

---

## 🧪 Tests

### Exécuter les Tests
```bash
mvn test
```

### Tests d'Intégration
```bash
mvn verify
```

### Couverture de Code
```bash
mvn clean test jacoco:report
# Rapport dans: target/site/jacoco/index.html
```

---

## ⚙️ Configuration Avancée

### Changer le Port
```properties
# application.properties
server.port=9090
```

### Activer le Mode Debug
```properties
logging.level.com.construction=DEBUG
```

### Configuration JWT
```properties
# Durée de validité du token (en ms)
jwt.expiration=86400000  # 24 heures

# Clé secrète (minimum 512 bits pour HS512)
jwt.secret=VotreCléSecrète...
```

---

## 🐛 Résolution des Problèmes

### Erreur de Connexion MySQL
```
Error: Access denied for user 'root'@'localhost'
```
**Solution**: Vérifier username/password dans `application.properties`

### Port Déjà Utilisé
```
Port 8080 is already in use
```
**Solution**: 
```bash
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Erreur de Compilation MapStruct
```bash
mvn clean install -U
```

### Problème de Droits MySQL
```sql
GRANT ALL PRIVILEGES ON construction_material_db.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

---

## 📞 Support

- **Documentation**: Swagger UI (http://localhost:8080/swagger-ui.html)
- **Logs**: `logs/application.log`
- **Base de données**: Scripts dans `database/`

---

## ✅ Checklist de Démarrage

- [ ] Java 17+ installé
- [ ] Maven 3.8+ installé
- [ ] MySQL 8.0+ installé et démarré
- [ ] Base de données créée
- [ ] Script SQL exécuté
- [ ] `application.properties` configuré
- [ ] Compilation réussie (`mvn clean install`)
- [ ] Serveur démarré
- [ ] Swagger UI accessible
- [ ] Test de login réussi

---

## 🎯 Prochaines Étapes

Une fois le backend fonctionnel:
1. Tester tous les endpoints via Swagger
2. Vérifier les données de démonstration
3. Passer au développement du Frontend Angular

---

**Version**: 1.0.0  
**Date**: Novembre 2025  
**Auteur**: Construction Material Management Team
