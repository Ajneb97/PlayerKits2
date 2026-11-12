#!/usr/bin/env bash
set -uo pipefail

cd "$(dirname "$0")" || exit 1

java_sources="src/main/java"
plugin_descriptor="src/main/resources/plugin.yml"
errors=0
warnings=0

error() {
    echo "error: $*"
    errors=$((errors + 1))
}

warning() {
    echo "warning: $*"
    warnings=$((warnings + 1))
}

build_log="$(mktemp)"
trap 'rm -f "$build_log"' EXIT

echo "==> Building"
if ! mvn -B -q clean package >"$build_log" 2>&1; then
    error "maven build failed: $(grep -m1 -E 'ERROR|BUILD FAILURE' "$build_log" || echo 'see maven output')"
fi
while IFS= read -r line; do
    warning "javac: $line"
done < <(grep -E '^\[WARNING\] .*\.java' "$build_log")
if ! grep -qF '<arg>-Xlint:all</arg>' pom.xml; then
    error "pom.xml does not compile with -Xlint:all"
fi

echo "==> Checking jar"
artifact_name="$(mvn -B -q help:evaluate -Dexpression=project.name -DforceStdout 2>/dev/null)"
version="$(mvn -B -q help:evaluate -Dexpression=project.version -DforceStdout 2>/dev/null)"
jar="target/${artifact_name}-${version}.jar"
if [[ -z "$version" || -z "$artifact_name" ]]; then
    error "could not read project name or version from pom.xml"
elif [[ ! -f "$jar" ]]; then
    error "$jar was not built"
else
    jar_version="$(unzip -p "$jar" plugin.yml 2>/dev/null | sed -n 's/^version: *//p' | tr -d "'\"")"
    if [[ "$jar_version" != "$version" ]]; then
        error "plugin.yml version '$jar_version' does not match pom.xml version '$version'"
    fi
fi

echo "==> Checking commands"
declared_commands="$(awk '/^commands:/{inside=1; next} /^[a-z]/{inside=0} inside && /^  [a-z0-9_-]+:$/{gsub(/[ :]/, ""); print}' "$plugin_descriptor")"
for command in $declared_commands; do
    if ! grep -rqF "\"$command\"" "$java_sources"; then
        error "/$command is declared in plugin.yml but never registered in Java"
    fi
done

echo "==> Checking sources"
while IFS= read -r line; do
    warning "comment: $line"
done < <(grep -rnE --exclude=Metrics.java '^\s*(//|/\*|\*)' "$java_sources")
while IFS= read -r line; do
    warning "unfinished: $line"
done < <(grep -rnE 'TODO|FIXME|XXX' src)

echo "==> $errors error(s), $warnings warning(s)"
[[ $errors -eq 0 && $warnings -eq 0 ]]
