package ch.admin.bit.jeap.jme.processcontext.condition;

import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessSnapshotCondition;
import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessSnapshotConditionResult;
import ch.admin.bit.jeap.processcontext.plugin.api.context.Message;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessContext;

@SuppressWarnings("unused")
public class DestinationReachedSnapshotCondition implements ProcessSnapshotCondition {

    private static final String SNAPSHOT_CONDITION_NAME = "JmeDestinationReachedSnapshotCondition";
    private static final String DESTINATION_REACHED_EVENT_NAME = "JmeRaceDestinationReachedEvent";

    @Override
    public ProcessSnapshotConditionResult triggerSnapshot(ProcessContext processContext) {
        if (processContext.getMessages().stream().map(Message::getName).
                anyMatch(DESTINATION_REACHED_EVENT_NAME::equals)) {
            return ProcessSnapshotConditionResult.triggeredFor(SNAPSHOT_CONDITION_NAME);
        } else {
            return ProcessSnapshotConditionResult.NOT_TRIGGERED;
        }
    }

}
