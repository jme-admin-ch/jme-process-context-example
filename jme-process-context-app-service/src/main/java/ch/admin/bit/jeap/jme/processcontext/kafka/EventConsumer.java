package ch.admin.bit.jeap.jme.processcontext.kafka;

import ch.admin.bit.jeap.jme.processcontext.ProcessContextAppException;
import ch.admin.bit.jeap.jme.processcontext.domain.TestProcessService;
import ch.admin.bit.jeap.processcontext.event.process.instance.completed.ProcessInstanceCompletedEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Component listening to event from the process context service, updating the according test process
 */
@Component
@RequiredArgsConstructor
@Slf4j
class EventConsumer {
    private final TestProcessService testProcessService;

    @PostConstruct
    void info() {
        log.info("Start listening to events");
    }

    @KafkaListener(topics = TopicConfiguration.PROCESS_COMPLETED_TOPIC_NAME)
    public void consumeProcessCompletedEvent(final ProcessInstanceCompletedEvent event, Acknowledgment ack) {
        log.debug("Received process completed event {}", event.getIdentity().getEventId());
        String processId = event.getOptionalProcessId().orElseThrow(() -> ProcessContextAppException.invalidInput("No processId in event"));
        boolean eventCorrelatesToKnownRaceTestProcess = testProcessService.findById(processId).isPresent();
        if (eventCorrelatesToKnownRaceTestProcess) {
            // Commit changes to the database
            testProcessService.processCompleted(processId);
        } else {
            // This PCS example also features a 'document review' process and a 'doping' process, but those
            // processes' states are not tracked by the example and therefore shall be ignored here.
            log.warn("Ignoring a ProcessInstanceCompletedEvent with idempotence id '{}' for process id '{}'.",
                    event.getIdentity().getIdempotenceId(), processId);
        }
        // Then acknowledge the event
        ack.acknowledge();
        log.debug("Acknowledged event {}", event.getIdentity().getEventId());
    }
}
