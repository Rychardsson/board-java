package br.com.dio.metrics;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Relatório de métricas de performance
 */
@Getter
public class MetricsReport {
    
    private final LocalDateTime generatedAt;
    private final int totalOperations;
    private final double averageExecutionTime;
    private final long maxExecutionTime;
    private final long minExecutionTime;
    private final Map<String, DoubleSummaryStatistics> operationStats;
    private final List<PerformanceMetric> slowestOperations;
    
    public MetricsReport(Queue<PerformanceMetric> metrics) {
        this.generatedAt = LocalDateTime.now();
        
        List<PerformanceMetric> metricsList = List.copyOf(metrics);
        this.totalOperations = metricsList.size();
        
        if (metricsList.isEmpty()) {
            this.averageExecutionTime = 0;
            this.maxExecutionTime = 0;
            this.minExecutionTime = 0;
            this.operationStats = Map.of();
            this.slowestOperations = List.of();
            return;
        }
        
        DoubleSummaryStatistics globalStats = metricsList.stream()
            .mapToDouble(PerformanceMetric::getExecutionTimeMs)
            .summaryStatistics();
            
        this.averageExecutionTime = globalStats.getAverage();
        this.maxExecutionTime = (long) globalStats.getMax();
        this.minExecutionTime = (long) globalStats.getMin();
        
        this.operationStats = metricsList.stream()
            .collect(Collectors.groupingBy(
                PerformanceMetric::getOperation,
                Collectors.summarizingDouble(PerformanceMetric::getExecutionTimeMs)
            ));
            
        this.slowestOperations = metricsList.stream()
            .sorted((m1, m2) -> Long.compare(m2.getExecutionTimeMs(), m1.getExecutionTimeMs()))
            .limit(10)
            .collect(Collectors.toList());
    }
    
    public void printReport() {
        System.out.println("\n=== RELATÓRIO DE PERFORMANCE ===");
        System.out.println("Gerado em: " + generatedAt);
        System.out.println("Total de operações: " + totalOperations);
        System.out.printf("Tempo médio de execução: %.2f ms%n", averageExecutionTime);
        System.out.println("Tempo máximo: " + maxExecutionTime + " ms");
        System.out.println("Tempo mínimo: " + minExecutionTime + " ms");
        
        System.out.println("\n--- Estatísticas por Operação ---");
        operationStats.forEach((operation, stats) -> {
            System.out.printf("%s: Média=%.2f ms, Máx=%d ms, Mín=%d ms, Count=%d%n",
                operation, stats.getAverage(), (long)stats.getMax(), 
                (long)stats.getMin(), stats.getCount());
        });
        
        System.out.println("\n--- 10 Operações Mais Lentas ---");
        slowestOperations.forEach(metric -> {
            System.out.printf("%s: %d ms (%s)%n", 
                metric.getOperation(), metric.getExecutionTimeMs(), metric.getTimestamp());
        });
        
        System.out.println("================================\n");
    }
}
