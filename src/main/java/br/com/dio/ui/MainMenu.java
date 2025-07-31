package br.com.dio.ui;

import br.com.dio.exception.ValidationException;
import br.com.dio.metrics.MetricsCollector;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;
import br.com.dio.service.ReportService;
import br.com.dio.validation.EntityValidator;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.FINAL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.INITIAL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.PENDING;

@Slf4j
public class MainMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");
    private final MetricsCollector metricsCollector = MetricsCollector.getInstance();

    public void execute() throws SQLException {
        log.info("Sistema de gerenciamento de boards iniciado");
        System.out.println("=== BEM-VINDO AO GERENCIADOR DE BOARDS ===");
        System.out.println("Versão 1.0.0 - Sistema de Kanban");
        
        var option = -1;
        while (true){
            try {
                showMainMenu();
                option = readIntegerInput("Escolha uma opção: ");
                
                switch (option){
                    case 1 -> createBoard();
                    case 2 -> selectBoard();
                    case 3 -> deleteBoard();
                    case 4 -> listAllBoards();
                    case 5 -> showReports();
                    case 6 -> showMetrics();
                    case 7 -> {
                        System.out.println("Encerrando aplicação...");
                        log.info("Aplicação encerrada pelo usuário");
                        System.exit(0);
                    }
                    default -> System.out.println("❌ Opcao invalida! Escolha uma opcao entre 1 e 7.");
                }
            } catch (ValidationException e) {
                System.out.println("❌ Erro de validacao: " + e.getMessage());
                log.warn("Erro de validação na interface: {}", e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Erro inesperado: " + e.getMessage());
                log.error("Erro inesperado na interface principal", e);
            }
        }
    }
    
    private void showMainMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("           MENU PRINCIPAL");
        System.out.println("=".repeat(40));
        System.out.println("1 📝 Criar um novo board");
        System.out.println("2 📋 Selecionar um board existente");
        System.out.println("3 🗑 Excluir um board");
        System.out.println("4 📊 Listar todos os boards");
        System.out.println("5 📈 Relatorios");
        System.out.println("6 ⚡ Metricas de performance");
        System.out.println("7 🚪 Sair");
        System.out.println("=".repeat(40));
    }

    private void createBoard() throws SQLException {
        System.out.println("\n📝 CRIANDO NOVO BOARD");
        System.out.println("-".repeat(30));
        
        return metricsCollector.measureOperation("create_board", () -> {
            try {
                var entity = new BoardEntity();
                
                String boardName = readStringInput("Digite o nome do board (2-100 caracteres): ");
                entity.setName(boardName);

                int additionalColumns = readIntegerInput("Quantas colunas adicionais além das 3 padrões? (0 para nenhuma): ");
                if (additionalColumns < 0 || additionalColumns > 10) {
                    throw new ValidationException("Número de colunas adicionais deve estar entre 0 e 10");
                }

                List<BoardColumnEntity> columns = new ArrayList<>();

                String initialColumnName = readStringInput("Nome da coluna inicial: ");
                var initialColumn = createColumn(initialColumnName, INITIAL, 0);
                columns.add(initialColumn);

                for (int i = 0; i < additionalColumns; i++) {
                    String pendingColumnName = readStringInput("Nome da coluna de tarefa pendente " + (i + 1) + ": ");
                    var pendingColumn = createColumn(pendingColumnName, PENDING, i + 1);
                    columns.add(pendingColumn);
                }

                String finalColumnName = readStringInput("Nome da coluna final: ");
                var finalColumn = createColumn(finalColumnName, FINAL, additionalColumns + 1);
                columns.add(finalColumn);

                String cancelColumnName = readStringInput("Nome da coluna de cancelamento: ");
                var cancelColumn = createColumn(cancelColumnName, CANCEL, additionalColumns + 2);
                columns.add(cancelColumn);

                entity.setBoardColumns(columns);
                
                // Valida o board antes de salvar
                EntityValidator.validateBoard(entity);
                
                try(var connection = getConnection()){
                    var service = new BoardService(connection);
                    var savedBoard = service.insert(entity);
                    System.out.println("✅ Board '" + savedBoard.getName() + "' criado com sucesso! ID: " + savedBoard.getId());
                    log.info("Board criado: {} (ID: {})", savedBoard.getName(), savedBoard.getId());
                }
                
            } catch (SQLException e) {
                log.error("Erro ao criar board", e);
                throw new RuntimeException("Erro ao salvar board no banco de dados", e);
            }
            
            return null;
        });
    }

    private void selectBoard() throws SQLException {
        System.out.println("\n📋 SELECIONANDO BOARD");
        System.out.println("-".repeat(30));
        
        Long boardId = readLongInput("Digite o ID do board: ");
        
        try(var connection = getConnection()){
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(boardId);
            optional.ifPresentOrElse(
                    b -> {
                        System.out.println("✅ Board encontrado: " + b.getName());
                        log.info("Board selecionado: {} (ID: {})", b.getName(), b.getId());
                        new BoardMenu(b).execute();
                    },
                    () -> {
                        System.out.println("❌ Board com ID " + boardId + " nao foi encontrado");
                        log.warn("Tentativa de acesso a board inexistente: {}", boardId);
                    }
            );
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.println("\n🗑 EXCLUINDO BOARD");
        System.out.println("-".repeat(30));
        
        Long boardId = readLongInput("Digite o ID do board que será excluído: ");
        
        String confirmation = readStringInput("Tem certeza? Digite 'CONFIRMAR' para prosseguir: ");
        if (!"CONFIRMAR".equals(confirmation)) {
            System.out.println("❌ Operacao cancelada");
            return;
        }
        
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            if (service.delete(boardId)){
                System.out.println("✅ Board " + boardId + " foi excluído com sucesso");
                log.info("Board excluído: {}", boardId);
            } else {
                System.out.println("❌ Board com ID " + boardId + " nao foi encontrado");
                log.warn("Tentativa de exclusão de board inexistente: {}", boardId);
            }
        }
    }
    
    private void listAllBoards() throws SQLException {
        System.out.println("\n📊 LISTANDO TODOS OS BOARDS");
        System.out.println("-".repeat(30));
        
        try(var connection = getConnection()){
            var queryService = new BoardQueryService(connection);
            var boards = queryService.findAll();
            
            if (boards.isEmpty()) {
                System.out.println("📭 Nenhum board encontrado");
            } else {
                System.out.println("Boards disponíveis:");
                boards.forEach(board -> {
                    System.out.printf("• ID: %d | Nome: %s | Colunas: %d%n", 
                        board.getId(), board.getName(), board.getBoardColumns().size());
                });
            }
        }
    }
    
    private void showReports() throws SQLException {
        System.out.println("\n📈 RELATÓRIOS");
        System.out.println("-".repeat(30));
        System.out.println("1 - Relatório de produtividade de board");
        System.out.println("2 - Relatório de cards antigos");
        System.out.println("3 - Voltar ao menu principal");
        
        int option = readIntegerInput("Escolha uma opção: ");
        
        switch (option) {
            case 1 -> generateProductivityReport();
            case 2 -> generateOldCardsReport();
            case 3 -> { /* Volta ao menu principal */ }
            default -> System.out.println("❌ Opcao invalida");
        }
    }
    
    private void generateProductivityReport() throws SQLException {
        Long boardId = readLongInput("Digite o ID do board: ");
        
        try(var connection = getConnection()){
            var reportService = new ReportService(connection);
            var report = reportService.generateBoardProductivityReport(boardId);
            report.printReport();
        } catch (Exception e) {
            System.out.println("❌ Erro ao gerar relatorio: " + e.getMessage());
            log.error("Erro ao gerar relatório de produtividade", e);
        }
    }
    
    private void generateOldCardsReport() throws SQLException {
        int days = readIntegerInput("Cards mais antigos que quantos dias? ");
        
        try(var connection = getConnection()){
            var reportService = new ReportService(connection);
            var report = reportService.generateOldCardsReport(days);
            report.printReport();
        } catch (Exception e) {
            System.out.println("❌ Erro ao gerar relatorio: " + e.getMessage());
            log.error("Erro ao gerar relatório de cards antigos", e);
        }
    }
    
    private void showMetrics() {
        System.out.println("\n⚡ MÉTRICAS DE PERFORMANCE");
        System.out.println("-".repeat(30));
        
        var report = metricsCollector.generateReport();
        report.printReport();
    }
    
    // Métodos utilitários para entrada de dados com validação
    private String readStringInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.next().trim();
        if (input.isEmpty()) {
            throw new ValidationException("Entrada não pode ser vazia");
        }
        return input;
    }
    
    private int readIntegerInput(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // Limpa o buffer
            throw new ValidationException("Entrada deve ser um número válido");
        }
    }
    
    private Long readLongInput(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLong();
        } catch (Exception e) {
            scanner.nextLine(); // Limpa o buffer
            throw new ValidationException("Entrada deve ser um número válido");
        }
    }

    private BoardColumnEntity createColumn(final String name, final BoardColumnKindEnum kind, final int order){
        var boardColumn = new BoardColumnEntity();
        boardColumn.setName(name);
        boardColumn.setKind(kind);
        boardColumn.setOrder(order);
        return boardColumn;
    }

}
