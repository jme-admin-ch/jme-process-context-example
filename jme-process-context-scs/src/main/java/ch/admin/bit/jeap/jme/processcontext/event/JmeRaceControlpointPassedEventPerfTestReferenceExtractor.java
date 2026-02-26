package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.passed.JmeRaceControlpointPassedEventReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.ReferenceExtractor;

import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

public class JmeRaceControlpointPassedEventPerfTestReferenceExtractor implements ReferenceExtractor<JmeRaceControlpointPassedEventReferences> {

    @Override
    public Set<MessageData> getMessageData(JmeRaceControlpointPassedEventReferences references) {
        String controlpoint = references.getControlPoint().getControlPoint();
        return generateMessageData(controlpoint);
    }

    private Set<MessageData> generateMessageData(String controlpoint) {
        return IntStream.range(0, 10)
                .mapToObj(i -> MessageData.builder()
                        .key("controlpoint-" + i)
                        .value(controlpoint + "-" + i)
                        .build())
                .collect(toSet());
    }
}
