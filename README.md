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

## Infrastructure for load tests
The application `jme-process-context-app-service` can be started with an additional profile `loadtest` to generate initial
data. This is useful for load and performance tests, as they would start with a non-empty database.

When the profile is enabled, then the bean `ch.admin.bit.jeap.jme.processcontext.loadtest.InitialDataGeneratorApplicationRunner`
would generate process instances until the target count in the database reaches the value set by property `loadtest.targetProcessInstances`. 
Default value is 2000.
 
## Profiles 

* **application-local:** Contains all configurations for running the application locally.
* **application-local-npm-ui:** Contains the configurations for connecting to the frontend.

## Note

This repository is part of the open source distribution of JME. See [github.com/jme-admin-ch/jme](https://github.com/jme-admin-ch/jme)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
