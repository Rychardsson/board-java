package br.com.dio.metrics;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Coletor de métricas de performance
 */
@Slf4j
public class MetricsCollector {
    
    private static final MetricsCollector INSTANCE = new MetricsCollector();
    private final ConcurrentLinkedQueue<PerformanceMetric> metrics = new ConcurrentLinkedQueue<>();
    
    private MetricsCollector() {}
    
    public static MetricsCollector getInstance() {
        return INSTANCE;
    }
    
    /**
     * Executa uma operação medindo seu tempo de execução
     */
    public <T> T measureOperation(String operationName, Supplier<T> operation) {
        long startTime = System.currentTimeMillis();
        try {
            T result = operation.get();
            long executionTime = System.currentTimeMillis() - startTime;
            recordMetric(operationName, executionTime);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            recordMetric(operationName + " (ERROR)", executionTime, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Executa uma operação sem retorno medindo seu tempo de execução
     */
    public void measureOperation(String operationName, Runnable operation) {
        measureOperation(operationName, () -> {
            operation.run();
            return null;
        });
    }
    
    private void recordMetric(String operation, long executionTime) {
        recordMetric(operation, executionTime, "");
    }
    
    private void recordMetric(String operation, long executionTime, String details) {
        PerformanceMetric metric = new PerformanceMetric(operation, executionTime, 
            java.time.LocalDateTime.now(), details);
        metrics.offer(metric);
        
        // Log da métrica
        if (executionTime > 1000) { // Log operações que demoram mais de 1 segundo
            log.warn("Operação lenta detectada: {} - {}ms", operation, executionTime);
        } else {
            log.debug("Métrica registrada: {} - {}ms", operation, executionTime);
        }
        
        // Limita o número de métricas em memória
        while (metrics.size() > 1000) {
            metrics.poll();
        }
    }
    
    /**
     * Retorna estatísticas das métricas coletadas
     */
    public MetricsReport generateReport() {
        return new MetricsReport(metrics);
    }
}
