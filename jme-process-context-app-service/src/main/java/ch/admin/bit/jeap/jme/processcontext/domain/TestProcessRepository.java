package ch.admin.bit.jeap.jme.processcontext.domain;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TestProcessRepository {
    //Poor mans in-memory DB
    private final Map<String, Process> testProcesses = new ConcurrentHashMap<>();

    public <T> Optional<T> findById(String id, Class<T> tClass) {
        return Optional.ofNullable(testProcesses.get(id)).map(tClass::cast);
    }

    public void save(Process testProcess) {
        testProcesses.put(testProcess.getId(), testProcess);
    }
}
