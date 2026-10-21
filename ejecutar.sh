#!/bin/sh
# Compila y ejecuta el módulo sin necesidad de Maven (requiere Java 17 o superior).
#   ./ejecutar.sh          → menú de especialidades
#   ./ejecutar.sh prueba   → prueba automática del CRUD contra la base de datos
set -e
cd "$(dirname "$0")"
CP="lib/postgresql-42.7.14.jar"
rm -rf out && mkdir -p out
javac --release 17 -encoding UTF-8 -d out -cp "$CP" $(find src -name "*.java")
if [ "$1" = "prueba" ]; then
  java -cp "out:$CP" co.edu.sena.medicitas.PruebaCrudEspecialidades
else
  java -cp "out:$CP" co.edu.sena.medicitas.app.MenuEspecialidades
fi
