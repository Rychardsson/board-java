package br.com.dio.search;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Critérios de busca para cards
 */
@Data
@Builder
public class CardSearchCriteria {
    
    private String titleContains;
    private String descriptionContains;
    private List<Long> boardColumnIds;
    private List<Long> boardIds;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private Boolean isBlocked;
    private String blockReasonContains;
    private int limit;
    private int offset;
    
    public static CardSearchCriteria empty() {
        return CardSearchCriteria.builder()
            .limit(50)
            .offset(0)
            .build();
    }
    
    public boolean hasTextFilter() {
        return (titleContains != null && !titleContains.trim().isEmpty()) ||
               (descriptionContains != null && !descriptionContains.trim().isEmpty());
    }
    
    public boolean hasDateFilter() {
        return createdAfter != null || createdBefore != null;
    }
    
    public boolean hasColumnFilter() {
        return boardColumnIds != null && !boardColumnIds.isEmpty();
    }
    
    public boolean hasBoardFilter() {
        return boardIds != null && !boardIds.isEmpty();
    }
    
    public boolean hasBlockFilter() {
        return isBlocked != null || 
               (blockReasonContains != null && !blockReasonContains.trim().isEmpty());
    }
}
