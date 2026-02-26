package ch.admin.bit.jeap.jme.processcontext.condition;

import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionCondition;
import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionConditionResult;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessCompletionConclusion;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessContext;

public class AllControlPointsPassedCompletionCondition implements ProcessCompletionCondition {

    @Override
    public ProcessCompletionConditionResult isProcessCompleted(ProcessContext processContext) {
        int expectedControlPointMessageCount = processContext.getMessageDataForMessageType("JmeRaceStartedEvent").stream()
                .filter(md -> md.getKey().equals("controlPointCount")) // See JmeRaceStartedEventPerfTestReferenceExtractor
                .map(md -> Integer.valueOf(md.getValue()))
                .findFirst().orElse(Integer.MAX_VALUE);

        long actualControlPointMessageCount = processContext.countMessagesByType("JmeRaceControlpointPassedEvent");

        if (actualControlPointMessageCount >= expectedControlPointMessageCount) {
            return ProcessCompletionConditionResult.completedBuilder()
                    .conclusion(ProcessCompletionConclusion.SUCCEEDED)
                    .name("allControlPointsPassedCompletionCondition")
                    .build();
        } else {
            return ProcessCompletionConditionResult.IN_PROGRESS;
        }
    }
}
