package ch.admin.bit.jeap.jme.processcontext.db;

import ch.admin.bit.jeap.jme.processcontext.web.StatisticDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGInterval;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Service meant primarily for load and performance tests
 */
@Component
@RequiredArgsConstructor
public class StatisticService {

    private final EntityManager entityManager;

    public StatisticDto countRows(){
        final Long count = (Long) entityManager.createNativeQuery("SELECT count(*) FROM process_instance").getSingleResult();
        return new StatisticDto(count.intValue());
    }

    public StatisticDto countCompleted(){
        final Long count = (Long) entityManager.createNativeQuery("SELECT count(*) FROM process_instance where state = 'COMPLETED'").getSingleResult();
        return new StatisticDto(count.intValue());
    }

    public StatisticDto countStarted(){
        final Long count = (Long) entityManager.createNativeQuery("SELECT count(*) FROM process_instance where state = 'STARTED'").getSingleResult();
        return new StatisticDto(count.intValue());
    }

    public int minDuration(){
        final String result = (String)entityManager.createNativeQuery("select cast(min(pi.modified_at-pi.created_at)as varchar) FROM process_instance pi").getSingleResult();
        return LocalTime.parse(result).toSecondOfDay();
    }

    public double maxDuration(){
        final PGInterval result = (PGInterval)entityManager.createNativeQuery("select max(pi.modified_at-pi.created_at)as varchar FROM process_instance pi").getSingleResult();
        return result.getSeconds() + result.getMinutes() * 60 + result.getHours() * 3600;
    }

    public double avgDuration(){
        final PGInterval result = (PGInterval)entityManager.createNativeQuery("select avg(pi.modified_at-pi.created_at)as varchar FROM process_instance pi").getSingleResult();
        return result.getSeconds() + result.getMinutes() * 60 + result.getHours() * 3600;
    }

    public double totalDuration(){
        final PGInterval result = (PGInterval)entityManager.createNativeQuery("select (max(pi.modified_at)-min(pi.created_at))as varchar FROM process_instance pi").getSingleResult();
        return result.getSeconds() + result.getMinutes() * 60 + result.getHours() * 3600;
    }

}
