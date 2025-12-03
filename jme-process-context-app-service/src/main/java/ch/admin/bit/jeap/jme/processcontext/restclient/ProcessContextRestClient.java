package ch.admin.bit.jeap.jme.processcontext.restclient;

import ch.admin.bit.jeap.jme.processcontext.domain.ProcessContextClient;
import ch.admin.bit.jeap.processcontext.archive.processsnapshot.v2.ProcessSnapshot;
import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.SneakyThrows;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class ProcessContextRestClient implements ProcessContextClient {
    private final RestClient restClient;
    private final String processContextUrl;

    public ProcessContextRestClient(JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory,
                                    @Value("${example.processContextUrl}") String processContextUrl) {
        this.processContextUrl = processContextUrl;
        this.restClient = jeapOAuth2RestClientBuilderFactory
                .createForClientRegistryId("jme-process-context-app-service")
                .build();
    }

    @Override
    public void createProcess(String id, String templateName, Set<ch.admin.bit.jeap.jme.processcontext.domain.ExternalReferenceDTO> externalReferences) {
        restClient.put()
                .uri(processContextUrl + "/api/processes/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new NewProcessInstanceDTO(templateName, externalReferences.stream()
                        .map(extRef -> new ExternalReferenceDTO(extRef.getName(), extRef.getValue()))
                        .collect(Collectors.toSet())))
                .retrieve()
                .onStatus(statusCode -> statusCode.isSameCodeAs(HttpStatus.FORBIDDEN), (request, response) -> {
                    throw new AccessDeniedException("Insufficient authentication to access ProcessContext API.");
                })
                .toBodilessEntity();
    }

    @Override
    public Optional<ProcessSnapshot> getProcessSnapshot(String originProcessId, int version) {
        ResponseEntity<byte[]> response = restClient.get()
                .uri(processContextUrl + "/api/snapshot/{originProcessId}?version={version}", originProcessId, version)
                .retrieve()
                .toEntity(byte[].class);
        if (response.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN)) {
            throw new AccessDeniedException("Insufficient authentication to access the process context service snapshot API.");
        } else if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            return Optional.empty();
        } else {
            return Optional.of(deserializeProcessSnapshot(response));
        }
    }

    @SneakyThrows
    private static ProcessSnapshot deserializeProcessSnapshot(ResponseEntity<byte[]> response) {
        byte[] serializedSnapshot = response.getBody();
        String schemaVersion = response.getHeaders().toSingleValueMap().get("Archive-Data-Schema-Version");
        if (!StringUtils.hasText(schemaVersion)) {
            schemaVersion = response.getHeaders().toSingleValueMap().get("archive-data-schema-version");
        }
        int writerSchemaVersion = Integer.parseInt(schemaVersion);
        // currently, there are only two ProcessSnapshot schema versions: 1 and 2 -> if it ist not 2 it is 1
        Schema writerSchema = writerSchemaVersion == 2 ? ProcessSnapshot.getClassSchema() :
                ch.admin.bit.jeap.processcontext.archive.processsnapshot.v1.ProcessSnapshot.getClassSchema();
        Schema readerSchema = ProcessSnapshot.getClassSchema();
        DatumReader<ProcessSnapshot> datumReader = new SpecificDatumReader<>(writerSchema, readerSchema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(serializedSnapshot), null);
        return datumReader.read(null, decoder);
    }

}
