# PCS Maintenance Example

This workflow demonstrates relation reevaluation, process-data backfill, and relation republication against the local
Docker PostgreSQL and Kafka services. Run all commands from the repository root against a fresh local database after
starting the infrastructure and applications as described in the main [README](../README.md).

## Prerequisites

Build the example and a released jEAP CLI containing the PCS maintenance commands:

```bash
./mvnw install -pl '!jme-process-context-test' -DskipTests
export JEAP_CLI_JAR=/path/to/jeap-cli.jar
export PCS_URL=http://localhost:8080/process-context
export PCS_ACCESS_TOKEN=$(curl -fsS \
  -u 'jme-process-context-it-client:secret' \
  -d 'grant_type=client_credentials' \
  http://localhost:8081/jme-process-context-auth-scs/oauth2/token \
  | jq -r '.access_token')
```

The local OAuth client has the required `processcontextjob:write` and `processcontextjob:read` semantic roles.

## Create Source State

Create a document-review process and one document version. The version has role `1`; no review is published, so the
matching `reviewId` process data and review-to-version relation do not yet exist.

```bash
curl -fsS -H 'Content-Type: application/json' \
  -d '{"documentId":"DOC-MAINTENANCE","title":"Maintenance example","author":"jEAP"}' \
  -X POST \
  http://localhost:8082/jme-process-context-app-service/api/documentprocess/maintenance-document-review/createProcess

curl -fsS -H 'Content-Type: application/json' \
  -d '{"versionId":"v1","versionNumber":"1","changes":"initial version"}' \
  -X POST \
  http://localhost:8082/jme-process-context-app-service/api/documentprocess/maintenance-document-review/createDocumentVersion

until curl -fsS -H "Authorization: Bearer $PCS_ACCESS_TOKEN" \
  "$PCS_URL/api/processes/maintenance-document-review/process-data?size=100" \
  | jq -e '.content[]? | select(.key == "versionId" and .value == "v1")' >/dev/null; do sleep 1; done
```

## Reevaluate Relations

Submit the complete [reevaluation-job.yaml](reevaluation-job.yaml):

```bash
export REEVALUATION_JOB_ID=$(printf '%s' "$PCS_ACCESS_TOKEN" | java -jar "$JEAP_CLI_JAR" pcs reevaluate-relations \
  --file=maintenance/reevaluation-job.yaml \
  --url="$PCS_URL")
```

For CSV mode, [reevaluation-job-csv.yaml](reevaluation-job-csv.yaml) contains the job metadata and
[processes.csv](processes.csv) contains the targets:

```bash
export REEVALUATION_CSV_JOB_ID=$(printf '%s' "$PCS_ACCESS_TOKEN" | java -jar "$JEAP_CLI_JAR" pcs reevaluate-relations \
  --file=maintenance/reevaluation-job-csv.yaml \
  --processes-csv=maintenance/processes.csv \
  --url="$PCS_URL")
```

Poll the asynchronous report until `job-state` is `completed`, `job-result` is `succeeded`, and the process task state
is `succeeded`:

```bash
printf '%s' "$PCS_ACCESS_TOKEN" | java -jar "$JEAP_CLI_JAR" pcs report \
  --job-type=reevaluation --job-id="$REEVALUATION_JOB_ID" --url="$PCS_URL"
```

The job succeeds but creates no relation because `reviewId` is still missing.

## Backfill Process Data

Submit the complete [backfill-job.yaml](backfill-job.yaml). It adds `reviewId` with role `1`, then PCS reevaluates the
relation pattern and creates the missing relation.

```bash
export BACKFILL_JOB_ID=$(printf '%s' "$PCS_ACCESS_TOKEN" | java -jar "$JEAP_CLI_JAR" pcs backfill \
  --file=maintenance/backfill-job.yaml \
  --url="$PCS_URL")
```

The equivalent CSV submission combines [backfill-job-csv.yaml](backfill-job-csv.yaml) and
[process-data.csv](process-data.csv):

```bash
export BACKFILL_CSV_JOB_ID=$(printf '%s' "$PCS_ACCESS_TOKEN" | java -jar "$JEAP_CLI_JAR" pcs backfill \
  --file=maintenance/backfill-job-csv.yaml \
  --process-data-csv=maintenance/process-data.csv \
  --url="$PCS_URL")
```

Poll the report:

```bash
printf '%s' "$PCS_ACCESS_TOKEN" | java -jar "$JEAP_CLI_JAR" pcs report \
  --job-type=backfill --job-id="$BACKFILL_JOB_ID" --url="$PCS_URL"
```

## Republish The Relation

Relation selection is an operator responsibility. Query the local Docker PostgreSQL database for the relation UUID;
the example intentionally does not expose a production API for this operation:

```bash
RELATION_ROW=$(docker compose -f docker/docker-compose.yml exec -T jme-pcs-db \
  psql -U postgres -d jme-pcs-db -Atc \
  "select r.id || ' ' || r.idempotence_id from data.process_instance_relations r join data.process_instance p on p.id=r.process_instance_id where p.origin_process_id='maintenance-document-review' limit 1")
read -r RELATION_ID RELATION_IDEMPOTENCE_ID <<< "$RELATION_ROW"
test -n "$RELATION_ID"
printf 'Persisted relation: %s\n' "$RELATION_ID"
```

The checked-in [relation-publication-job.yaml](relation-publication-job.yaml) and [relations.csv](relations.csv) use the
all-zero UUID as a visible placeholder. Produce copy-pasteable requests containing the queried UUID:

```bash
sed "s/00000000-0000-0000-0000-000000000000/$RELATION_ID/" \
  maintenance/relation-publication-job.yaml > /tmp/relation-publication-job.yaml
sed "s/00000000-0000-0000-0000-000000000000/$RELATION_ID/" \
  maintenance/relations.csv > /tmp/relations.csv
```

Submit either the complete YAML or CSV request:

```bash
export PUBLICATION_JOB_ID=$(printf '%s' "$PCS_ACCESS_TOKEN" | java -jar "$JEAP_CLI_JAR" pcs notify-relations \
  --file=/tmp/relation-publication-job.yaml \
  --url="$PCS_URL")

export PUBLICATION_CSV_JOB_ID=$(printf '%s' "$PCS_ACCESS_TOKEN" | java -jar "$JEAP_CLI_JAR" pcs notify-relations \
  --relations-csv=/tmp/relations.csv \
  --url="$PCS_URL")
```

Poll the report and verify that the example listener handled the relation again. A count of at least `2` covers the
initial relation notification and republication; it can be higher if both example submissions were run.

```bash
printf '%s' "$PCS_ACCESS_TOKEN" | java -jar "$JEAP_CLI_JAR" pcs report \
  --job-type=relation-publication --job-id="$PUBLICATION_JOB_ID" --url="$PCS_URL"

curl -fsS -H "Authorization: Bearer $PCS_ACCESS_TOKEN" \
  "$PCS_URL/api/example-relation-notifications/$RELATION_IDEMPOTENCE_ID"
```

All jobs use generated UUIDs. Reusing a UUID with the same normalized request is idempotent; reusing it with different
content returns `409 Conflict`.
