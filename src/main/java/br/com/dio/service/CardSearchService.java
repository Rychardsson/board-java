package br.com.dio.service;

import br.com.dio.metrics.MetricsCollector;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.search.CardSearchCriteria;
import br.com.dio.search.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Serviço de busca avançada para cards
 */
@Slf4j
@RequiredArgsConstructor
public class CardSearchService {
    
    private final Connection connection;
    private final MetricsCollector metricsCollector = MetricsCollector.getInstance();
    
    /**
     * Busca cards com base nos critérios fornecidos
     */
    public SearchResult<CardEntity> searchCards(CardSearchCriteria criteria) throws SQLException {
        log.debug("Iniciando busca de cards com critérios: {}", criteria);
        
        return metricsCollector.measureOperation("search_cards", () -> {
            try {
                CardDAO cardDAO = new CardDAO(connection);
                
                // Conta o total de registros
                long totalCount = cardDAO.countByCriteria(criteria);
                
                // Busca os cards da página atual
                List<CardEntity> cards = cardDAO.findByCriteria(criteria);
                
                int pageSize = criteria.getLimit();
                int currentPage = criteria.getOffset() / pageSize;
                
                SearchResult<CardEntity> result = SearchResult.of(cards, totalCount, pageSize, currentPage);
                
                log.info("Busca concluída. Encontrados {} cards de {} total", 
                    cards.size(), totalCount);
                
                return result;
                
            } catch (SQLException e) {
                log.error("Erro ao buscar cards: {}", e.getMessage(), e);
                throw new RuntimeException("Erro ao buscar cards", e);
            }
        });
    }
    
    /**
     * Busca cards por texto livre (título ou descrição)
     */
    public SearchResult<CardEntity> searchByText(String searchText, int limit, int offset) throws SQLException {
        CardSearchCriteria criteria = CardSearchCriteria.builder()
            .titleContains(searchText)
            .descriptionContains(searchText)
            .limit(limit)
            .offset(offset)
            .build();
            
        return searchCards(criteria);
    }
    
    /**
     * Busca cards de um board específico
     */
    public SearchResult<CardEntity> searchByBoard(Long boardId, int limit, int offset) throws SQLException {
        CardSearchCriteria criteria = CardSearchCriteria.builder()
            .boardIds(List.of(boardId))
            .limit(limit)
            .offset(offset)
            .build();
            
        return searchCards(criteria);
    }
    
    /**
     * Busca cards bloqueados
     */
    public SearchResult<CardEntity> searchBlockedCards(int limit, int offset) throws SQLException {
        CardSearchCriteria criteria = CardSearchCriteria.builder()
            .isBlocked(true)
            .limit(limit)
            .offset(offset)
            .build();
            
        return searchCards(criteria);
    }
}
