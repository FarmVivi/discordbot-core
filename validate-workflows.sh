#!/bin/bash

# Script de validation des workflows GitHub optimisés
# Vérifie la syntaxe et la configuration des workflows

set -e

echo "🔍 Validation des workflows GitHub optimisés"
echo "============================================="

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher les résultats
print_result() {
    local status=$1
    local message=$2
    if [ "$status" = "OK" ]; then
        echo -e "${GREEN}✅ $message${NC}"
    elif [ "$status" = "WARNING" ]; then
        echo -e "${YELLOW}⚠️  $message${NC}"
    else
        echo -e "${RED}❌ $message${NC}"
    fi
}

# Vérifier que nous sommes dans le bon répertoire
if [ ! -d ".github/workflows" ]; then
    echo -e "${RED}❌ Répertoire .github/workflows non trouvé${NC}"
    echo "   Assurez-vous d'être dans le répertoire racine du projet"
    exit 1
fi

echo ""
echo "📁 Vérification des fichiers de workflow"
echo "========================================"

# Liste des workflows attendus
WORKFLOWS=(
    "docker-image-ci.yml"
    "docker-dev-build.yml"
    "docker-performance-test.yml"
    "release-build.yml"
)

# Vérifier l'existence des workflows
for workflow in "${WORKFLOWS[@]}"; do
    if [ -f ".github/workflows/$workflow" ]; then
        print_result "OK" "Workflow $workflow trouvé"
    else
        print_result "ERROR" "Workflow $workflow manquant"
    fi
done

echo ""
echo "🔧 Vérification des optimisations Docker"
echo "========================================"

# Vérifier les Dockerfiles optimisés
DOCKERFILES=(
    "Dockerfile.optimized"
    "Dockerfile.buildkit"
    "settings.xml"
    ".dockerignore"
)

for dockerfile in "${DOCKERFILES[@]}"; do
    if [ -f "$dockerfile" ]; then
        print_result "OK" "Fichier $dockerfile trouvé"
    else
        print_result "ERROR" "Fichier $dockerfile manquant"
    fi
done

echo ""
echo "📊 Vérification de la syntaxe YAML"
echo "================================="

# Vérifier la syntaxe YAML des workflows
for workflow in "${WORKFLOWS[@]}"; do
    if [ -f ".github/workflows/$workflow" ]; then
        if command -v yq &> /dev/null; then
            if yq eval '.' ".github/workflows/$workflow" > /dev/null 2>&1; then
                print_result "OK" "Syntaxe YAML valide pour $workflow"
            else
                print_result "ERROR" "Erreur de syntaxe YAML dans $workflow"
            fi
        else
            print_result "WARNING" "yq non installé - impossible de valider la syntaxe YAML"
        fi
    fi
done

echo ""
echo "🚀 Vérification des optimisations spécifiques"
echo "============================================="

# Vérifier les optimisations dans docker-image-ci.yml
if [ -f ".github/workflows/docker-image-ci.yml" ]; then
    if grep -q "Dockerfile.buildkit" ".github/workflows/docker-image-ci.yml"; then
        print_result "OK" "Dockerfile.buildkit configuré dans docker-image-ci.yml"
    else
        print_result "WARNING" "Dockerfile.buildkit non trouvé dans docker-image-ci.yml"
    fi
    
    if grep -q "Cache Maven dependencies" ".github/workflows/docker-image-ci.yml"; then
        print_result "OK" "Cache Maven configuré dans docker-image-ci.yml"
    else
        print_result "WARNING" "Cache Maven non trouvé dans docker-image-ci.yml"
    fi
    
    if grep -q "platforms: linux/amd64,linux/arm64" ".github/workflows/docker-image-ci.yml"; then
        print_result "OK" "Build multi-architecture configuré"
    else
        print_result "WARNING" "Build multi-architecture non configuré"
    fi
fi

# Vérifier les optimisations dans release-build.yml
if [ -f ".github/workflows/release-build.yml" ]; then
    if grep -q "-T1C" ".github/workflows/release-build.yml"; then
        print_result "OK" "Build parallèle Maven configuré dans release-build.yml"
    else
        print_result "WARNING" "Build parallèle Maven non trouvé dans release-build.yml"
    fi
    
    if grep -q "Dmaven.javadoc.skip=true" ".github/workflows/release-build.yml"; then
        print_result "OK" "Skip Javadoc configuré dans release-build.yml"
    else
        print_result "WARNING" "Skip Javadoc non trouvé dans release-build.yml"
    fi
fi

echo ""
echo "📖 Vérification de la documentation"
echo "=================================="

# Vérifier la documentation
DOCS=(
    "README.md"
    "DOCKER_OPTIMIZATION.md"
    "GITHUB_WORKFLOWS.md"
    "OPTIMIZATION_COMPARISON.md"
)

for doc in "${DOCS[@]}"; do
    if [ -f "$doc" ]; then
        print_result "OK" "Documentation $doc trouvée"
    else
        print_result "WARNING" "Documentation $doc manquante"
    fi
done

# Vérifier que le README contient les nouvelles instructions Docker
if [ -f "README.md" ]; then
    if grep -q "Dockerfile.optimized" "README.md"; then
        print_result "OK" "Instructions Docker optimisées dans README.md"
    else
        print_result "WARNING" "Instructions Docker optimisées non trouvées dans README.md"
    fi
    
    if grep -q "BuildKit" "README.md"; then
        print_result "OK" "Instructions BuildKit dans README.md"
    else
        print_result "WARNING" "Instructions BuildKit non trouvées dans README.md"
    fi
fi

echo ""
echo "🛠️ Vérification des scripts utilitaires"
echo "======================================"

# Vérifier les scripts
SCRIPTS=(
    "build-optimized.sh"
    "test-optimizations.sh"
    "validate-workflows.sh"
)

for script in "${SCRIPTS[@]}"; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            print_result "OK" "Script $script trouvé et exécutable"
        else
            print_result "WARNING" "Script $script trouvé mais non exécutable"
        fi
    else
        print_result "WARNING" "Script $script manquant"
    fi
done

echo ""
echo "📋 Résumé de la validation"
echo "========================="

# Compter les erreurs et avertissements
ERRORS=$(grep -c "❌" <<< "$(tail -n +1 /dev/stdin)" 2>/dev/null || echo "0")
WARNINGS=$(grep -c "⚠️" <<< "$(tail -n +1 /dev/stdin)" 2>/dev/null || echo "0")

echo ""
if [ "$ERRORS" -eq 0 ] && [ "$WARNINGS" -eq 0 ]; then
    echo -e "${GREEN}🎉 Toutes les validations sont passées avec succès !${NC}"
    echo -e "${GREEN}   Les workflows GitHub sont prêts à être utilisés.${NC}"
elif [ "$ERRORS" -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Validation terminée avec $WARNINGS avertissement(s)${NC}"
    echo -e "${YELLOW}   Les workflows fonctionneront mais certaines optimisations peuvent être manquantes.${NC}"
else
    echo -e "${RED}❌ Validation échouée avec $ERRORS erreur(s) et $WARNINGS avertissement(s)${NC}"
    echo -e "${RED}   Veuillez corriger les erreurs avant d'utiliser les workflows.${NC}"
fi

echo ""
echo "🔧 Prochaines étapes recommandées :"
echo "1. Commiter les changements : git add . && git commit -m 'Optimize Docker builds and GitHub workflows'"
echo "2. Pousser vers GitHub : git push origin main"
echo "3. Tester les workflows : Aller dans Actions > Docker Image CI"
echo "4. Surveiller les performances : Utiliser le workflow Docker Performance Test"

echo ""
echo "📚 Documentation disponible :"
echo "- DOCKER_OPTIMIZATION.md : Guide complet des optimisations Docker"
echo "- GITHUB_WORKFLOWS.md : Documentation des workflows GitHub"
echo "- OPTIMIZATION_COMPARISON.md : Comparaison détaillée des optimisations"