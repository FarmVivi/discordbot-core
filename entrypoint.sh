#!/bin/sh
set -e

# Env vars
# INSTALL_PLUGINS: comma-separated list of plugin artifact simple names or '*' for all bundles (default: empty)
# INSTALL_EXAMPLES: true/false to allow example bundles to be installed (default: false)
# AUTO_UPDATE_PLUGINS: true/false to update installed plugins at container start if newer bundle available (default: true)
# JAVA_OPTS: extra JVM options

APP_DIR="/app"
PLUGINS_DIR="$APP_DIR/plugins"
BUNDLES_PLUGINS_DIR="/opt/discordbot/bundles/plugins"
BUNDLES_EXAMPLES_DIR="/opt/discordbot/bundles/examples"
CORE_JAR="$APP_DIR/discordbot-core.jar"
HEALTH_PORT="${HEALTH_PORT:-8081}"

INSTALL_PLUGINS="${INSTALL_PLUGINS:-}"
INSTALL_EXAMPLES="${INSTALL_EXAMPLES:-false}"
AUTO_UPDATE_PLUGINS="${AUTO_UPDATE_PLUGINS:-true}"

mkdir -p "$PLUGINS_DIR"

# --- Logging helpers ---
ts() { date -u +"%Y-%m-%dT%H:%M:%SZ"; }
log_info() { printf '%s [INFO] %s\n' "$(ts)" "$*"; }
log_warn() { printf '%s [WARN] %s\n' "$(ts)" "$*"; }
log_err()  { printf '%s [ERROR] %s\n' "$(ts)" "$*" 1>&2; }

log_info "Entrypoint started"
log_info "INSTALL_PLUGINS='${INSTALL_PLUGINS:-}' INSTALL_EXAMPLES='${INSTALL_EXAMPLES}' AUTO_UPDATE_PLUGINS='${AUTO_UPDATE_PLUGINS}' HEALTH_PORT='${HEALTH_PORT}'"
if [ -z "${INSTALL_PLUGINS}" ]; then
  log_info "No plugins requested for installation (INSTALL_PLUGINS is empty)."
fi
if [ "${AUTO_UPDATE_PLUGINS}" != "true" ]; then
  log_info "Auto-update of plugins is disabled (AUTO_UPDATE_PLUGINS='${AUTO_UPDATE_PLUGINS}')."
fi

normalize_name() {
  # Extract base artifactId without version suffix
  # e.g. music-plugin-2.3.27-SNAPSHOT.jar -> music-plugin
  f=$(basename "$1")
  echo "$f" | sed -E 's/(.*)-[0-9]+(\.[0-9]+)*(-SNAPSHOT)?\.jar/\1/' | sed -E 's/\.jar$//'
}

extract_version() {
  # e.g. music-plugin-2.3.27-SNAPSHOT.jar -> 2.3.27-SNAPSHOT
  f=$(basename "$1")
  echo "$f" | sed -nE 's/.*-([0-9]+(\.[0-9]+)*(-SNAPSHOT)?)\.jar/\1/p'
}

cmp_versions() {
  # returns 0 if v1==v2, 1 if v1>v2, 2 if v1<v2
  # handle suffix -SNAPSHOT as lower than release
  v1="$1"; v2="$2"
  s1=0; s2=0
  case "$v1" in *-SNAPSHOT) s1=1; v1=${v1%-SNAPSHOT};; esac
  case "$v2" in *-SNAPSHOT) s2=1; v2=${v2%-SNAPSHOT};; esac
  res=$(awk -v A="$v1" -v B="$v2" 'BEGIN{
    n=split(A,a,"."); m=split(B,b,".");
    L=(n>m?n:m);
    for(i=1;i<=L;i++){
      ai=(i<=n)?a[i]:0; bi=(i<=m)?b[i]:0;
      if ((ai+0)>(bi+0)){print 1; exit}
      if ((ai+0)<(bi+0)){print 2; exit}
    }
    print 0;
  }')
  if [ "$res" -eq 0 ]; then
    # numeric equal -> compare snapshot flag (snapshot < release)
    if [ "$s1" -lt "$s2" ]; then
      return 2
    elif [ "$s1" -gt "$s2" ]; then
      return 1
    fi
    return 0
  fi
  return "$res"
}

find_installed_for() {
  # arguments: name
  # outputs: path to highest version installed (or empty)
  name="$1"
  best=""; best_v=""
  for f in "$PLUGINS_DIR"/${name}-*.jar; do
    [ -e "$f" ] || continue
    v=$(extract_version "$f")
    if [ -z "$best" ]; then best="$f"; best_v="$v"; continue; fi
    cmp_versions "$v" "$best_v"
    case $? in
      1) best="$f"; best_v="$v" ;;
    esac
  done
  echo "$best"
}

remove_old_versions() {
  name="$1"; keep="$2"
  for f in "$PLUGINS_DIR"/${name}-*.jar; do
    [ -e "$f" ] || continue
    [ "$f" = "$keep" ] && continue
    rm -f "$f"
  done
}

install_or_update() {
  src="$1"
  name=$(normalize_name "$src")
  src_v=$(extract_version "$src")
  current=""; current_v=""
  current=$(find_installed_for "$name")
  if [ -z "$current" ]; then
    cp "$src" "$PLUGINS_DIR/"
    log_info "Installed plugin '$name' version $src_v"
    return 0
  fi
  current_v=$(extract_version "$current")
  if [ "$AUTO_UPDATE_PLUGINS" = "true" ]; then
    cmp_versions "$src_v" "$current_v"
    case $? in
      1)
        # src newer
        rm -f "$current"
        cp "$src" "$PLUGINS_DIR/"
        remove_old_versions "$name" "$PLUGINS_DIR/$(basename "$src")"
        log_info "Updated plugin '$name' from $current_v to $src_v"
        return 0
        ;;
    esac
  fi
  return 1
}

install_from_dir() {
  src_dir="$1"; allow="$2"
  [ -d "$src_dir" ] || return 0
  [ -z "$INSTALL_PLUGINS" ] && return 0

  OLDIFS="$IFS"; IFS=','; set -- $INSTALL_PLUGINS; IFS="$OLDIFS"
  for jar in "$src_dir"/*.jar; do
    [ -e "$jar" ] || continue
    name=$(normalize_name "$jar")
    should_install=false
    for w in "$@"; do
      [ "$w" = "*" ] && { should_install=true; break; }
      [ "$name" = "$w" ] && { should_install=true; break; }
    done
    if [ "$should_install" = true ] && [ "$allow" = true ]; then
      log_info "Traitement du plugin '${name}' depuis bundle ($(basename "$jar"))"
      if install_or_update "$jar"; then
        :
      else
        log_info "Aucune mise à jour nécessaire pour '${name}'"
      fi
    fi
  done
}

# Install requested plugins from bundled dirs
if [ -d "$BUNDLES_PLUGINS_DIR" ]; then
  log_info "Scanning bundled plugins in $BUNDLES_PLUGINS_DIR"
  install_from_dir "$BUNDLES_PLUGINS_DIR" true
else
  log_warn "No bundled plugins found ($BUNDLES_PLUGINS_DIR)"
fi
if [ "$INSTALL_EXAMPLES" = "true" ]; then
  if [ -d "$BUNDLES_EXAMPLES_DIR" ]; then
    log_info "Scanning bundled example plugins in $BUNDLES_EXAMPLES_DIR"
    install_from_dir "$BUNDLES_EXAMPLES_DIR" true
  else
    log_warn "No bundled example plugins found ($BUNDLES_EXAMPLES_DIR)"
  fi
else
  log_info "Installation of example plugins is disabled (INSTALL_EXAMPLES='${INSTALL_EXAMPLES}')."
fi

# Run application
log_info "Starting application (JAVA_OPTS='$JAVA_OPTS')..."
exec java $JAVA_OPTS -jar "$CORE_JAR"
