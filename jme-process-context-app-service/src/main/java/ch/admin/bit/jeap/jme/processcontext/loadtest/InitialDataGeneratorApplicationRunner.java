package ch.admin.bit.jeap.jme.processcontext.loadtest;

import ch.admin.bit.jeap.jme.processcontext.db.StatisticService;
import ch.admin.bit.jeap.jme.processcontext.domain.ProcessCreationType;
import ch.admin.bit.jeap.jme.processcontext.domain.TestProcessService;
import ch.admin.bit.jeap.jme.processcontext.web.NewProcessDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fills the database with initial data so that other load or performance tests don't start with an empty database
 */
@RequiredArgsConstructor
@Slf4j
@Profile("loadtest")
@Component
public class InitialDataGeneratorApplicationRunner implements ApplicationRunner {

    private final TestProcessService testProcessService;
    private final StatisticService statisticService;

    @Value("${loadtest.targetProcessInstances:2000}")
    private int targetProcessInstances;

    @Override
    public void run(ApplicationArguments args) {
        int currentProcessInstances = statisticService.countRows().getCount();
        log.info("Current process instances: " + currentProcessInstances);

        if (currentProcessInstances < targetProcessInstances) {
            log.info("Starting initial data creation ...");
            NewProcessDTO newProcessDTO = new NewProcessDTO("42");

            for (int i = 0; i < targetProcessInstances - currentProcessInstances; i++) {
                String processId = UUID.randomUUID().toString();
                testProcessService.create(processId, ProcessCreationType.REST, newProcessDTO);
                testProcessService.raceStarted(processId, "sunny");

                if (Math.random() < 0.2) {
                    testProcessService.planRaceCarPostChecks(processId);
                }
                if (Math.random() < 0.3) {
                    testProcessService.objectsOnTheRoadSpotted(processId, false);
                }
                if (Math.random() < 0.5) {
                    testProcessService.raceControlpointPassed(processId, "Bern");
                    testProcessService.raceControlpointPassed(processId, "Chur");
                    testProcessService.raceControlpointPassed(processId, "Brig");
                    testProcessService.mobileCheckpointPassed(processId, "mobile-1", "resolved", "mobile-1");
                    testProcessService.mobileCheckpointPassed(processId, "mobile-2", "resolved", "mobile-2");
                }
            }
            log.info("Initial data creation finished...");
        }
    }

}
