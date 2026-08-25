package ch.admin.bit.jeap.jme.processcontext.test;

import ch.admin.bit.jeap.jme.test.BootServiceSpringIntegrationTestBase;
import ch.admin.bit.jeap.messaging.avro.security.AvroClassSecurity;
import ch.admin.bit.jeap.processcontext.archive.processsnapshot.v2.ProcessSnapshot;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SuppressWarnings("unchecked")
class ProcessContextExampleIT extends BootServiceSpringIntegrationTestBase {

    private static final String AUTH_BASE_URL = "http://localhost:8081/jme-process-context-auth-scs";
    private static final String SCS_BASE_URL = "http://localhost:8080/process-context";
    private static final String APP_BASE_URL = "http://localhost:8082/jme-process-context-app-service";
    private static final String AUTH_TOKEN_URL = AUTH_BASE_URL + "/oauth2/token";
    private static final Duration MAINTENANCE_TIMEOUT = Duration.ofSeconds(90);
    private static final String MAINTENANCE_PROCESS_ID_PLACEHOLDER = "maintenance-document-review";
    private static final String RELATION_ID_PLACEHOLDER = "00000000-0000-0000-0000-000000000000";

    @BeforeAll
    static void installAvroClassSecurity() {
        // This test deserializes an Avro generated type without a jEAP messaging Spring context installing the
        // Avro class whitelist, so it has to install the whitelist itself.
        AvroClassSecurity.installDefaultIfMissing();
    }

    @BeforeAll
    static void startServices() throws Exception {
        startService("jme-process-context-auth-scs", AUTH_BASE_URL);
        startService("jme-process-context-scs", SCS_BASE_URL);
        startService("jme-process-context-app-service", APP_BASE_URL);
    }

    @Test
    void runRaceProcessTest() throws Exception {
        String accessToken = retrieveAccessToken();

        String processId = "ci_test_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // Create process
        given()
                .baseUri(APP_BASE_URL)
                .contentType(ContentType.JSON)
                .body("{\"raceCarNumber\": \"" + processId + "\"}")
                .when()
                .post("/api/raceprocess/{processId}/createProcess?processCreationType=EVENT", processId);

        Map<String, String> expectedName = Map.of(
                "de", "Rennen durch die Schweiz",
                "fr", "Course à travers la Suisse",
                "it", "Corsa attraverso la Svizzera");

        // Wait for process to start
        await().until(() -> retrieveProcessData(accessToken, processId).get("state") != null);

        // Check created process
        JsonPath jsonPath = retrieveProcessData(accessToken, processId);
        assertThat((String) jsonPath.get("originProcessId")).isEqualTo(processId);
        assertThat(jsonPath.getMap("name")).isEqualTo(expectedName);
        assertThat((String) jsonPath.get("state")).isEqualTo("STARTED");

        // Start race
        given()
                .baseUri(APP_BASE_URL)
                .contentType(ContentType.JSON)
                .body("{\"raceCarNumber\": \"" + processId + "\"}")
                .when()
                .post("/api/raceprocess/{processId}/raceStarted?weatherAlertSubject={processId}", processId, processId);

        // Pass control points
        given().baseUri(APP_BASE_URL).when()
                .post("/api/raceprocess/{processId}/raceControlpointPassed?controlPoint=Bern", processId);
        given().baseUri(APP_BASE_URL).when()
                .post("/api/raceprocess/{processId}/raceControlpointPassed?controlPoint=Brig", processId);
        given().baseUri(APP_BASE_URL).when()
                .post("/api/raceprocess/{processId}/raceControlpointPassed?controlPoint=Chur", processId);

        // Validate race
        given().baseUri(APP_BASE_URL).when()
                .post("/api/raceprocess/{processId}/raceValidated", processId);

        // Reach destination
        given().baseUri(APP_BASE_URL).when()
                .post("/api/raceprocess/{processId}/raceDestinationReached", processId);

        // Finish refuelling
        given().baseUri(APP_BASE_URL).when()
                .post("/api/raceprocess/{processId}/carRefuellingCompleted", processId);

        // Check process after messages
        JsonPath processData = retrieveProcessData(accessToken, processId);
        assertThat((String) processData.get("originProcessId")).isEqualTo(processId);
        assertThat(processData.getMap("name")).isEqualTo(expectedName);

        // Wait for process to complete
        await().until(() -> "COMPLETED".equals(retrieveProcessData(accessToken, processId).get("state")));

        // Wait for the refuelling task to complete
        await().until(() -> {
            JsonPath data = retrieveProcessData(accessToken, processId);
            Map<String, Object> task = getTaskByValue(data.get("tasks"), "Rennwagen betanken");
            return "COMPLETED".equals(task.get("state"));
        });

        // Check tasks
        processData = retrieveProcessData(accessToken, processId);
        List<Map<String, Object>> tasks = processData.get("tasks");
        assertTaskRaceStart(tasks);
        assertTaskRaceValidated(tasks);
        assertTaskCarRefuelling(tasks);

        // Check that the snapshot triggered by the process completion has been created
        waitForSnapshotCreated(processId);
        assertGetSnapshotFromArchiveDataRestInterface(accessToken, processId);
    }

    @Test
    void runMaintenanceJobs() throws Exception {
        String accessToken = retrieveAccessToken();
        String processId = "maintenance-" + UUID.randomUUID();
        String versionId = "version-" + UUID.randomUUID();
        String reviewId = "review-" + UUID.randomUUID();

        given()
                .baseUri(APP_BASE_URL)
                .contentType(ContentType.JSON)
                .body("""
                        {"documentId":"%s","title":"Maintenance example","author":"jEAP"}
                        """.formatted(processId))
                .when()
                .post("/api/documentprocess/{processId}/createProcess", processId)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        await().atMost(MAINTENANCE_TIMEOUT)
                .until(() -> retrieveProcessData(accessToken, processId).get("state") != null);

        given()
                .baseUri(APP_BASE_URL)
                .contentType(ContentType.JSON)
                .body("""
                        {"versionId":"%s","versionNumber":"1","changes":"initial version"}
                        """.formatted(versionId))
                .when()
                .post("/api/documentprocess/{processId}/createDocumentVersion", processId)
                .then()
                .statusCode(HttpStatus.OK.value());

        await().atMost(MAINTENANCE_TIMEOUT).untilAsserted(() -> assertThat(retrieveProcessDataItems(accessToken, processId))
                .anySatisfy(item -> assertThat(item)
                        .containsEntry("key", "versionId")
                        .containsEntry("value", versionId)
                        .containsEntry("role", "1")));

        UUID reevaluationJobId = UUID.randomUUID();
        submitMaintenanceJob(accessToken, "reevaluation-jobs", reevaluationJobId,
                maintenanceFixture("reevaluation-job.yaml").replace(MAINTENANCE_PROCESS_ID_PLACEHOLDER, processId));
        awaitSuccessfulMaintenanceJob(accessToken, "reevaluation-jobs", reevaluationJobId,
                "origin-process-id: " + processId);
        assertThat(countRelations(processId)).isZero();

        UUID backfillJobId = UUID.randomUUID();
        String backfillRequest = maintenanceFixture("backfill-job.yaml")
                .replace(MAINTENANCE_PROCESS_ID_PLACEHOLDER, processId)
                .replace("maintenance-review", reviewId);
        submitMaintenanceJob(accessToken, "backfill-jobs", backfillJobId, backfillRequest);
        awaitSuccessfulMaintenanceJob(accessToken, "backfill-jobs", backfillJobId,
                "origin-process-id: " + processId);

        await().atMost(MAINTENANCE_TIMEOUT).untilAsserted(() -> assertThat(retrieveProcessDataItems(accessToken, processId))
                .anySatisfy(item -> assertThat(item)
                        .containsEntry("key", "reviewId")
                        .containsEntry("value", reviewId)
                        .containsEntry("role", "1")));

        PersistedRelation relation = await().atMost(MAINTENANCE_TIMEOUT)
                .until(() -> findRelation(processId), Optional::isPresent)
                .orElseThrow();
        await().atMost(MAINTENANCE_TIMEOUT)
                .untilAsserted(() -> assertThat(countRelationNotifications(accessToken, relation.idempotenceId()))
                        .isEqualTo(1));

        UUID publicationJobId = UUID.randomUUID();
        submitMaintenanceJob(accessToken, "relation-publication-jobs", publicationJobId,
                maintenanceFixture("relation-publication-job.yaml")
                        .replace(RELATION_ID_PLACEHOLDER, relation.id().toString()));
        awaitSuccessfulMaintenanceJob(accessToken, "relation-publication-jobs", publicationJobId,
                "relation-id: " + relation.id());
        await().atMost(MAINTENANCE_TIMEOUT)
                .untilAsserted(() -> assertThat(countRelationNotifications(accessToken, relation.idempotenceId()))
                        .isEqualTo(2));
    }

    private void submitMaintenanceJob(String accessToken, String jobEndpoint, UUID jobId, String request) {
        given()
                .config(RestAssured.config().encoderConfig(
                        EncoderConfig.encoderConfig().encodeContentTypeAs("application/yaml", ContentType.TEXT)))
                .baseUri(SCS_BASE_URL)
                .auth().oauth2(accessToken)
                .contentType("application/yaml")
                .body(request)
                .when()
                .put("/api/{jobEndpoint}/{jobId}", jobEndpoint, jobId)
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    private void awaitSuccessfulMaintenanceJob(String accessToken, String jobEndpoint, UUID jobId,
                                               String expectedTaskTarget) {
        await().atMost(MAINTENANCE_TIMEOUT).untilAsserted(() -> {
            String report = given()
                    .baseUri(SCS_BASE_URL)
                    .auth().oauth2(accessToken)
                    .accept("application/yaml")
                    .when()
                    .get("/api/{jobEndpoint}/{jobId}", jobEndpoint, jobId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().asString();
            assertThat(report)
                    .contains("job-state: completed")
                    .contains("job-result: succeeded")
                    .contains(expectedTaskTarget)
                    .contains("state: succeeded");
        });
    }

    private String maintenanceFixture(String fileName) throws Exception {
        return Files.readString(Path.of("..", "maintenance", fileName));
    }

    private Optional<PersistedRelation> findRelation(String processId) throws SQLException {
        try (Connection connection = openPcsDatabaseConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     select relation.id, relation.idempotence_id
                     from data.process_instance_relations relation
                     join data.process_instance process on process.id = relation.process_instance_id
                     where process.origin_process_id = ?
                     """)) {
            statement.setString(1, processId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(new PersistedRelation(
                        resultSet.getObject(1, UUID.class), resultSet.getObject(2, UUID.class))) : Optional.empty();
            }
        }
    }

    private int countRelations(String processId) throws SQLException {
        return queryCount("""
                select count(*)
                from data.process_instance_relations relation
                join data.process_instance process on process.id = relation.process_instance_id
                where process.origin_process_id = ?
                """, processId);
    }

    private int countRelationNotifications(String accessToken, UUID idempotenceId) {
        return given()
                .baseUri(SCS_BASE_URL)
                .auth().oauth2(accessToken)
                .when()
                .get("/api/example-relation-notifications/{idempotenceId}", idempotenceId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(Integer.class);
    }

    private int queryCount(String sql, Object parameter) throws SQLException {
        try (Connection connection = openPcsDatabaseConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private Connection openPcsDatabaseConnection() throws SQLException {
        String host = System.getenv("CI") == null ? "localhost" : "jme-pcs-db";
        return DriverManager.getConnection("jdbc:postgresql://" + host + ":5432/jme-pcs-db", "postgres", "secret");
    }

    private record PersistedRelation(UUID id, UUID idempotenceId) {
    }

    private String retrieveAccessToken() {
        return RestAssured.given()
                .config(RestAssured.config().encoderConfig(
                        EncoderConfig.encoderConfig().encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)))
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", "jme-process-context-it-client")
                .formParam("client_secret", "secret")
                .post(AUTH_TOKEN_URL)
                .jsonPath().get("access_token");
    }

    private JsonPath retrieveProcessData(String accessToken, String processId) {
        return given()
                .baseUri(SCS_BASE_URL)
                .auth().oauth2(accessToken)
                .when()
                .get("/api/processes/" + processId).jsonPath();
    }

    private List<Map<String, String>> retrieveProcessDataItems(String accessToken, String processId) {
        return given()
                .baseUri(SCS_BASE_URL)
                .auth().oauth2(accessToken)
                .when()
                .get("/api/processes/{processId}/process-data?size=100", processId)
                .jsonPath().getList("content");
    }

    private byte[] retrieveSnapshotArchiveData(String accessToken, String processId) {
        return given()
                .baseUri(SCS_BASE_URL)
                .auth().oauth2(accessToken)
                .when()
                .get("/api/snapshot/" + processId + "?version=1")
                .asByteArray();
    }

    private void waitForSnapshotCreated(String processId) {
        await().until(() -> processSnapshotExists(processId));
    }

    @Test
    void runSimpleProcessPerfTest() {
        // Start the simple process perf test with a single process instance
        String testRunId = given()
                .baseUri(APP_BASE_URL)
                .queryParam("processCount", 1)
                .queryParam("warmUpProcessCount", 1)
                .when()
                .post("/api/perftests/scenarios/simpleProcess")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .extract()
                .jsonPath().getString("id");

        log.info("Started simple process perf test with testRunId: {}", testRunId);

        // Wait for the perf test to complete successfully
        awaitPerfTestCompleted(testRunId);
    }

    @Test
    void runHighMessageCountPerfTest() {
        String testRunId = given()
                .baseUri(APP_BASE_URL)
                .queryParam("processCount", 1)
                .queryParam("warmUpProcessCount", 1)
                .queryParam("tasksPerProcessCount", 5)
                .queryParam("messagePerProcessCount", 5)
                .when()
                .post("/api/perftests/scenarios/highMessageCount")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .extract()
                .jsonPath().getString("id");

        log.info("Started high message count perf test with testRunId: {}", testRunId);
        awaitPerfTestCompleted(testRunId);
    }

    @Test
    void runProcessRelationsPerfTest() {
        String testRunId = given()
                .baseUri(APP_BASE_URL)
                .queryParam("processCount", 1)
                .queryParam("warmUpProcessCount", 1)
                .when()
                .post("/api/perftests/scenarios/processRelations")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .extract()
                .jsonPath().getString("id");

        log.info("Started process relations perf test with testRunId: {}", testRunId);
        awaitPerfTestCompleted(testRunId);
    }

    @Test
    void runProcessContextQueriesPerfTest() {
        String testRunId = given()
                .baseUri(APP_BASE_URL)
                .queryParam("processCount", 1)
                .queryParam("warmUpProcessCount", 1)
                .queryParam("messagePerProcessCount", 5)
                .when()
                .post("/api/perftests/scenarios/processContextQueries")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .extract()
                .jsonPath().getString("id");

        log.info("Started process context queries perf test with testRunId: {}", testRunId);
        awaitPerfTestCompleted(testRunId);
    }

    private void awaitPerfTestCompleted(String testRunId) {
        await().untilAsserted(() -> {
            String status = given()
                    .baseUri(APP_BASE_URL)
                    .when()
                    .get("/api/perftests/{testRunId}", testRunId)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .jsonPath().getString("status");
            log.info("Perf test {} status: {}", testRunId, status);
            assertThat(status).isEqualTo("COMPLETED");
        });
    }

    private boolean processSnapshotExists(String processId) {
        return given()
                .baseUri(APP_BASE_URL)
                .when()
                .get("/api/snapshot/" + processId + "/1")
                .statusCode() == HttpStatus.OK.value();
    }

    private void assertGetSnapshotFromArchiveDataRestInterface(String accessToken, String processId) throws Exception {
        byte[] data = retrieveSnapshotArchiveData(accessToken, processId);
        ProcessSnapshot processSnapshot = deserializeProcessSnapshot(data);
        assertThat(processSnapshot.getOriginProcessId()).isEqualTo(processId);
    }

    private static ProcessSnapshot deserializeProcessSnapshot(byte[] serializedSnapshot) throws Exception {
        DatumReader<ProcessSnapshot> datumReader = new SpecificDatumReader<>(ProcessSnapshot.class);
        Decoder decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(serializedSnapshot), null);
        return datumReader.read(null, decoder);
    }

    private void assertTaskRaceStart(List<Map<String, Object>> tasks) {
        Map<String, Object> task = getTaskByValue(tasks, "Rennen starten");
        assertThat(task).containsEntry("state", "COMPLETED");
        assertThat((Collection<?>) task.get("plannedBy")).isEmpty();
        assertThat((List<Map<?, ?>>) task.get("completedBy"))
                .contains(Map.of("key", "id", "value", "maxId", "label", Map.of("de", "Benutzer-ID", "fr", "No Utilisateur", "it", "No utente")))
                .contains(Map.of("key", "familyName", "value", "Starter", "label", Map.of("de", "Nachname", "fr", "Nom", "it", "Cognome")))
                .contains(Map.of("key", "givenName", "value", "Max", "label", Map.of("de", "Vorname", "fr", "Prénom", "it", "Nome")))
                .contains(Map.of("key", "customsOffice", "value", "Basel", "label", Map.of("de", "Dienststelle", "fr", "Bureau de douane", "it", "Ufficio doganale")));
    }

    private void assertTaskRaceValidated(List<Map<String, Object>> tasks) {
        Map<String, Object> task = getTaskByValue(tasks, "Rennstrecke validieren");
        assertThat(task).containsEntry("state", "COMPLETED");
        assertThat((Collection<?>) task.get("plannedBy")).isEmpty();
        assertThat((List<Map<?, ?>>) task.get("completedBy"))
                .contains(Map.of("key", "id", "value", "joeId", "label", Map.of("de", "Benutzer-ID", "fr", "No Utilisateur", "it", "No utente")))
                .contains(Map.of("key", "familyName", "value", "Validator", "label", Map.of("de", "Nachname", "fr", "Nom", "it", "Cognome")))
                .contains(Map.of("key", "givenName", "value", "Joe", "label", Map.of("de", "Vorname", "fr", "Prénom", "it", "Nome")))
                .contains(Map.of("key", "customsOffice", "value", "Bern", "label", Map.of("de", "Dienststelle", "fr", "Bureau de douane", "it", "Ufficio doganale")));
    }

    private void assertTaskCarRefuelling(List<Map<String, Object>> tasks) {
        Map<String, Object> task = getTaskByValue(tasks, "Rennwagen betanken");
        assertThat(task).containsEntry("state", "COMPLETED");

        // user data
        assertThat((List<Map<?, ?>>) task.get("plannedBy"))
                .contains(Map.of("key", "id", "value", "jackId", "label", Map.of("de", "Benutzer-ID", "fr", "No Utilisateur", "it", "No utente")))
                .contains(Map.of("key", "familyName", "value", "Reached", "label", Map.of("de", "Nachname", "fr", "Nom", "it", "Cognome")))
                .contains(Map.of("key", "givenName", "value", "Jack", "label", Map.of("de", "Vorname", "fr", "Prénom", "it", "Nome")))
                .contains(Map.of("key", "customsOffice", "value", "London", "label", Map.of("de", "Dienststelle", "fr", "Bureau de douane", "it", "Ufficio doganale")));
        assertThat((List<Map<?, ?>>) task.get("completedBy")).isEmpty();

        // task data
        assertThat((List<Map<?, ?>>) task.get("taskData")).containsExactlyInAnyOrder(
                Map.of("key", "parkingSpotNumber", "value", "7", "labels",
                        Map.of("de", "Parkplatznummer", "fr", "Numéro de place de parking", "it", "Numero del posto auto")),
                Map.of("key", "fuelType", "value", "gasoline", "labels",
                        Map.of("de", "Treibstofftyp", "fr", "Type de carburant", "it", "Tipo di carburante")),
                Map.of("key", "fuelAmount", "value", "65", "labels",
                        Map.of("de", "Treibstoffmenge", "fr", "Quantité de carburant", "it", "Quantità di carburante")));
    }

    private Map<String, Object> getTaskByValue(Object tasks, String name) {
        List<Map<String, Object>> taskList = (List<Map<String, Object>>) tasks;
        Optional<Map<String, Object>> optionalTask = taskList.stream()
                .filter(t -> checkTaskName(name, t.get("name")))
                .findFirst();
        assertThat(optionalTask).isPresent();
        return optionalTask.get();
    }

    private boolean checkTaskName(String name, Object values) {
        Map<String, String> map = (Map<String, String>) values;
        return map.get("de").equals(name);
    }


}
