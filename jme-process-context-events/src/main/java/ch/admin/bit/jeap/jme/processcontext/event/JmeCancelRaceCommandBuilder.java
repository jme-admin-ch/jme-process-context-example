package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.command.avro.AvroCommandBuilder;
import ch.admin.bit.jeap.jme.processcontext.command.race.cancel.JmeCancelRaceCommand;
import ch.admin.bit.jeap.jme.processcontext.command.race.cancel.JmeCancelRaceCommandReferences;
import ch.admin.bit.jeap.jme.processcontext.command.race.cancel.JmeRaceReference;

public class JmeCancelRaceCommandBuilder extends AvroCommandBuilder<JmeCancelRaceCommandBuilder, JmeCancelRaceCommand> {

    private String raceId;

    private JmeCancelRaceCommandBuilder(String processId) {
        super(JmeCancelRaceCommand::new);
        setProcessId(processId);
    }

    public static JmeCancelRaceCommandBuilder builder(String processId) {
        return new JmeCancelRaceCommandBuilder(processId);
    }

    public JmeCancelRaceCommandBuilder raceId(String raceId) {
        this.raceId = raceId;
        return this;
    }

    @Override
    protected final String getServiceName() {
        return "jme-process-context-app-service";
    }

    @Override
    protected final String getSystemName() {
        return "JME";
    }

    @Override
    protected JmeCancelRaceCommandBuilder self() {
        return this;

    }

    @Override
    public JmeCancelRaceCommand build() {
        setReferences(new JmeCancelRaceCommandReferences(new JmeRaceReference("race", raceId)));
        return super.build();
    }
}
