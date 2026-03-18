package ch.admin.bit.jeap.jme.processcontext.condition;

import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionCondition;
import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionConditionResult;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessCompletionConclusion;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessContext;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;

import java.util.Optional;

public class AllControlPointsPassedCompletionCondition implements ProcessCompletionCondition {

    @Override
    public ProcessCompletionConditionResult isProcessCompleted(ProcessContext processContext) {
        int expectedControlPointMessageCount = processContext.getMessageDataForMessageType("JmeRaceStartedEvent").stream()
                .filter(md -> md.getKey().equals("controlPointCount")) // See JmeRaceStartedEventPerfTestReferenceExtractor
                .flatMap(md -> maybeParseInt(md).stream())
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

    private static Optional<Integer> maybeParseInt(MessageData md) {
        try {
            return Optional.of(Integer.valueOf(md.getValue()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
