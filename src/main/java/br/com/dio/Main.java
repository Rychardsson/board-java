package br.com.dio;

import br.com.dio.config.ApplicationConfig;
import br.com.dio.persistence.migration.MigrationStrategy;
import br.com.dio.ui.MainMenu;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

/**
 * Classe principal da aplicação Board de Tarefas
 * Sistema de gerenciamento de boards estilo Kanban
 */
@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("=== INICIANDO APLICAÇÃO BOARD DE TAREFAS ===");
        
        try {
            // Carrega configurações
            ApplicationConfig config = ApplicationConfig.getInstance();
            log.info("Configurações carregadas: ambiente={}", config.getLogLevel());
            
            // Executa migrações do banco
            log.info("Executando migrações do banco de dados...");
            executeMigrations();
            log.info("Migrações executadas com sucesso");
            
            // Inicia interface do usuário
            log.info("Iniciando interface do usuário");
            new MainMenu().execute();
            
        } catch (SQLException e) {
            log.error("Erro de conexão com banco de dados", e);
            System.err.println("❌ Erro de conexão com banco de dados: " + e.getMessage());
            System.err.println("Verifique se o MySQL está rodando e as credenciais estão corretas.");
            System.exit(1);
            
        } catch (Exception e) {
            log.error("Erro inesperado na aplicação", e);
            System.err.println("❌ Erro inesperado: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void executeMigrations() throws SQLException {
        try (var connection = getConnection()) {
            new MigrationStrategy(connection).executeMigration();
            connection.commit();
        }
    }
}
