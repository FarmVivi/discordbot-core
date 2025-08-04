# DiscordBot Core

DiscordBot Core is a modular framework for building Discord bots with plugin support. It provides a full command system, audio API, permissions, internationalisation and configuration utilities.

## Prerequisites

- **Java 17** or newer
- **Maven** for building the project

## Build

Compile the project and package the jar:

```bash
mvn package
```

## Tests

Run the unit tests with:

```bash
mvn test
```

## Docker (optional)

A `docker-compose.yml` is provided to run the bot in a container. It builds the application using the included `Dockerfile` and mounts the `data`, `plugins` and configuration directories.

Start the services with:

```bash
docker compose up --build
```

Stop them with `docker compose down`.

## Features and Commands

DiscordBot Core exposes several services for plugins:

- **Command system** – unified slash and prefix commands with permissions, cooldowns and categories. Includes built‑in commands such as `help`, `version` and `shutdown`.
- **Audio API** – send and receive audio streams with mixing and priority management.
- **Plugin manager** – load external plugins at runtime from the `plugins` folder.
- **Configuration and language management** – YAML configuration with environment overrides and i18n support.

See the `docs/` folder for detailed guides on the command system and audio API.
