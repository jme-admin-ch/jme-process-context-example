package ch.admin.bit.jeap.jme.processcontext.kafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "example.topic")
@Data
@Slf4j
class TopicConfiguration {
    public static final String PROCESS_COMPLETED_TOPIC_NAME = "${example.topic.process-completed}";
    public static final String PROCESS_MILESTONE_REACHED_TOPIC_NAME = "${example.topic.process-milestone-reached}";

    private String processCompleted;
    private String processMilestoneReached;
    private String raceStarted;
    private String raceControlpointPassed;
    private String raceControlpointPlanned;
    private String raceDestinationReached;
    private String raceMobileCheckpointPassed;
    private String raceMobileCheckpointPlanned;
    private String raceCarPostChecksCompleted;
    private String raceCarPostChecksPlanned;
    private String raceCarMaintenanceRequired;
    private String raceCarRefuellingCompleted;
    private String raceCarTirePressureChecked;
    private String weatherAlertActivated;
    private String createProcessInstance;
    private String objectsOnRoadSpotted;
    private String raceValidated;
    private String racePrepared;
    private String cancelRace;
    private String documentCreated;
    private String documentVersionCreated;
    private String documentReviewed;
    private String documentVersionReviewed;
}
