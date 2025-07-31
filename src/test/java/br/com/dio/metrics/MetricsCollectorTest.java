package br.com.dio.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para coleta de métricas
 */
class MetricsCollectorTest {

    private MetricsCollector metricsCollector;
    
    @BeforeEach
    void setUp() {
        metricsCollector = MetricsCollector.getInstance();
    }
    
    @Test
    void shouldMeasureOperationWithReturnValue() {
        // Given
        String expectedResult = "test result";
        
        // When
        String result = metricsCollector.measureOperation("test_operation", () -> {
            simulateWork(100); // Simula 100ms de trabalho
            return expectedResult;
        });
        
        // Then
        assertThat(result).isEqualTo(expectedResult);
        
        MetricsReport report = metricsCollector.generateReport();
        assertThat(report.getTotalOperations()).isGreaterThan(0);
        assertThat(report.getOperationStats()).containsKey("test_operation");
    }
    
    @Test
    void shouldMeasureOperationWithoutReturnValue() {
        // Given
        boolean[] executed = {false};
        
        // When
        metricsCollector.measureOperation("void_operation", () -> {
            simulateWork(50);
            executed[0] = true;
        });
        
        // Then
        assertThat(executed[0]).isTrue();
        
        MetricsReport report = metricsCollector.generateReport();
        assertThat(report.getOperationStats()).containsKey("void_operation");
    }
    
    @Test
    void shouldCaptureExceptionInMetrics() {
        // Given
        RuntimeException expectedException = new RuntimeException("Test exception");
        
        // When & Then
        assertThatThrownBy(() -> 
            metricsCollector.measureOperation("failing_operation", () -> {
                simulateWork(30);
                throw expectedException;
            })
        ).isEqualTo(expectedException);
        
        MetricsReport report = metricsCollector.generateReport();
        assertThat(report.getOperationStats()).containsKey("failing_operation (ERROR)");
    }
    
    @Test
    void shouldGenerateValidReport() {
        // Given - primeira limpar métricas existentes verificando se há um método de reset
        // Como MetricsCollector é singleton, pode ter métricas de outros testes
        
        // Executa algumas operações para o teste
        metricsCollector.measureOperation("op1", () -> simulateWork(100));
        metricsCollector.measureOperation("op2", () -> simulateWork(200));
        metricsCollector.measureOperation("op1", () -> simulateWork(150));
        
        // When
        MetricsReport report = metricsCollector.generateReport();
        
        // Then - usa valores mais flexíveis devido ao singleton
        assertThat(report.getTotalOperations()).isGreaterThanOrEqualTo(3);
        assertThat(report.getOperationStats()).containsKeys("op1", "op2");
        assertThat(report.getOperationStats().get("op1").getCount()).isGreaterThanOrEqualTo(2);
        assertThat(report.getOperationStats().get("op2").getCount()).isGreaterThanOrEqualTo(1);
        assertThat(report.getAverageExecutionTime()).isGreaterThan(0);
    }
    
    private void simulateWork(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
