# JME Process Context Example

This example shows how to use the process context service. It contains the following modules

* jme-process-context-scs: An instance of the process context service
* jme-process-context-auth-scs: An instance of the oauth mock server to access the process context service
* jme-process-context-app-service: A mock for application using the process context service. This app
  will generate messages and call the rest endpoints of the process context service to simulate a real
  application. Note: In a real life situation, an application will consist of several microservices.
  To simplify the example there is only one here.

This example project show how to use the [jeap-process-context-service](https://github.com/jeap-admin-ch/jeap-process-context-service) library.
The library contains all the necessary components to set up a process context service instance.

## Changes

This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Prerequisites

To use this project, ensure you have the following installed:

1. **Java Development Kit (JDK)**: Version 25.
2. **Docker**: For running the required infrastructure.

**Note:** Use the provided maven wrapper to build and run the project.

**Optional:**
If you want to start the frontend UI independent of the Spring Boot application, you will also need:
- **Node.js and npm**: Required for running the frontend UI (if you want to start it detatched from the Spring Boot application).

## Getting started

### Infrastructure

Before the examples can be started the infrastructure has to be started using docker

```shell
docker-compose -f docker/docker-compose.yml up
```

### Build

The project itself can be built with a simple

```shell
./mvnw install
```

### Start 

Then the individual subprojects can be started using

```shell
./mvnw --projects jme-process-context-auth-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw --projects jme-process-context-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw --projects jme-process-context-app-service spring-boot:run -Dspring-boot.run.profiles=local
```

### Independent UI

To run the frontend UI independent of the Spring Boot application, the profile 'local-npm-ui' can be used in addition to the 'local' profile.
This allows to use hot reload for the UI without the need to restart the Spring Boot application after every change.

See the [jeap-process-context-service](https://github.com/jeap-admin-ch/jeap-process-context-service) library for details on the frontend.

## Process Examples

To try out the application yourself and for detailed process examples with step-by-step instructions, see [PROCESS-EXAMPLES.md](./PROCESS-EXAMPLES.md).

## Profiles 

* **application-local:** Contains all configurations for running the application locally.
* **application-local-npm-ui:** Contains the configurations for connecting to the frontend.

## Integration Tests

The `jme-process-context-test` module contains end-to-end integration tests that verify that the PCS and its example
work: it covers docker compose infrastructure, the process context service and the example application service.

### How it works

The test uses Spring Boot Docker Compose support to automatically start and stop the Docker infrastructure
(PostgreSQL, two Kafka clusters with schema registries, and MinIO) before and after the test run. It then starts
the three Spring Boot services (auth, PCS, app service) as Maven subprocesses via `mvnw spring-boot:run` and
polls their health endpoints until they are ready.

The tests themselves use REST-Assured to interact with the services and Awaitility for polling asynchronous state:

- **`createAndStartProcess`** — Exercises the full race process lifecycle: creates a process, publishes events
  (race start, control points, validation, destination reached, refuelling), and verifies that the process
  completes with the expected tasks, user references, and a snapshot in the archive.
- **`runSimpleProcessPerfTest`** — Runs the simple process scenario with a single process instance and verifies
  successful completion.
- **`runHighMessageCountPerfTest`** — Runs the high message count scenario with a single process instance and a
  low number of tasks and messages per process.
- **`runProcessRelationsPerfTest`** — Runs the process relations scenario with a single process instance.
- **`runProcessContextQueriesPerfTest`** — Runs the process context queries scenario with a single process
  instance and a low number of messages per process.

### Running locally

```shell
# Build and install all local modules
./mvnw install -pl '!:jme-process-context-test'
# Run integration tests
./mvnw test -pl jme-process-context-test
```

This will:

1. Start the Docker Compose infrastructure (containers are stopped after the test).
2. Build and start the three Spring Boot services on ports 8080, 8081, and 8082.
3. Run the integration tests.
4. Stop all services and containers.

Ensure Docker is running and ports 5432, 8080–8082, 9000, 10000–13000 are available.

### Running on CI

On CI the `CI` environment variable must be set. This activates the `ci` Spring profile which uses
`docker-compose-ci.yml` as an overlay (removing host port bindings and using container-internal hostnames for
Kafka advertised listeners). On CI, an isolated Docker network is used to allow for parallel builds.

## Performance Tests

See [README-PERFTESTS.md](./README-PERFTESTS.md) for information about the built-in load and performance tests for the
process context service.

## Note

This repository is part of the open source distribution of JME. See [github.com/jme-admin-ch/jme](https://github.com/jme-admin-ch/jme)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
