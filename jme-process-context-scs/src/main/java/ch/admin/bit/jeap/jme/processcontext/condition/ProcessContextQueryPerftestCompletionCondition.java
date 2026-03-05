package ch.admin.bit.jeap.jme.processcontext.condition;

import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionCondition;
import ch.admin.bit.jeap.processcontext.plugin.api.condition.ProcessCompletionConditionResult;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessCompletionConclusion;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessContext;

import java.util.Map;
import java.util.Set;

public class ProcessContextQueryPerftestCompletionCondition implements ProcessCompletionCondition {

    private static final ProcessCompletionConditionResult COMPLETED = ProcessCompletionConditionResult.completedBuilder()
            .conclusion(ProcessCompletionConclusion.SUCCEEDED)
            .name("completed")
            .build();

    @Override
    public ProcessCompletionConditionResult isProcessCompleted(ProcessContext processContext) {
        // get message data
        processContext.getMessageDataForMessageType("JmeDocumentReviewedEvent");

        // contains
        processContext.containsMessageOfType("JmeDocumentReviewedEvent");
        processContext.containsMessageByTypeWithAnyMessageDataKeyValue("JmeDocumentReviewedEvent",
                Map.of("anyKey", Set.of("anyValue", "anotherValue")));
        processContext.containsMessageByTypeWithAnyMessageDataValue("JmeDocumentReviewedEvent",
                "anyKey", Set.of("anyValue", "anotherValue"));
        processContext.containsMessageByTypeWithMessageData("JmeDocumentReviewedEvent", "anyKey", "anyValue");

        // count
        processContext.countMessagesByType("JmeDocumentReviewedEvent");
        processContext.countMessagesByTypes(Set.of("JmeDocumentReviewedEvent"));
        processContext.countMessagesByTypeWithAnyMessageData("JmeDocumentReviewedEvent", Map.of("anyKey", "anyValue"));
        processContext.countMessagesByTypeWithMessageData("JmeDocumentReviewedEvent", "anyKey", "anyValue");

        // tasks
        boolean allTasksInFinalState = processContext.areAllTasksInFinalState();

        return allTasksInFinalState ? COMPLETED : ProcessCompletionConditionResult.IN_PROGRESS;
    }
}
