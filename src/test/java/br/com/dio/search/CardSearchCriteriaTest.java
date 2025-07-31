package br.com.dio.search;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para critérios de busca
 */
class CardSearchCriteriaTest {

    @Test
    void shouldCreateEmptySearchCriteria() {
        // When
        CardSearchCriteria criteria = CardSearchCriteria.empty();
        
        // Then
        assertThat(criteria.getLimit()).isEqualTo(50);
        assertThat(criteria.getOffset()).isEqualTo(0);
        assertThat(criteria.getTitleContains()).isNull();
        assertThat(criteria.getDescriptionContains()).isNull();
    }
    
    @Test
    void shouldDetectTextFilter() {
        // Given
        CardSearchCriteria criteriaWithTitle = CardSearchCriteria.builder()
            .titleContains("test")
            .build();
            
        CardSearchCriteria criteriaWithDescription = CardSearchCriteria.builder()
            .descriptionContains("test")
            .build();
            
        CardSearchCriteria criteriaWithoutText = CardSearchCriteria.builder()
            .build();
        
        // When & Then
        assertThat(criteriaWithTitle.hasTextFilter()).isTrue();
        assertThat(criteriaWithDescription.hasTextFilter()).isTrue();
        assertThat(criteriaWithoutText.hasTextFilter()).isFalse();
    }
    
    @Test
    void shouldDetectDateFilter() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        CardSearchCriteria criteriaWithDateAfter = CardSearchCriteria.builder()
            .createdAfter(now)
            .build();
            
        CardSearchCriteria criteriaWithDateBefore = CardSearchCriteria.builder()
            .createdBefore(now)
            .build();
            
        CardSearchCriteria criteriaWithoutDate = CardSearchCriteria.builder()
            .build();
        
        // When & Then
        assertThat(criteriaWithDateAfter.hasDateFilter()).isTrue();
        assertThat(criteriaWithDateBefore.hasDateFilter()).isTrue();
        assertThat(criteriaWithoutDate.hasDateFilter()).isFalse();
    }
    
    @Test
    void shouldDetectColumnFilter() {
        // Given
        CardSearchCriteria criteriaWithColumns = CardSearchCriteria.builder()
            .boardColumnIds(List.of(1L, 2L))
            .build();
            
        CardSearchCriteria criteriaWithoutColumns = CardSearchCriteria.builder()
            .build();
        
        // When & Then
        assertThat(criteriaWithColumns.hasColumnFilter()).isTrue();
        assertThat(criteriaWithoutColumns.hasColumnFilter()).isFalse();
    }
    
    @Test
    void shouldDetectBoardFilter() {
        // Given
        CardSearchCriteria criteriaWithBoards = CardSearchCriteria.builder()
            .boardIds(List.of(1L, 2L))
            .build();
            
        CardSearchCriteria criteriaWithoutBoards = CardSearchCriteria.builder()
            .build();
        
        // When & Then
        assertThat(criteriaWithBoards.hasBoardFilter()).isTrue();
        assertThat(criteriaWithoutBoards.hasBoardFilter()).isFalse();
    }
    
    @Test
    void shouldDetectBlockFilter() {
        // Given
        CardSearchCriteria criteriaWithBlockStatus = CardSearchCriteria.builder()
            .isBlocked(true)
            .build();
            
        CardSearchCriteria criteriaWithBlockReason = CardSearchCriteria.builder()
            .blockReasonContains("waiting")
            .build();
            
        CardSearchCriteria criteriaWithoutBlock = CardSearchCriteria.builder()
            .build();
        
        // When & Then
        assertThat(criteriaWithBlockStatus.hasBlockFilter()).isTrue();
        assertThat(criteriaWithBlockReason.hasBlockFilter()).isTrue();
        assertThat(criteriaWithoutBlock.hasBlockFilter()).isFalse();
    }
}
