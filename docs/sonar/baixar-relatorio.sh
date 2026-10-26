#!/usr/bin/env bash
# Baixa os resultados da analise do SonarQube para arquivos JSON.
# Usa a variavel de ambiente SONAR_TOKEN (o token NAO e gravado em nenhum arquivo).
#
# Uso (na raiz do projeto, no Git Bash):
#   bash docs/sonar/baixar-relatorio.sh primeira
#   bash docs/sonar/baixar-relatorio.sh segunda

ETAPA="${1:-analise}"
KEY="${SONAR_PROJECT_KEY:-Lab-stock}"
URL="${SONAR_HOST_URL:-http://localhost:9000}"
OUT="docs/sonar/$ETAPA"

if [ -z "$SONAR_TOKEN" ]; then
  echo "ERRO: defina o token antes. Exemplo: export SONAR_TOKEN=seu_token"
  exit 1
fi

mkdir -p "$OUT"

# baixar <caminho da API> <arquivo de saida>
baixar() {
  if curl -sf -u "$SONAR_TOKEN:" "$URL/api/$1" -o "$OUT/$2"; then
    echo "ok: $OUT/$2"
  else
    echo "FALHOU: $2 (confira o token e a chave do projeto: $KEY)"
  fi
}

baixar "qualitygates/project_status?projectKey=$KEY" quality-gate.json
baixar "measures/component?component=$KEY&metricKeys=ncloc,bugs,vulnerabilities,code_smells,security_hotspots,coverage,line_coverage,branch_coverage,duplicated_lines_density,duplicated_blocks,reliability_rating,security_rating,sqale_rating" medidas.json
baixar "issues/search?componentKeys=$KEY&resolved=false&ps=500" issues.json
baixar "hotspots/search?projectKey=$KEY&ps=500" hotspots.json
