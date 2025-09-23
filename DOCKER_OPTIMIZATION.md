# Optimisations Docker pour Fluxcord

Ce document explique les optimisations appliquées pour améliorer le temps de build du Dockerfile.

## 🚀 Optimisations Implémentées

### 1. **Cache des Dépendances Maven** (Gain le plus important)
- **Problème** : Les dépendances Maven étaient téléchargées à chaque build
- **Solution** : 
  - Copie des fichiers `pom.xml` en premier
  - Exécution de `mvn dependency:go-offline` pour pré-télécharger les dépendances
  - Cette couche Docker est mise en cache tant que les `pom.xml` ne changent pas

### 2. **Configuration Maven Optimisée**
- **Fichier `settings.xml`** avec :
  - Repository Maven Central optimisé
  - Politique de mise à jour quotidienne
  - Configuration des mirrors pour accélérer les téléchargements

### 3. **Build Multi-Stage Optimisé**
- **Séparation claire** entre build et production
- **Copie sélective** des artefacts nécessaires
- **Image de production** plus légère

### 4. **Cache Mounts avec BuildKit** (Optionnel)
- **Cache persistant** du repository Maven local
- **Cache des targets** de compilation
- **Gains significatifs** sur les builds répétés

### 5. **Optimisations Maven**
- **Build parallèle** : `-T1C` (1 thread par CPU core)
- **Skip des étapes inutiles** :
  - `-DskipTests` : Tests ignorés
  - `-Dmaven.test.skip=true` : Compilation des tests ignorée
  - `-Dmaven.javadoc.skip=true` : Génération Javadoc ignorée
  - `-Dmaven.source.skip=true` : Sources JAR ignorées
  - `-Dmaven.site.skip=true` : Site Maven ignoré

### 6. **Dockerfile Optimisé**
- **`.dockerignore`** pour réduire le contexte de build
- **Ordre des instructions** optimisé pour le cache Docker
- **Layers minimaux** pour réduire la taille de l'image

## 📊 Gains de Performance Attendus

| Optimisation | Gain Estimé | Description |
|--------------|-------------|-------------|
| Cache dépendances Maven | **60-80%** | Premier build : même temps, builds suivants : très rapides |
| BuildKit + Cache mounts | **40-60%** | Cache persistant entre builds |
| Skip étapes inutiles | **20-30%** | Moins d'opérations Maven |
| .dockerignore | **10-20%** | Contexte de build plus petit |
| **TOTAL** | **70-90%** | Sur les builds répétés |

## 🛠️ Utilisation

### Build Standard Optimisé
```bash
# Utilise Dockerfile.optimized
docker build -f Dockerfile.optimized -t fluxcord:latest .
```

### Build avec BuildKit (Recommandé)
```bash
# Active BuildKit et utilise les cache mounts
DOCKER_BUILDKIT=1 docker build -f Dockerfile.buildkit -t fluxcord:latest .
```

### Script Automatisé
```bash
# Utilise le script optimisé qui détecte automatiquement BuildKit
./build-optimized.sh
```

## 🔧 Configuration Requise

### Pour BuildKit (Optionnel mais recommandé)
- Docker 18.09+ avec BuildKit activé
- Ou Docker 19.03+ (BuildKit par défaut)

### Variables d'Environnement
```bash
# Activer BuildKit
export DOCKER_BUILDKIT=1

# Ou utiliser le script qui gère automatiquement
./build-optimized.sh
```

## 📈 Monitoring des Performances

### Mesurer le Temps de Build
```bash
# Build avec timing
time docker build -f Dockerfile.optimized -t fluxcord:latest .

# Comparer avec l'ancien Dockerfile
time docker build -f Dockerfile -t fluxcord:old .
```

### Analyser la Taille de l'Image
```bash
# Voir la taille des images
docker images fluxcord

# Analyser les layers
docker history fluxcord:latest
```

## 🎯 Recommandations

1. **Utilisez BuildKit** si disponible (gains les plus importants)
2. **Gardez les `pom.xml` stables** pour maximiser le cache
3. **Utilisez le script `build-optimized.sh`** pour une expérience optimale
4. **Surveillez la taille de l'image** pour éviter la dérive

## 🔍 Dépannage

### Cache Maven Corrompu
```bash
# Nettoyer le cache Docker
docker builder prune

# Nettoyer le cache BuildKit
rm -rf /tmp/.buildx-cache
```

### Build Échoue
```bash
# Build sans cache pour diagnostiquer
docker build --no-cache -f Dockerfile.optimized -t fluxcord:latest .
```

### Performance Décevante
1. Vérifiez que BuildKit est activé
2. Assurez-vous que les `pom.xml` n'ont pas changé
3. Vérifiez la connectivité réseau vers Maven Central