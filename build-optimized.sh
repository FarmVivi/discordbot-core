#!/bin/bash

# Script de build optimisé pour Fluxcord
# Utilise les meilleures pratiques pour accélérer le build Docker

set -e

echo "🚀 Build optimisé de Fluxcord avec Docker"

# Configuration
IMAGE_NAME="fluxcord"
TAG="latest"
DOCKERFILE="Dockerfile.optimized"

# Vérifier si BuildKit est disponible
if docker buildx version >/dev/null 2>&1; then
    echo "✅ BuildKit détecté - utilisation du Dockerfile optimisé avec cache mounts"
    DOCKERFILE="Dockerfile.buildkit"
    BUILDX_ARGS="--cache-from type=local,src=/tmp/.buildx-cache --cache-to type=local,dest=/tmp/.buildx-cache-new,mode=max"
else
    echo "⚠️  BuildKit non disponible - utilisation du Dockerfile standard optimisé"
    BUILDX_ARGS=""
fi

# Créer le cache directory si nécessaire
mkdir -p /tmp/.buildx-cache

echo "📦 Build de l'image avec le fichier: $DOCKERFILE"

# Build avec les optimisations
if [ -n "$BUILDX_ARGS" ]; then
    # Build avec BuildKit et cache mounts
    DOCKER_BUILDKIT=1 docker build \
        $BUILDX_ARGS \
        -f "$DOCKERFILE" \
        -t "$IMAGE_NAME:$TAG" \
        .
else
    # Build standard optimisé
    docker build \
        -f "$DOCKERFILE" \
        -t "$IMAGE_NAME:$TAG" \
        .
fi

# Déplacer le cache si BuildKit a été utilisé
if [ -n "$BUILDX_ARGS" ] && [ -d "/tmp/.buildx-cache-new" ]; then
    rm -rf /tmp/.buildx-cache
    mv /tmp/.buildx-cache-new /tmp/.buildx-cache
fi

echo "✅ Build terminé avec succès!"
echo "📊 Taille de l'image:"
docker images "$IMAGE_NAME:$TAG" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"

echo ""
echo "🔧 Commandes utiles:"
echo "  - Lancer le conteneur: docker run -d --name fluxcord $IMAGE_NAME:$TAG"
echo "  - Voir les logs: docker logs fluxcord"
echo "  - Arrêter: docker stop fluxcord"
echo "  - Supprimer: docker rm fluxcord"