package ch.admin.bit.jeap.jme.processcontext.command;

import ch.admin.bit.jeap.jme.processcontext.command.race.cancel.JmeCancelRaceCommandReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.ReferenceExtractor;

import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

public class JmeCancelRaceCommandPerfTestReferenceExtractor implements ReferenceExtractor<JmeCancelRaceCommandReferences> {

    @Override
    public Set<MessageData> getMessageData(JmeCancelRaceCommandReferences references) {
        return generateMessageData(references.getRaceReference().getRaceId());
    }

    private Set<MessageData> generateMessageData(String originProcessIdPrefix) {
        return IntStream.range(0, 1000)
                .mapToObj(i -> MessageData.builder()
                        .key("relatedProcessId")
                        .value(originProcessIdPrefix + i)
                        .build())
                .collect(toSet());
    }
}
