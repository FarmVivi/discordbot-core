# GitHub Workflows - Fluxcord

Ce document décrit les workflows GitHub optimisés pour Fluxcord, incluant les builds Docker, les tests de performance, et les déploiements.

## 🚀 Workflows Disponibles

### 1. **Docker Image CI** (`.github/workflows/docker-image-ci.yml`)

**Déclenchement :**
- Push sur `main`, `develop`
- Tags `v*.*.*`
- Pull requests sur `main`

**Fonctionnalités :**
- ✅ Build multi-architecture (linux/amd64, linux/arm64)
- ✅ Cache Docker layers optimisé
- ✅ Cache Maven dependencies
- ✅ Utilise `Dockerfile.buildkit` pour les meilleures performances
- ✅ Push automatique vers GHCR
- ✅ Métadonnées Docker automatiques

**Optimisations :**
```yaml
# Cache Docker layers
- name: Cache Docker layers
  uses: actions/cache@v4.2.4
  with:
    path: /tmp/.buildx-cache
    key: ${{ runner.os }}-buildx-${{ github.sha }}

# Cache Maven dependencies
- name: Cache Maven dependencies
  uses: actions/cache@v4.2.4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}

# Build avec BuildKit
- name: Build and push Docker image
  uses: docker/build-push-action@v6.18.0
  with:
    file: ./Dockerfile.buildkit
    platforms: linux/amd64,linux/arm64
    cache-from: type=local,src=/tmp/.buildx-cache
    cache-to: type=local,dest=/tmp/.buildx-cache-new,mode=max
```

### 2. **Docker Development Build** (`.github/workflows/docker-dev-build.yml`)

**Déclenchement :**
- Push sur `develop`, `feature/*`
- Pull requests sur `develop`

**Fonctionnalités :**
- ✅ Build rapide pour le développement
- ✅ Utilise `Dockerfile.optimized`
- ✅ Tests automatiques de l'image
- ✅ Rapport de build avec informations détaillées
- ✅ Pas de push (build local uniquement)

**Utilisation :**
```yaml
# Build optimisé pour le développement
- name: Build Docker image (Development)
  uses: docker/build-push-action@v6.18.0
  with:
    file: ./Dockerfile.optimized
    push: false
    tags: fluxcord:dev-${{ github.sha }}
```

### 3. **Docker Performance Test** (`.github/workflows/docker-performance-test.yml`)

**Déclenchement :**
- Déclenchement manuel (`workflow_dispatch`)
- Planifié chaque dimanche à 2h (`cron: '0 2 * * 0'`)

**Fonctionnalités :**
- ✅ Comparaison des performances entre Dockerfiles
- ✅ Tests de cache et de build répétés
- ✅ Génération de rapports de performance
- ✅ Upload des rapports comme artefacts
- ✅ Commentaires automatiques sur les PR

**Tests effectués :**
1. Build avec `Dockerfile` original
2. Build avec `Dockerfile.optimized`
3. Build avec `Dockerfile.buildkit`
4. Build avec cache (deuxième build)

### 4. **Release Build** (`.github/workflows/release-build.yml`)

**Déclenchement :**
- Création de release GitHub

**Fonctionnalités :**
- ✅ Build Maven optimisé avec `-T1C`
- ✅ Cache Maven dependencies
- ✅ Suppression automatique des `-SNAPSHOT`
- ✅ Upload du JAR sur la release
- ✅ Skip des tests et Javadoc pour la rapidité

**Optimisations :**
```yaml
# Build Maven optimisé
- name: Build with Maven
  run: mvn -T1C package -DskipTests -Dmaven.test.skip=true -Dmaven.javadoc.skip=true
```

## 📊 Métriques de Performance

### Temps de Build Estimés

| Workflow | Premier Build | Build avec Cache | Gain |
|----------|---------------|------------------|------|
| **Docker Image CI** | 8-12 min | 2-4 min | 70-80% |
| **Docker Dev Build** | 5-8 min | 1-2 min | 75-85% |
| **Release Build** | 3-5 min | 1-2 min | 60-70% |

### Utilisation du Cache

| Type de Cache | Taille Estimée | Durée de Vie |
|---------------|----------------|--------------|
| **Docker Layers** | 500MB-1GB | 7 jours |
| **Maven Dependencies** | 200-500MB | 7 jours |
| **BuildKit Cache** | 1-2GB | 7 jours |

## 🔧 Configuration Requise

### Secrets GitHub

| Secret | Description | Workflow |
|--------|-------------|----------|
| `GITHUB_TOKEN` | Token automatique | Tous |
| `DEPLOY_WEBHOOK_URL` | Webhook de déploiement | Docker Image CI |

### Permissions

```yaml
permissions:
  contents: read         # Pour checkout
  packages: write        # Pour push sur GHCR
```

## 🚀 Utilisation

### Déclencher un Build de Performance

```bash
# Via GitHub CLI
gh workflow run "Docker Performance Test"

# Via l'interface GitHub
# Actions > Docker Performance Test > Run workflow
```

### Surveiller les Builds

```bash
# Voir les workflows en cours
gh run list

# Voir les logs d'un workflow
gh run view <run-id>

# Télécharger les artefacts
gh run download <run-id>
```

### Optimiser les Builds

1. **Maintenir les `pom.xml` stables** pour maximiser le cache Maven
2. **Utiliser les branches appropriées** pour déclencher les bons workflows
3. **Surveiller les métriques** via les rapports de performance
4. **Nettoyer le cache** si nécessaire via les actions GitHub

## 🔍 Dépannage

### Build Lent

**Causes possibles :**
- Cache expiré ou corrompu
- Changements dans les `pom.xml`
- Problèmes de connectivité réseau

**Solutions :**
```bash
# Nettoyer le cache via GitHub CLI
gh api repos/${{ github.repository }}/actions/caches --method DELETE

# Vérifier les logs de build
gh run view <run-id> --log
```

### Erreurs de Cache

**Symptômes :**
- Build échoue avec erreurs de cache
- Temps de build anormalement longs

**Solutions :**
1. Vérifier les permissions du cache
2. Nettoyer le cache corrompu
3. Redémarrer le workflow

### Problèmes de BuildKit

**Symptômes :**
- Erreurs avec `Dockerfile.buildkit`
- Cache mounts non fonctionnels

**Solutions :**
1. Vérifier la version de Docker (18.09+)
2. Utiliser `Dockerfile.optimized` en fallback
3. Activer BuildKit explicitement

## 📈 Améliorations Futures

### Optimisations Planifiées

1. **Cache Registry** : Utiliser un registry Docker pour le cache
2. **Build Matrix** : Tests sur plusieurs versions de Docker
3. **Notifications** : Intégration Slack/Discord pour les builds
4. **Métriques** : Dashboard de performance des builds
5. **Auto-scaling** : Utilisation de runners auto-scalés

### Intégrations Possibles

- **SonarQube** : Analyse de qualité du code
- **Dependabot** : Mise à jour automatique des dépendances
- **Security Scanning** : Scan de vulnérabilités
- **Performance Monitoring** : Surveillance des performances en production

## 🎯 Recommandations

1. **Utilisez les workflows appropriés** selon votre contexte
2. **Surveillez les métriques** de performance régulièrement
3. **Maintenez les caches** en évitant les changements inutiles
4. **Testez les optimisations** avec le workflow de performance
5. **Documentez les changements** dans les workflows