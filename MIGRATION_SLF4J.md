# Migration de WyLog vers SLF4J + Logback

## Résumé des changements

Ce document décrit la migration complète de WyLog vers SLF4J avec Logback comme implémentation dans le projet FluxCord.

## Changements effectués

### 1. Dépendances Maven

#### POM parent (`pom.xml`)
- **Supprimé** : `wylog.version` (2.0.0)
- **Ajouté** : 
  - `slf4j.version` (2.0.17)
  - `logback.version` (1.4.7)
- **Remplacé** : Dépendance `net.wytrem:wylog` par `org.slf4j:slf4j-api` et `ch.qos.logback:logback-classic`

#### Modules affectés
- `fluxcord-api` : Remplacé WyLog par SLF4J API
- `fluxcord-core` : Remplacé WyLog par SLF4J + Logback
- `plugins/music-plugin` : Remplacé WyLog par SLF4J API
- `plugins/ai-audio-plugin` : Remplacé WyLog par SLF4J API
- `examples/plugins/plugin-example-audio` : Remplacé WyLog par SLF4J API
- `examples/plugins/plugin-example-commands` : Remplacé WyLog par SLF4J API
- `plugin-template` : Remplacé WyLog par SLF4J API

### 2. Configuration

#### Configuration Maven (`fluxcord-core/pom.xml`)
- **Supprimé** : Propriété système `net.wytrem.wylog.defaultLogLevel=debug`
- **Ajouté** : Propriété système `logback.configurationFile=logback.xml`

#### Fichiers de configuration Logback
- **Créé** : `fluxcord-core/src/main/resources/logback.xml` (configuration de production)
- **Créé** : `fluxcord-core/run/logback.xml` (configuration de développement)

### 3. Code Java

**Aucun changement requis** dans le code Java car :
- Le projet utilisait déjà l'API SLF4J standard (`LoggerFactory.getLogger()`)
- Les imports et l'utilisation des loggers restent identiques
- Seule l'implémentation sous-jacente change

## Avantages de la migration

### 1. Standardisation
- Utilisation de l'API SLF4J standard (industrie standard)
- Plus de dépendance sur une implémentation personnalisée

### 2. Flexibilité
- Possibilité de changer d'implémentation (Logback, Log4j2, etc.)
- Configuration plus riche et flexible

### 3. Performance
- Logback est généralement plus performant que WyLog
- Support des appenders asynchrones
- Rotation automatique des fichiers de log

### 4. Fonctionnalités avancées
- Filtres de log sophistiqués
- Appenders multiples (console + fichier)
- Configuration par environnement
- Support des markers et MDC

## Configuration Logback

### Format des logs
Le format des logs a été conservé similaire à WyLog :
```
[2024-01-01 12:00:00] [INFO] [ClassName] Message
```

### Niveaux de log
- **Production** : INFO par défaut, DEBUG pour FluxCord
- **Développement** : DEBUG par défaut
- **Bibliothèques externes** : WARN (JDA, HikariCP, MariaDB, AWS SDK)

### Appenders
- **Console** : Sortie vers la console
- **File** : Rotation automatique des fichiers de log (10MB max, 30 jours de rétention)

## Migration des propriétés WyLog

Les propriétés WyLog suivantes ont été migrées vers Logback :

| Propriété WyLog | Configuration Logback |
|----------------|----------------------|
| `net.wytrem.wylog.defaultLogLevel` | `<root level="INFO">` |
| `net.wytrem.wylog.showDateTime` | `%d{yyyy-MM-dd HH:mm:ss}` |
| `net.wytrem.wylog.showLogName` | `%logger{36}` |
| `net.wytrem.wylog.levelInBrackets` | `[%level]` |
| `net.wytrem.wylog.logFile` | Appenders CONSOLE et FILE |

## Tests

Pour tester la migration :

1. **Compilation** : `mvn clean compile`
2. **Exécution** : `mvn exec:java -pl fluxcord-core`
3. **Vérification des logs** : Les logs doivent apparaître dans le même format qu'avant

## Compatibilité

- ✅ **API** : 100% compatible (même interface SLF4J)
- ✅ **Format** : Format de log similaire
- ✅ **Configuration** : Migration transparente
- ✅ **Plugins** : Aucun changement requis dans les plugins

## Support

En cas de problème :
1. Vérifier la configuration Logback dans `logback.xml`
2. Consulter la documentation Logback : https://logback.qos.ch/
3. Vérifier les propriétés système Maven