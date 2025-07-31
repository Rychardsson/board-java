package br.com.dio.service;

import br.com.dio.dto.BoardDetailsDTO;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.search.CardSearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço para geração de relatórios
 */
@Slf4j
@RequiredArgsConstructor
public class ReportService {
    
    private final Connection connection;
    
    /**
     * Gera relatório de produtividade de um board
     */
    public BoardProductivityReport generateBoardProductivityReport(Long boardId) throws SQLException {
        log.info("Gerando relatório de produtividade para board {}", boardId);
        
        BoardDAO boardDAO = new BoardDAO(connection);
        CardDAO cardDAO = new CardDAO(connection);
        
        // Busca detalhes do board
        BoardDetailsDTO boardDetails = boardDAO.findById(boardId)
            .map(board -> {
                try {
                    BoardQueryService queryService = new BoardQueryService(connection);
                    return queryService.showBoardDetails(boardId).orElse(null);
                } catch (SQLException e) {
                    throw new RuntimeException("Erro ao buscar detalhes do board", e);
                }
            })
            .orElseThrow(() -> new RuntimeException("Board não encontrado: " + boardId));
        
        // Calcula métricas por coluna
        Map<String, Long> cardsByColumn = boardDetails.columns().stream()
            .collect(Collectors.toMap(
                col -> col.name(),
                col -> {
                    try {
                        CardSearchCriteria criteria = CardSearchCriteria.builder()
                            .boardColumnIds(List.of(col.id()))
                            .build();
                        return cardDAO.countByCriteria(criteria);
                    } catch (SQLException e) {
                        log.error("Erro ao contar cards da coluna {}", col.id(), e);
                        return 0L;
                    }
                }
            ));
        
        // Calcula cards bloqueados
        CardSearchCriteria blockedCriteria = CardSearchCriteria.builder()
            .boardIds(List.of(boardId))
            .isBlocked(true)
            .build();
        long blockedCards = cardDAO.countByCriteria(blockedCriteria);
        
        // Calcula total de cards
        CardSearchCriteria totalCriteria = CardSearchCriteria.builder()
            .boardIds(List.of(boardId))
            .build();
        long totalCards = cardDAO.countByCriteria(totalCriteria);
        
        return new BoardProductivityReport(
            boardId,
            boardDetails.name(),
            totalCards,
            blockedCards,
            cardsByColumn,
            LocalDateTime.now()
        );
    }
    
    /**
     * Gera relatório de cards mais antigos
     */
    public OldCardsReport generateOldCardsReport(int daysThreshold) throws SQLException {
        log.info("Gerando relatório de cards antigos (threshold: {} dias)", daysThreshold);
        
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(daysThreshold);
        
        CardDAO cardDAO = new CardDAO(connection);
        CardSearchCriteria criteria = CardSearchCriteria.builder()
            .createdBefore(thresholdDate)
            .limit(100) // Limita para evitar sobrecarga
            .build();
        
        var oldCards = cardDAO.findByCriteria(criteria);
        
        return new OldCardsReport(
            daysThreshold,
            thresholdDate,
            oldCards,
            LocalDateTime.now()
        );
    }
    
    /**
     * Relatório de produtividade de um board
     */
    public static class BoardProductivityReport {
        private final Long boardId;
        private final String boardName;
        private final long totalCards;
        private final long blockedCards;
        private final Map<String, Long> cardsByColumn;
        private final LocalDateTime generatedAt;
        
        public BoardProductivityReport(Long boardId, String boardName, long totalCards, 
                                     long blockedCards, Map<String, Long> cardsByColumn, 
                                     LocalDateTime generatedAt) {
            this.boardId = boardId;
            this.boardName = boardName;
            this.totalCards = totalCards;
            this.blockedCards = blockedCards;
            this.cardsByColumn = cardsByColumn;
            this.generatedAt = generatedAt;
        }
        
        public void printReport() {
            System.out.println("\n=== RELATÓRIO DE PRODUTIVIDADE ===");
            System.out.println("Board: " + boardName + " (ID: " + boardId + ")");
            System.out.println("Gerado em: " + generatedAt);
            System.out.println("Total de cards: " + totalCards);
            System.out.println("Cards bloqueados: " + blockedCards);
            
            if (totalCards > 0) {
                double blockedPercentage = (double) blockedCards / totalCards * 100;
                System.out.printf("Percentual bloqueado: %.1f%%%n", blockedPercentage);
            }
            
            System.out.println("\n--- Distribuição por Coluna ---");
            cardsByColumn.forEach((column, count) -> {
                if (totalCards > 0) {
                    double percentage = (double) count / totalCards * 100;
                    System.out.printf("%s: %d cards (%.1f%%)%n", column, count, percentage);
                } else {
                    System.out.printf("%s: %d cards%n", column, count);
                }
            });
            
            System.out.println("=================================\n");
        }
        
        // Getters
        public Long getBoardId() { return boardId; }
        public String getBoardName() { return boardName; }
        public long getTotalCards() { return totalCards; }
        public long getBlockedCards() { return blockedCards; }
        public Map<String, Long> getCardsByColumn() { return cardsByColumn; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
    }
    
    /**
     * Relatório de cards antigos
     */
    public static class OldCardsReport {
        private final int daysThreshold;
        private final LocalDateTime thresholdDate;
        private final List<?> oldCards;
        private final LocalDateTime generatedAt;
        
        public OldCardsReport(int daysThreshold, LocalDateTime thresholdDate, 
                            List<?> oldCards, LocalDateTime generatedAt) {
            this.daysThreshold = daysThreshold;
            this.thresholdDate = thresholdDate;
            this.oldCards = oldCards;
            this.generatedAt = generatedAt;
        }
        
        public void printReport() {
            System.out.println("\n=== RELATÓRIO DE CARDS ANTIGOS ===");
            System.out.println("Threshold: " + daysThreshold + " dias");
            System.out.println("Data limite: " + thresholdDate);
            System.out.println("Gerado em: " + generatedAt);
            System.out.println("Cards encontrados: " + oldCards.size());
            
            if (!oldCards.isEmpty()) {
                System.out.println("\n--- Cards que precisam de atenção ---");
                oldCards.forEach(card -> {
                    System.out.println("• " + card.toString());
                });
            }
            
            System.out.println("===================================\n");
        }
        
        // Getters
        public int getDaysThreshold() { return daysThreshold; }
        public LocalDateTime getThresholdDate() { return thresholdDate; }
        public List<?> getOldCards() { return oldCards; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
    }
}
