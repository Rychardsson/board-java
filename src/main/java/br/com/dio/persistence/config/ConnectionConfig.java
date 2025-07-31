package br.com.dio.persistence.config;

import br.com.dio.config.ApplicationConfig;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static lombok.AccessLevel.PRIVATE;

/**
 * Configuração de conexão com o banco de dados
 * Utiliza configurações centralizadas da aplicação
 */
@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class ConnectionConfig {

    private static final ApplicationConfig config = ApplicationConfig.getInstance();

    public static Connection getConnection() throws SQLException {
        try {
            var connection = DriverManager.getConnection(
                config.getDatabaseUrl(),
                config.getDatabaseUser(),
                config.getDatabasePassword()
            );
            connection.setAutoCommit(false);
            
            if (config.isEnableQueryLogging()) {
                log.debug("Conexão estabelecida com o banco de dados: {}", config.getDatabaseUrl());
            }
            
            return connection;
        } catch (SQLException e) {
            log.error("Erro ao estabelecer conexão com o banco de dados: {}", e.getMessage(), e);
            throw e;
        }
    }

}
