package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.destination.reached.JmeRaceDestinationReachedEventReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.event.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.ReferenceExtractor;

import java.util.HashSet;
import java.util.Set;

public class JmeRaceDestinationReachedEventReferenceExtractor implements ReferenceExtractor<JmeRaceDestinationReachedEventReferences> {

    @Override
    public Set<MessageData> getMessageData(JmeRaceDestinationReachedEventReferences references) {
        Set<MessageData> messageData = new HashSet<>();
        String processId = references.getProcessReference().getProcessId();
        messageData.add(MessageData.builder()
                .key("relatedProcessId")
                .value(processId)
                .build());
        if (references.getParkingReference() != null) {
            int parkingSpotNumber = references.getParkingReference().getParkingSpotNumber();
            messageData.add(MessageData.builder()
                    .key("parkingSpotNumber")
                    .value(Integer.toString(parkingSpotNumber))
                    .build());
        }
        return messageData;
    }
}
