-- Migração adicional para melhorias na estrutura do banco
-- Autor: Rychardsson
-- Data: 2025-01-31

-- Adiciona campos de auditoria nas tabelas principais
ALTER TABLE BOARDS 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

ALTER TABLE BOARDS_COLUMNS 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE CARDS 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
ADD COLUMN IF NOT EXISTS due_date DATE,
ADD COLUMN IF NOT EXISTS estimated_hours DECIMAL(5,2),
ADD COLUMN IF NOT EXISTS actual_hours DECIMAL(5,2);

-- Adiciona índices para performance
CREATE INDEX IF NOT EXISTS idx_cards_board_column ON CARDS(board_column_id);
CREATE INDEX IF NOT EXISTS idx_cards_created_at ON CARDS(created_at);
CREATE INDEX IF NOT EXISTS idx_cards_priority ON CARDS(priority);
CREATE INDEX IF NOT EXISTS idx_cards_due_date ON CARDS(due_date);

CREATE INDEX IF NOT EXISTS idx_boards_name ON BOARDS(name);
CREATE INDEX IF NOT EXISTS idx_boards_created_at ON BOARDS(created_at);

CREATE INDEX IF NOT EXISTS idx_boards_columns_board_id ON BOARDS_COLUMNS(board_id);
CREATE INDEX IF NOT EXISTS idx_boards_columns_order ON BOARDS_COLUMNS(order_position);

CREATE INDEX IF NOT EXISTS idx_blocks_card_id ON BLOCKS(card_id);
CREATE INDEX IF NOT EXISTS idx_blocks_blocked_at ON BLOCKS(blocked_at);

-- Cria tabela para histórico de movimentações de cards
CREATE TABLE IF NOT EXISTS CARD_MOVEMENTS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    from_column_id BIGINT,
    to_column_id BIGINT NOT NULL,
    moved_by VARCHAR(100),
    moved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    
    FOREIGN KEY (card_id) REFERENCES CARDS(id) ON DELETE CASCADE,
    FOREIGN KEY (from_column_id) REFERENCES BOARDS_COLUMNS(id),
    FOREIGN KEY (to_column_id) REFERENCES BOARDS_COLUMNS(id),
    
    INDEX idx_card_movements_card_id (card_id),
    INDEX idx_card_movements_moved_at (moved_at)
);

-- Cria tabela para métricas de performance
CREATE TABLE IF NOT EXISTS PERFORMANCE_METRICS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_name VARCHAR(100) NOT NULL,
    execution_time_ms BIGINT NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    
    INDEX idx_performance_metrics_operation (operation_name),
    INDEX idx_performance_metrics_executed_at (executed_at),
    INDEX idx_performance_metrics_execution_time (execution_time_ms)
);

-- Cria tabela para configurações da aplicação
CREATE TABLE IF NOT EXISTS APPLICATION_SETTINGS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_application_settings_key (setting_key)
);

-- Insere configurações padrão
INSERT IGNORE INTO APPLICATION_SETTINGS (setting_key, setting_value, description) VALUES
('app.version', '1.0.0', 'Versão atual da aplicação'),
('app.maintenance_mode', 'false', 'Modo de manutenção ativo'),
('metrics.retention_days', '30', 'Dias de retenção de métricas de performance'),
('backup.enabled', 'true', 'Backup automático habilitado'),
('notification.enabled', 'false', 'Sistema de notificações habilitado');

-- Cria view para relatórios de produtividade
CREATE OR REPLACE VIEW BOARD_PRODUCTIVITY_VIEW AS
SELECT 
    b.id as board_id,
    b.name as board_name,
    bc.id as column_id,
    bc.name as column_name,
    bc.kind as column_kind,
    COUNT(c.id) as total_cards,
    COUNT(CASE WHEN bl.id IS NOT NULL THEN 1 END) as blocked_cards,
    AVG(c.estimated_hours) as avg_estimated_hours,
    AVG(c.actual_hours) as avg_actual_hours,
    COUNT(CASE WHEN c.due_date < CURDATE() THEN 1 END) as overdue_cards
FROM BOARDS b
LEFT JOIN BOARDS_COLUMNS bc ON b.id = bc.board_id
LEFT JOIN CARDS c ON bc.id = c.board_column_id
LEFT JOIN BLOCKS bl ON c.id = bl.card_id AND bl.unblocked_at IS NULL
GROUP BY b.id, b.name, bc.id, bc.name, bc.kind
ORDER BY b.name, bc.order_position;

-- Cria view para cards em atraso
CREATE OR REPLACE VIEW OVERDUE_CARDS_VIEW AS
SELECT 
    c.id,
    c.title,
    c.description,
    c.due_date,
    DATEDIFF(CURDATE(), c.due_date) as days_overdue,
    c.priority,
    bc.name as column_name,
    b.name as board_name,
    CASE 
        WHEN bl.id IS NOT NULL THEN 'BLOCKED'
        ELSE 'ACTIVE'
    END as status
FROM CARDS c
INNER JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id
INNER JOIN BOARDS b ON bc.board_id = b.id
LEFT JOIN BLOCKS bl ON c.id = bl.card_id AND bl.unblocked_at IS NULL
WHERE c.due_date IS NOT NULL 
  AND c.due_date < CURDATE()
  AND bc.kind NOT IN ('FINAL', 'CANCEL')
ORDER BY c.due_date ASC, c.priority DESC;

-- Cria view para histórico de movimentações
CREATE OR REPLACE VIEW CARD_MOVEMENT_HISTORY_VIEW AS
SELECT 
    cm.id,
    c.title as card_title,
    b.name as board_name,
    bc_from.name as from_column,
    bc_to.name as to_column,
    cm.moved_by,
    cm.moved_at,
    cm.notes,
    TIMESTAMPDIFF(HOUR, 
        LAG(cm.moved_at) OVER (PARTITION BY cm.card_id ORDER BY cm.moved_at), 
        cm.moved_at
    ) as hours_in_previous_column
FROM CARD_MOVEMENTS cm
INNER JOIN CARDS c ON cm.card_id = c.id
INNER JOIN BOARDS_COLUMNS bc_to ON cm.to_column_id = bc_to.id
LEFT JOIN BOARDS_COLUMNS bc_from ON cm.from_column_id = bc_from.id
INNER JOIN BOARDS b ON bc_to.board_id = b.id
ORDER BY cm.moved_at DESC;

-- Adiciona comentários às tabelas para documentação
ALTER TABLE BOARDS COMMENT = 'Tabela principal dos boards/quadros de tarefas';
ALTER TABLE BOARDS_COLUMNS COMMENT = 'Colunas que compõem cada board (ex: To Do, Doing, Done)';
ALTER TABLE CARDS COMMENT = 'Cards/tarefas individuais dentro das colunas';
ALTER TABLE BLOCKS COMMENT = 'Registro de bloqueios de cards com motivos';
ALTER TABLE CARD_MOVEMENTS COMMENT = 'Histórico de movimentações de cards entre colunas';
ALTER TABLE PERFORMANCE_METRICS COMMENT = 'Métricas de performance das operações do sistema';
ALTER TABLE APPLICATION_SETTINGS COMMENT = 'Configurações gerais da aplicação';
