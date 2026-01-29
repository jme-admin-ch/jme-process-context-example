package ch.admin.bit.jeap.jme.processcontext.condition;

import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionCondition;
import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionConditionResult;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessCompletionConclusion;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessContext;

public class TooManyMaintenanceStopsProcessCompletionCondition implements ProcessCompletionCondition {
    @Override
    public ProcessCompletionConditionResult isProcessCompleted(ProcessContext processContext) {
        if (processContext.countMessagesByType("JmeRaceCarMaintenanceRequiredEvent") > 2) {
            return ProcessCompletionConditionResult.completedBuilder()
                    .conclusion(ProcessCompletionConclusion.CANCELLED)
                    .name("tooManyMaintenanceStopsProcessCompletionCondition")
                    .build();
        } else {
            return ProcessCompletionConditionResult.IN_PROGRESS;
        }
    }
}
