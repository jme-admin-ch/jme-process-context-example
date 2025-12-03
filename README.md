# JME Process Context Example

This example shows how to use the process context service. It contains the following modules

* jme-process-context-scs: An instance of the process context service
* jme-process-context-auth-scs: An instance of the oauth mock server to access the process context service
* jme-process-context-app-service: A mock for application using the process context service. This app
  will generate messages and call the rest endpoints of the process context service to simulate a real
  application. Note: In a real life situation, an application will consist of several microservices.
  To simplify the example there is only one here.

## Getting started

Before the examples can be started the infrastructure has to be started using docker

```
docker-compose -f docker/docker-compose.yml up
```

The project itself can be build with a simple

```
./mvnw install
```

Then the individual sub-projects can be started using

```
./mvnw --projects jme-process-context-auth-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw --projects jme-process-context-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw --projects jme-process-context-app-service spring-boot:run -Dspring-boot.run.profiles=local
```

The profile 'local-npm-ui' can be used in addition to the 'local' profile to run the SCS UI using 'npm start', which
will run the UI on port 4200..

The app provides a HTTP-Interface that allows you to trigger the various process changes. You can find its Swagger-UI
at http://localhost:8082/jme-process-context-app-service/swagger-ui.html

After you have created a process, you can find this process in the process-context service at
http://localhost:8080/process-context/process/{id}

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

## Changes

This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Note

This repository is part of the open source distribution of JME. See [github.com/jme-admin-ch/jme](https://github.com/jme-admin-ch/jme)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).