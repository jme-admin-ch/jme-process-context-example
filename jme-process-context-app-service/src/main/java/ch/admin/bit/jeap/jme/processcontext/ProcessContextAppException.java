package ch.admin.bit.jeap.jme.processcontext;

import ch.admin.bit.jeap.messaging.avro.errorevent.MessageHandlerExceptionInformation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

@Getter
public class ProcessContextAppException extends RuntimeException implements MessageHandlerExceptionInformation {
    private static final String WRONG_STATE_CODE = "WRONG_STATE";
    private static final String PROCESS_NOT_FOUND = "PROCESS_NOT_FOUND";
    private static final String INVALID_INPUT = "INVALID_INPUT";
    private static final String CANNOT_SEND_EVENT = "CANNOT_SEND_EVENT";
    private static final String CANNOT_CREATE_PROCESS = "CANNOT_CREATE_PROCESS";

    private final String errorCode;
    private final Temporality temporality;
    private final String description;

    private ProcessContextAppException(String description, Temporality temporality, String errorCode, Throwable cause) {
        super(description, cause);
        this.temporality = temporality;
        this.errorCode = errorCode;
        this.description = description;
    }

    public static ProcessContextAppException notFound(String processId) {
        String message = String.format("Process '%s' does not (yet) exist", processId);
        return new ProcessContextAppException(message, Temporality.TEMPORARY, PROCESS_NOT_FOUND, null);
    }

    public static ProcessContextAppException invalidInput(String message) {
        return new ProcessContextAppException(message, Temporality.PERMANENT, INVALID_INPUT, null);
    }

    public static ProcessContextAppException cannotSendEvent(Throwable cause) {
        return new ProcessContextAppException("Cannot send Event", Temporality.TEMPORARY, CANNOT_SEND_EVENT, cause);
    }

    public static ProcessContextAppException alreadyPlanned(String processId) {
        String message = String.format("Tasks for process '%s' are already planned", processId);
        return new ProcessContextAppException(message, Temporality.TEMPORARY, PROCESS_NOT_FOUND, null);
    }

    public static ProcessContextAppException taskNotPlanned(String processId, int taskId) {
        String message = String.format("Tasks '%s' for process '%s' is not planned", taskId, processId);
        return new ProcessContextAppException(message, Temporality.TEMPORARY, PROCESS_NOT_FOUND, null);
    }

    public static ProcessContextAppException cannotCreateProcess(HttpStatus responseStatusCode) {
        String message = String.format("Cannot create new process on process context service, return %s", responseStatusCode);
        return new ProcessContextAppException(message, Temporality.TEMPORARY, CANNOT_CREATE_PROCESS, null);
    }

    @Override
    public String getStackTraceAsString() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        this.printStackTrace(pw);
        return sw.toString();
    }
}
