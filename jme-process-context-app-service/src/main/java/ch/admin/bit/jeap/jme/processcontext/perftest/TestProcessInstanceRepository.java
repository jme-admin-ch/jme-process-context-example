package ch.admin.bit.jeap.jme.processcontext.perftest;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
@Slf4j
class TestProcessInstanceRepository {

    private static final int BATCH_SIZE = 100;
    private static final int COMPLETION_CHECK_RATE_MILLIS = 2000;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @SuppressWarnings("BusyWait")
    @SneakyThrows
    public void waitUntilAllProcessesCompleted(List<TestProcessInstance> processInstances, Duration timeout) {
        List<String> originProcessIds = processInstances.stream()
                .map(TestProcessInstance::getOriginProcessId)
                .toList();

        Instant deadline = Instant.now().plus(timeout);
        long totalCompleted = 0;
        for (int i = 0; i < originProcessIds.size(); i += BATCH_SIZE) {
            List<String> batch = originProcessIds.subList(i, Math.min(i + BATCH_SIZE, originProcessIds.size()));
            long batchCompleted = 0;
            while (batchCompleted < batch.size()) {
                if (Instant.now().isAfter(deadline)) {
                    throw new ProcessCompletionTimeoutException(
                            totalCompleted + batchCompleted, originProcessIds.size(), timeout.toSeconds());
                }
                log.info("Waiting for processes to complete... {}/{}", totalCompleted + batchCompleted, originProcessIds.size());
                batchCompleted = countCompletedProcesses(batch);
                if (batchCompleted < batch.size()) {
                    Thread.sleep(COMPLETION_CHECK_RATE_MILLIS);
                }
            }
            totalCompleted += batchCompleted;
        }
        log.info("All processes completed successfully");
    }

    public void collectProcessState(List<TestProcessInstance> testProcessInstances) {
        Map<String, TestProcessInstance> byOriginProcessId = testProcessInstances.stream()
                .collect(Collectors.toMap(TestProcessInstance::getOriginProcessId, Function.identity()));

        List<String> originProcessIds = List.copyOf(byOriginProcessId.keySet());

        for (int i = 0; i < originProcessIds.size(); i += BATCH_SIZE) {
            List<String> originProcessIdBatch = originProcessIds.subList(i, Math.min(i + BATCH_SIZE, originProcessIds.size()));
            collectProcessState(originProcessIdBatch, byOriginProcessId);
        }
    }

    private void collectProcessState(List<String> batch, Map<String, TestProcessInstance> byOriginProcessId) {
        jdbcTemplate.query(
                "select pi.origin_process_id, pi.created_at, pi.modified_at, pi.state from process_instance pi where pi.origin_process_id in (:ids)",
                new MapSqlParameterSource("ids", batch),
                (rs) -> {
                    String originProcessId = rs.getString("origin_process_id");
                    TestProcessInstance testProcessInstance = byOriginProcessId.get(originProcessId);
                    if (testProcessInstance == null) {
                        throw new IllegalStateException("No matching process instance found for origin process id: " + originProcessId);
                    }
                    testProcessInstance.createdAt(rs.getTimestamp("created_at").toLocalDateTime());
                    String state = rs.getString("state");
                    testProcessInstance.state(state);
                    if ("COMPLETED".equals(state)) {
                        testProcessInstance.completed(rs.getTimestamp("modified_at").toLocalDateTime());
                    }
                }
        );
    }

    private Long countCompletedProcesses(List<String> originProcessIds) {
        return jdbcTemplate.queryForObject(
                "select count(*) from process_instance pi where pi.origin_process_id in (:ids) and pi.state = 'COMPLETED'",
                new MapSqlParameterSource("ids", originProcessIds),
                Long.class
        );
    }

    public void clearDatabase() {
        log.info("Clearing PCS database");
        jdbcTemplate.getJdbcTemplate().execute("""
                TRUNCATE
                    event_reference,
                    process_instance_process_data,
                    process_instance_process_relations,
                    process_instance_relations,
                    task_instance,
                    process_instance,
                    pending_message,
                    events_event_data,
                    events
                """);
        jdbcTemplate.getJdbcTemplate().execute("VACUUM");
    }

    public int getTotalRelationCount(List<TestProcessInstance> processInstances) {
        List<String> originProcessIds = processInstances.stream()
                .map(TestProcessInstance::getOriginProcessId)
                .collect(toList());

        int totalCount = 0;
        for (int i = 0; i < originProcessIds.size(); i += BATCH_SIZE) {
            List<String> originProcessIdBatch = originProcessIds.subList(i, Math.min(i + BATCH_SIZE, originProcessIds.size()));
            totalCount += countRelations(originProcessIdBatch);
        }
        return totalCount;
    }

    @SuppressWarnings("DataFlowIssue")
    private int countRelations(List<String> originProcessIds) {
        return jdbcTemplate.query(
                "select count(*) as counter from process_instance pi where pi.origin_process_id in (:ids)",
                new MapSqlParameterSource("ids", originProcessIds),
                (rs) -> {
                    rs.next();
                    return rs.getInt("counter");
                }
        );
    }

}
