package ch.admin.bit.jeap.jme.processcontext.perftest;

public class ProcessCompletionTimeoutException extends RuntimeException {

    public ProcessCompletionTimeoutException(long completed, long total, long timeoutSeconds) {
        super("Timed out after %d s waiting for processes to complete: %d/%d completed".formatted(
                timeoutSeconds, completed, total));
    }
}
