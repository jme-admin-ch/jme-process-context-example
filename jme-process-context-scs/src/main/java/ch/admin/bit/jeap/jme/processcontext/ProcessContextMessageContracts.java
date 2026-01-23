package ch.admin.bit.jeap.jme.processcontext;

import ch.admin.bit.jeap.messaging.annotations.JeapMessageConsumerContract;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageConsumerContractsByTemplates;
import ch.admin.bit.jeap.processcontext.command.process.instance.create.CreateProcessInstanceCommand;

@JeapMessageConsumerContractsByTemplates
@JeapMessageConsumerContract(value = CreateProcessInstanceCommand.TypeRef.class, topic = "jme-process-createprocessinstance")
class ProcessContextMessageContracts {
}
