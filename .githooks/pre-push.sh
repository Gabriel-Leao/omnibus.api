#!/usr/bin/env bash
set -e

echo "==> Rodando testes..."
./mvnw test

echo "==> Verificando formatação (Spotless)..."
./mvnw spotless:check

echo "==> Verificando estilo de código (Checkstyle)..."
./mvnw checkstyle:check

echo "✅ Tudo passou — prosseguindo com o push."
