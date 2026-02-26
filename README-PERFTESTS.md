# PCS Performance and Load Tests

The jme-process-context-app-service provides a built-in performance test harness for load-testing the PCS.

The tests can be started using the REST API of the jme-process-context-app-service, and the test status and results
can be queried via REST as well. The tests simulate different scenarios of process instance creation, message
correlation, and process relations, and measure the performance of the PCS under load.

The easies way to start the tests is using the SwaggerUI
at http://localhost:8082/jme-process-context-app-service/swagger-ui.html.

When test run is complete or failed, an HTML test report will be generated (see API documentation below for details).

## Performance Tests

### Test Scenarios

| Scenario           | Process Template          | Description                                                                                                                                        |
|--------------------|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| Simple Process     | `perfestSimpleProcess`    | Simulates a simple process instance observing ten messages and completing after the tenth message, creating a task per message, without relations. |
| High Message Count | `perfestHighMessageCount` | Simulates process instances with a large number of correlated messages and created relations.                                                      |
| Process Relations  | `perfestProcessRelations` | Simulates a process instance with a large number of process relations to other process instances.                                                  |

### REST Endpoints

Base path: `/api/perftests/`

#### Start a test run

| Method | Path                          | Description                         |
|--------|-------------------------------|-------------------------------------|
| POST   | `/scenarios/simpleProcess`    | Start a Simple Process test run     |
| POST   | `/scenarios/highMessageCount` | Start a High Message Count test run |
| POST   | `/scenarios/processRelations` | Start a Process Relations test run  |

Common query parameters:

| Parameter            | Default | Description                                   |
|----------------------|---------|-----------------------------------------------|
| `processCount`       | 100     | Number of process instances to create         |
| `warmUpProcessCount` | 5       | Number of warm-up processes                   |
| `durationMinutes`    | 0       | Duration to spread load over (0 = burst mode) |
| `timeoutMinutes`     | 10      | Test timeout                                  |
| `clearDatabase`      | true    | Clear database before test                    |

Additional parameters for High Message Count:

| Parameter                | Default | Description                         |
|--------------------------|---------|-------------------------------------|
| `tasksPerProcessCount`   | 100     | Number of tasks created per process |
| `messagePerProcessCount` | 100000  | Number of messages per process      |

All POST endpoints return `202 Accepted` with a test run ID.

#### Query test status and report

| Method | Path                  | Description                         |
|--------|-----------------------|-------------------------------------|
| GET    | `/latest`             | Status of the latest test run       |
| GET    | `/{testRunId}`        | Status of a specific test run       |
| GET    | `/latest/report`      | HTML report for the latest test run |
| GET    | `/{testRunId}/report` | HTML report for a specific test run |

### Test Report

The HTML report includes:

- **Summary** — status, timestamps, durations, process instance counts, message throughput, and creation throughput.
- **Scenario Parameters** — the parameters used for the test run.
- **Verification Results** — messages from post-test verification checks (e.g. expected completion counts, relation
  counts).
- **Process Creation Delay** — min/max/avg/median time from event creation in the load generator until the PCS created
  the process instance.
- **Process Completion Duration** — min/max/avg/median time from process instance creation until completion.
- **Time Series Charts** — processes created per timeslot, processes completed per timeslot, creation delay over time,
  and completion duration over time.
