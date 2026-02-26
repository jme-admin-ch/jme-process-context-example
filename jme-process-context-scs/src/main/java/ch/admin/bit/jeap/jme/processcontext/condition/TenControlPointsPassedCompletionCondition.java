package ch.admin.bit.jeap.jme.processcontext.condition;

import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionCondition;
import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionConditionResult;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessCompletionConclusion;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessContext;

public class TenControlPointsPassedCompletionCondition implements ProcessCompletionCondition {

    @Override
    public ProcessCompletionConditionResult isProcessCompleted(ProcessContext processContext) {
        long actualControlPointMessageCount = processContext.countMessagesByType("JmeRaceControlpointPassedEvent");

        if (actualControlPointMessageCount >= 10) {
            return ProcessCompletionConditionResult.completedBuilder()
                    .conclusion(ProcessCompletionConclusion.SUCCEEDED)
                    .name("tenControlPointsPassedCompletionCondition")
                    .build();
        } else {
            return ProcessCompletionConditionResult.IN_PROGRESS;
        }
    }
}
