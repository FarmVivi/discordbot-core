# 🎯 Système de Synchronisation Automatique des Versions

## ✅ Configuration Mise en Place

### 1. **Filtering des Ressources Activé**
Tous les `pom.xml` des plugins ont maintenant la configuration :
```xml
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
    <!-- ... -->
</build>
```

### 2. **Variables Maven dans plugin.yml**
Tous les `plugin.yml` utilisent maintenant :
```yaml
version: ${project.version}
```

### 3. **Héritage des Versions**
Tous les plugins héritent de la version du POM parent :
```xml
<parent>
    <groupId>fr.farmvivi.fluxcord</groupId>
    <artifactId>fluxcord-parent</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</parent>
```

## 🔄 Comment Ça Fonctionne

### **Avant la Compilation :**
```yaml
# plugin.yml (source)
version: ${project.version}
```

### **Pendant la Compilation Maven :**
1. Maven lit le `pom.xml` du plugin
2. Maven hérite de la version du parent : `3.0.0-SNAPSHOT`
3. Maven active le filtering des ressources
4. Maven remplace `${project.version}` par `3.0.0-SNAPSHOT`

### **Après la Compilation :**
```yaml
# plugin.yml (dans target/classes)
version: 3.0.0-SNAPSHOT
```

## 🚀 Avantages du Système

### ✅ **Synchronisation Automatique**
- Plus besoin de mettre à jour manuellement les versions
- Les versions se synchronisent automatiquement avec le POM parent

### ✅ **Gestion des SNAPSHOT**
- Les suffixes `-SNAPSHOT` sont automatiquement gérés
- Pas de risque d'oublier de les ajouter/supprimer

### ✅ **Cohérence Garantie**
- Impossible d'avoir des versions différentes entre `pom.xml` et `plugin.yml`
- Une seule source de vérité : le POM parent

### ✅ **Maintenance Simplifiée**
- Pour changer la version : modifier uniquement le POM parent
- Tous les plugins suivent automatiquement

## 📋 Plugins Configurés

| Plugin | Status | Version Source |
|--------|--------|----------------|
| `music-plugin` | ✅ | `${project.version}` |
| `ai-audio-plugin` | ✅ | `${project.version}` |
| `plugin-template` | ✅ | `${project.version}` |
| `plugin-example-audio` | ✅ | `${project.version}` |
| `plugin-example-commands` | ✅ | `${project.version}` |

## 🎯 Résultat Final

**Maintenant, quand vous changez la version dans le POM principal :**
1. Tous les plugins héritent automatiquement de la nouvelle version
2. Tous les `plugin.yml` sont automatiquement mis à jour lors de la compilation
3. Plus jamais de versions désynchronisées ! 🎉