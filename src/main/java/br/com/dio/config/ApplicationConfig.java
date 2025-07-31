package br.com.dio.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuração centralizada da aplicação
 * Carrega propriedades de configuração de diferentes fontes
 */
@Slf4j
@Getter
public class ApplicationConfig {
    
    private static ApplicationConfig instance;
    
    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;
    private final int maxConnectionPoolSize;
    private final boolean enableQueryLogging;
    private final String logLevel;
    
    private ApplicationConfig() {
        Properties props = loadProperties();
        
        this.databaseUrl = props.getProperty("database.url", "jdbc:mysql://localhost/board");
        this.databaseUser = props.getProperty("database.user", "board");
        this.databasePassword = props.getProperty("database.password", "board");
        this.maxConnectionPoolSize = Integer.parseInt(props.getProperty("database.pool.max", "10"));
        this.enableQueryLogging = Boolean.parseBoolean(props.getProperty("database.query.logging", "false"));
        this.logLevel = props.getProperty("logging.level", "INFO");
        
        log.info("Configuração da aplicação carregada com sucesso");
    }
    
    public static ApplicationConfig getInstance() {
        if (instance == null) {
            synchronized (ApplicationConfig.class) {
                if (instance == null) {
                    instance = new ApplicationConfig();
                }
            }
        }
        return instance;
    }
    
    private Properties loadProperties() {
        Properties props = new Properties();
        
        // Primeiro tenta carregar do arquivo de properties
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
                log.debug("Propriedades carregadas do arquivo application.properties");
            }
        } catch (IOException e) {
            log.warn("Não foi possível carregar application.properties, usando valores padrão", e);
        }
        
        // Depois sobrescreve com variáveis de ambiente se existirem
        System.getenv().forEach((key, value) -> {
            if (key.startsWith("BOARD_")) {
                String propKey = key.toLowerCase().replace("board_", "").replace("_", ".");
                props.setProperty(propKey, value);
                log.debug("Propriedade {} sobrescrita por variável de ambiente", propKey);
            }
        });
        
        return props;
    }
}
