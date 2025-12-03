package ch.admin.bit.jeap.jme.processcontext.monitoring;

import ch.admin.bit.jeap.jme.processcontext.db.StatisticService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorConfig {
 
    @Bean
    StatisticMeter processInstancesStatisticMeter(StatisticService statisticService) {
        return new StatisticMeter(statisticService);
    }
}
