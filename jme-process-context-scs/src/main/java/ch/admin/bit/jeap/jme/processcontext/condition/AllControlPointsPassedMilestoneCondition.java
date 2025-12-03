package ch.admin.bit.jeap.jme.processcontext.condition;

import ch.admin.bit.jeap.processcontext.plugin.api.condition.MilestoneCondition;
import ch.admin.bit.jeap.processcontext.plugin.api.context.ProcessContext;
import ch.admin.bit.jeap.processcontext.plugin.api.context.TaskState;

public class AllControlPointsPassedMilestoneCondition implements MilestoneCondition {

    private static final String CONTROL_POINT_PASSED = "passRaceControlpoint";

    @Override
    public boolean isMilestoneReached(ProcessContext processContext) {
        return processContext.isTasksInState(CONTROL_POINT_PASSED, TaskState.COMPLETED);
    }
}
