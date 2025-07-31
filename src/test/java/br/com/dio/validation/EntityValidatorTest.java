package br.com.dio.validation;

import br.com.dio.exception.ValidationException;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.CardEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para validação de entidades
 */
class EntityValidatorTest {

    @Test
    void shouldValidateValidBoard() {
        // Given
        BoardEntity board = createValidBoard();
        
        // When & Then
        assertThatNoException().isThrownBy(() -> EntityValidator.validateBoard(board));
    }
    
    @Test
    void shouldThrowExceptionForNullBoard() {
        // When & Then
        assertThatThrownBy(() -> EntityValidator.validateBoard(null))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Board não pode ser nulo");
    }
    
    @Test
    void shouldThrowExceptionForBoardWithoutName() {
        // Given
        BoardEntity board = createValidBoard();
        board.setName(null);
        
        // When & Then
        assertThatThrownBy(() -> EntityValidator.validateBoard(board))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Nome do board");
    }
    
    @Test
    void shouldThrowExceptionForBoardWithShortName() {
        // Given
        BoardEntity board = createValidBoard();
        board.setName("A"); // Nome muito curto
        
        // When & Then
        assertThatThrownBy(() -> EntityValidator.validateBoard(board))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Nome do board deve ter pelo menos 2 caracteres");
    }
    
    @Test
    void shouldThrowExceptionForBoardWithoutInitialColumn() {
        // Given
        BoardEntity board = createValidBoard();
        board.getBoardColumns().clear();
        
        BoardColumnEntity column = new BoardColumnEntity();
        column.setName("Final");
        column.setKind(BoardColumnKindEnum.FINAL);
        column.setOrder(0);
        board.setBoardColumns(List.of(column));
        
        // When & Then
        assertThatThrownBy(() -> EntityValidator.validateBoard(board))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Board deve ter uma coluna inicial");
    }
    
    @Test
    void shouldValidateValidCard() {
        // Given
        CardEntity card = createValidCard();
        
        // When & Then
        assertThatNoException().isThrownBy(() -> EntityValidator.validateCard(card));
    }
    
    @Test
    void shouldThrowExceptionForCardWithShortTitle() {
        // Given
        CardEntity card = createValidCard();
        card.setTitle("AB"); // Título muito curto
        
        // When & Then
        assertThatThrownBy(() -> EntityValidator.validateCard(card))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Título do card deve ter pelo menos 3 caracteres");
    }
    
    @Test
    void shouldThrowExceptionForCardWithoutColumn() {
        // Given
        CardEntity card = createValidCard();
        card.setBoardColumn(null);
        
        // When & Then
        assertThatThrownBy(() -> EntityValidator.validateCard(card))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Card deve estar associado a uma coluna");
    }
    
    @Test
    void shouldValidateBlockReason() {
        // Given
        String validReason = "Aguardando aprovação da equipe";
        
        // When & Then
        assertThatNoException().isThrownBy(() -> EntityValidator.validateBlockReason(validReason));
    }
    
    @Test
    void shouldThrowExceptionForShortBlockReason() {
        // Given
        String shortReason = "Erro"; // Muito curto
        
        // When & Then
        assertThatThrownBy(() -> EntityValidator.validateBlockReason(shortReason))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Motivo do bloqueio deve ter pelo menos 5 caracteres");
    }
    
    private BoardEntity createValidBoard() {
        BoardEntity board = new BoardEntity();
        board.setName("Board de Teste");
        
        BoardColumnEntity initialColumn = new BoardColumnEntity();
        initialColumn.setName("Inicial");
        initialColumn.setKind(BoardColumnKindEnum.INITIAL);
        initialColumn.setOrder(0);
        
        BoardColumnEntity finalColumn = new BoardColumnEntity();
        finalColumn.setName("Final");
        finalColumn.setKind(BoardColumnKindEnum.FINAL);
        finalColumn.setOrder(1);
        
        board.setBoardColumns(List.of(initialColumn, finalColumn));
        
        return board;
    }
    
    private CardEntity createValidCard() {
        CardEntity card = new CardEntity();
        card.setTitle("Card de Teste");
        card.setDescription("Descrição detalhada do card de teste");
        
        BoardColumnEntity column = new BoardColumnEntity();
        column.setId(1L);
        column.setName("Em Progresso");
        card.setBoardColumn(column);
        
        return card;
    }
}
