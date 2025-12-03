package ch.admin.bit.jeap.jme.processcontext.command;

import ch.admin.bit.jeap.jme.processcontext.command.race.cancel.JmeCancelRaceCommandReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.event.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.ReferenceExtractor;

import java.util.Set;

public class JmeCancelRaceCommandReferenceExtractor implements ReferenceExtractor<JmeCancelRaceCommandReferences> {
    @Override
    public Set<MessageData> getMessageData(JmeCancelRaceCommandReferences references) {
        return Set.of(MessageData.builder()
                .key("race-id")
                .value(references.getRaceReference().getRaceId())
                .build());
    }
}
