package ch.admin.bit.jeap.jme.processcontext.web;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "JME Process Context Example",
                description = "An example how to use the process context service",
                contact = @Contact(
                        email = "jEAP-Community@bit.admin.ch",
                        name = "jEAP",
                        url = "https://confluence.eap.bit.admin.ch/display/BLUE/"
                )
        ),
        externalDocs = @ExternalDocumentation(
                url = "https://confluence.eap.bit.admin.ch/display/JEAP/Blueprint+Microservices",
                description = "Blueprint Microservices in Confluence")
)
@Configuration
public class SwaggerConfig {

    @Bean
    GroupedOpenApi raceProcessApi() {
        return GroupedOpenApi.builder()
                .group("Race Process API")
                .pathsToMatch("/api/raceprocess/**")
                .packagesToScan(this.getClass().getPackageName())
                .build();
    }

    @Bean
    GroupedOpenApi documentProcessApi() {
        return GroupedOpenApi.builder()
                .group("Document Process API")
                .pathsToMatch("/api/documentprocess/**")
                .packagesToScan(this.getClass().getPackageName())
                .build();
    }

    @Bean
    GroupedOpenApi processSnapshotApi() {
        return GroupedOpenApi.builder()
                .group("Process Snapshot API")
                .pathsToMatch("/api/snapshot/**")
                .packagesToScan(this.getClass().getPackageName())
                .build();
    }

}
