package ch.admin.bit.jeap.jme.processcontext.test;

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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SuppressWarnings("unchecked")
public class ProcessContextExampleIT extends SpringBootServiceTestBase {

    private static final String AUTH_BASE_URL = "http://localhost:8081/jme-process-context-auth-scs";
    private static final String SCS_BASE_URL = "http://localhost:8080/process-context";
    private static final String APP_BASE_URL = "http://localhost:8082/jme-process-context-app-service";
    private static final String AUTH_TOKEN_URL = AUTH_BASE_URL + "/oauth2/token";

    @BeforeAll
    static void startServices() throws Exception {
        startService("jme-process-context-auth-scs", AUTH_BASE_URL);
        startService("jme-process-context-scs", SCS_BASE_URL);
        startService("jme-process-context-app-service", APP_BASE_URL);
    }

    @Test
    void createAndStartProcess() throws Exception {
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

    @SpringBootApplication
    public static class TestApp {
    }
}
