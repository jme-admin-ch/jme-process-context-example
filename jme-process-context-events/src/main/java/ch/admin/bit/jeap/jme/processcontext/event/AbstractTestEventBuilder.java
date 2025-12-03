package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.domainevent.avro.AvroDomainEvent;
import ch.admin.bit.jeap.domainevent.avro.AvroDomainEventBuilder;
import ch.admin.bit.jeap.messaging.model.MessageReferences;
import lombok.Getter;

import java.util.function.Supplier;

abstract class AbstractTestEventBuilder<BuilderType extends AvroDomainEventBuilder<BuilderType, EventType>, EventType extends AvroDomainEvent>
        extends AvroDomainEventBuilder<BuilderType, EventType> {

    private Supplier<MessageReferences> referencesSupplier;

    @Getter
    private final String processId;

    protected AbstractTestEventBuilder(
            Supplier<EventType> constructor,
            Supplier<MessageReferences> referencesSupplier,
            String processId) {
        super(constructor);
        this.referencesSupplier = referencesSupplier;
        this.processId = processId;
    }

    protected AbstractTestEventBuilder(
            Supplier<EventType> constructor,
            String processId) {
        super(constructor);
        this.processId = processId;
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
    @SuppressWarnings("unchecked")
    protected final BuilderType self() {
        return (BuilderType) this;
    }

    @Override
    public EventType build() {
        if (referencesSupplier != null) {
            setReferences(referencesSupplier.get());
        }
        setProcessId(processId);
        return super.build();
    }

}
