package ch.admin.bit.jeap.jme.processcontext.condition;

import ch.admin.bit.jeap.jme.processcontext.event.race.prepared.JmeRacePreparedEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.event.ProcessInstantiationCondition;

@SuppressWarnings("unused")
public class RacePreparedProcessInstantiationCondition implements ProcessInstantiationCondition<JmeRacePreparedEvent> {

    @Override
    public boolean triggersProcessInstantiation(JmeRacePreparedEvent event) {
        // Don't create a process if the race car id is "test-car"
        return !"test-car".equals(event.getPayload().getRaceCarNumber());
    }

}
