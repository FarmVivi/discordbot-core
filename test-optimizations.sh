#!/bin/bash

# Script de test pour comparer les performances des optimisations Docker
# À exécuter dans un environnement avec Docker installé

set -e

echo "🧪 Test des optimisations Docker pour Fluxcord"
echo "=============================================="

# Vérifier que Docker est disponible
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé ou n'est pas dans le PATH"
    echo "   Ce script doit être exécuté dans un environnement avec Docker"
    exit 1
fi

# Vérifier que BuildKit est disponible
BUILDKIT_AVAILABLE=false
if docker buildx version >/dev/null 2>&1; then
    BUILDKIT_AVAILABLE=true
    echo "✅ BuildKit détecté"
else
    echo "⚠️  BuildKit non disponible - certains tests seront ignorés"
fi

echo ""
echo "📊 Tests de Performance"
echo "======================"

# Fonction pour mesurer le temps de build
measure_build() {
    local dockerfile=$1
    local tag=$2
    local description=$3
    local buildkit=$4
    
    echo ""
    echo "🔨 Test: $description"
    echo "   Dockerfile: $dockerfile"
    echo "   Tag: $tag"
    
    if [ "$buildkit" = "true" ] && [ "$BUILDKIT_AVAILABLE" = "true" ]; then
        echo "   BuildKit: Activé"
        DOCKER_BUILDKIT=1 time docker build -f "$dockerfile" -t "$tag" . 2>&1 | grep -E "(real|user|sys|Successfully built)"
    else
        echo "   BuildKit: Désactivé"
        time docker build -f "$dockerfile" -t "$tag" . 2>&1 | grep -E "(real|user|sys|Successfully built)"
    fi
    
    # Afficher la taille de l'image
    echo "   Taille de l'image:"
    docker images "$tag" --format "   {{.Repository}}:{{.Tag}} - {{.Size}}"
}

# Test 1: Dockerfile original (sans cache)
echo ""
echo "🏁 Test 1: Dockerfile original (sans cache)"
measure_build "Dockerfile" "fluxcord:original" "Dockerfile original" "false"

# Test 2: Dockerfile optimisé (sans cache)
echo ""
echo "🏁 Test 2: Dockerfile optimisé (sans cache)"
measure_build "Dockerfile.optimized" "fluxcord:optimized" "Dockerfile optimisé" "false"

# Test 3: Dockerfile optimisé (avec cache)
echo ""
echo "🏁 Test 3: Dockerfile optimisé (avec cache)"
measure_build "Dockerfile.optimized" "fluxcord:optimized-cached" "Dockerfile optimisé avec cache" "false"

# Test 4: BuildKit (si disponible)
if [ "$BUILDKIT_AVAILABLE" = "true" ]; then
    echo ""
    echo "🏁 Test 4: BuildKit avec cache mounts"
    measure_build "Dockerfile.buildkit" "fluxcord:buildkit" "BuildKit avec cache mounts" "true"
fi

echo ""
echo "📈 Résumé des Performances"
echo "=========================="

# Afficher un tableau comparatif
echo ""
echo "| Version | Taille | Description |"
echo "|---------|--------|-------------|"
docker images fluxcord --format "| {{.Tag}} | {{.Size}} | {{.Repository}} |"

echo ""
echo "🔍 Analyse des Layers"
echo "===================="

# Analyser les layers de chaque image
for tag in original optimized optimized-cached; do
    if docker images "fluxcord:$tag" --format "{{.Repository}}:{{.Tag}}" | grep -q "fluxcord:$tag"; then
        echo ""
        echo "📦 Layers pour fluxcord:$tag:"
        docker history "fluxcord:$tag" --format "   {{.CreatedBy}} - {{.Size}}"
    fi
done

if [ "$BUILDKIT_AVAILABLE" = "true" ]; then
    if docker images "fluxcord:buildkit" --format "{{.Repository}}:{{.Tag}}" | grep -q "fluxcord:buildkit"; then
        echo ""
        echo "📦 Layers pour fluxcord:buildkit:"
        docker history "fluxcord:buildkit" --format "   {{.CreatedBy}} - {{.Size}}"
    fi
fi

echo ""
echo "✅ Tests terminés!"
echo ""
echo "💡 Recommandations:"
echo "   - Utilisez 'Dockerfile.optimized' pour un build standard optimisé"
echo "   - Utilisez 'Dockerfile.buildkit' avec BuildKit pour les meilleures performances"
echo "   - Le cache des dépendances Maven apporte le plus gros gain sur les builds répétés"
echo ""
echo "🧹 Nettoyage (optionnel):"
echo "   docker rmi fluxcord:original fluxcord:optimized fluxcord:optimized-cached fluxcord:buildkit"