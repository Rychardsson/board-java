package br.com.dio.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Representa uma métrica de performance da aplicação
 */
@Data
@AllArgsConstructor
public class PerformanceMetric {
    private String operation;
    private long executionTimeMs;
    private LocalDateTime timestamp;
    private String details;
    
    public PerformanceMetric(String operation, long executionTimeMs) {
        this.operation = operation;
        this.executionTimeMs = executionTimeMs;
        this.timestamp = LocalDateTime.now();
        this.details = "";
    }
}
