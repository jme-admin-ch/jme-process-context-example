package ch.admin.bit.jeap.processcontext;

import ch.admin.bit.jeap.processcontext.plugin.api.relation.Relation;
import ch.admin.bit.jeap.processcontext.plugin.api.relation.RelationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("local")
@RestController
@RequestMapping("/api/example-relation-notifications")
@Slf4j
public class ExampleRelationListener implements RelationListener {

    private final Map<UUID, AtomicInteger> notificationCounts = new ConcurrentHashMap<>();

    @Override
    public void relationsAdded(Collection<Relation> relations) {
        relations.forEach(relation -> {
            notificationCounts.computeIfAbsent(relation.getIdempotenceId(), ignored -> new AtomicInteger())
                    .incrementAndGet();
            log.info("Handled relation notification for process {} and relation idempotence ID {}",
                    relation.getOriginProcessId(), relation.getIdempotenceId());
        });
    }

    @GetMapping("/{idempotenceId}")
    @PreAuthorize("hasRole('processinstance', 'view')")
    public int notificationCount(@PathVariable UUID idempotenceId) {
        return notificationCounts.getOrDefault(idempotenceId, new AtomicInteger()).get();
    }
}
