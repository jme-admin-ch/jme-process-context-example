package ch.admin.bit.jeap.jme.processcontext.perftest.report;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HtmlTestReporter {

    private static final TemplateEngine TEMPLATE_ENGINE = createTemplateEngine();

    public static String generateReport(TestReport testReport) {
        Context context = new Context();
        context.setVariable("status", testReport.getStatus().name());
        context.setVariable("scenario", testReport.getScenario());
        context.setVariable("warmUpStartedAt", formatDateTime(testReport.getWarmUpStartedAt()));
        context.setVariable("startedAt", formatDateTime(testReport.getStartedAt()));
        context.setVariable("finishedAt", formatDateTime(testReport.getFinishedAt()));
        context.setVariable("warmUpDuration", formatDurationSeconds(testReport.getWarmUpDuration()));
        context.setVariable("totalRunDuration", formatDurationSeconds(testReport.getTotalRunDuration()));
        context.setVariable("processInstancesCreated", testReport.getProcessInstancesCreated());
        context.setVariable("processInstancesCompleted", testReport.getProcessInstancesCompleted());
        context.setVariable("messageThroughput", formatThroughput(testReport.getMessageThroughput(), "msg/sec"));
        context.setVariable("creationThroughput", formatThroughput(testReport.getCreationThroughput(), "processes/sec"));
        context.setVariable("totalMessages", totalMsgs(testReport));
        context.setVariable("parameters", toParameterEntries(testReport.getParameters()));
        setStatsVariables(context, "creation", testReport.getCreationStats());
        setStatsVariables(context, "completion", testReport.getCompletionStats());
        TimeSeries ts = testReport.getStatsTimeSeries();
        context.setVariable("creationTimeSeriesJson", formatCountTimeSeries(ts, Bucket::getCreated));
        context.setVariable("completionTimeSeriesJson", formatCountTimeSeries(ts, Bucket::getCompleted));
        context.setVariable("creationDurationTimeSeriesJson", formatDurationTimeSeries(ts, Bucket::getCreationDuration));
        context.setVariable("completionDurationTimeSeriesJson", formatDurationTimeSeries(ts, Bucket::getCompletionDuration));
        context.setVariable("failureMessage", testReport.getFailureMessage());
        context.setVariable("messages", testReport.getMessages());
        return TEMPLATE_ENGINE.process("report/perftests-report.template", context);
    }

    private static List<Map.Entry<String, Object>> toParameterEntries(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return List.of();
        }
        return parameters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();
    }

    private static String totalMsgs(TestReport testReport) {
        double messagesPerProcess = (double) testReport.getTotalMessages() / testReport.getProcessInstancesCreated();
        return "%d (%s per process)".formatted(testReport.getTotalMessages(), messagesPerProcess);
    }

    private static void setStatsVariables(Context context, String prefix, Stats stats) {
        if (stats == null) {
            context.setVariable(prefix + "Min", "-");
            context.setVariable(prefix + "Max", "-");
            context.setVariable(prefix + "Avg", "-");
            context.setVariable(prefix + "Median", "-");
            return;
        }
        context.setVariable(prefix + "Min", formatDurationMs(stats.minDuration()));
        context.setVariable(prefix + "Max", formatDurationMs(stats.maxDuration()));
        context.setVariable(prefix + "Avg", formatDurationMs(stats.avgDuration()));
        context.setVariable(prefix + "Median", formatDurationMs(stats.medianDuration()));
    }

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMAT) : "-";
    }

    private static String formatDurationSeconds(Duration duration) {
        return duration != null ? secsAndMsecs(duration) : "-";
    }

    private static String secsAndMsecs(Duration duration) {
        return "%d s (%d ms)".formatted(duration.toSeconds(), duration.toMillis());
    }

    private static String formatThroughput(double throughput, String unit) {
        return "%.2f %s".formatted(throughput, unit);
    }

    private static String formatDurationMs(Duration duration) {
        return duration != null ? duration.toMillis() + " ms" : "-";
    }

    private static String formatCountTimeSeries(TimeSeries timeSeries, Function<Bucket, Long> valueExtractor) {
        if (timeSeries == null) {
            return "[]";
        }
        return timeSeries.buckets().stream()
                .map(b -> "{\"ts\":\"%s\",\"value\":%d}".formatted(
                        b.getTs().toString(), valueExtractor.apply(b)))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String formatDurationTimeSeries(TimeSeries timeSeries, Function<Bucket, DurationAggregate> extractor) {
        if (timeSeries == null) {
            return "[]";
        }
        return timeSeries.buckets().stream()
                .map(b -> {
                    DurationAggregate d = extractor.apply(b);
                    return "{\"ts\":\"%s\",\"min\":%d,\"avg\":%d,\"max\":%d}".formatted(
                            b.getTs().toString(), d.getMin(), d.getAvg(), d.getMax());
                })
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
