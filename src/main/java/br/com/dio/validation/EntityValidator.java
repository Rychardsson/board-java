package br.com.dio.validation;

import br.com.dio.exception.ValidationException;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.CardEntity;
import lombok.NoArgsConstructor;

import static br.com.dio.validation.ValidationUtils.*;
import static lombok.AccessLevel.PRIVATE;

/**
 * Validador para entidades do domínio
 */
@NoArgsConstructor(access = PRIVATE)
public final class EntityValidator {

    public static void validateBoard(BoardEntity board) {
        requireNonNull(board, "Board não pode ser nulo");
        requireLength(board.getName(), 2, 100, "Nome do board");
        requireNonEmpty(board.getBoardColumns(), "Board deve ter pelo menos uma coluna");
        
        // Valida se tem coluna inicial
        boolean hasInitialColumn = board.getBoardColumns().stream()
            .anyMatch(col -> col.getKind() != null && col.getKind().name().equals("INITIAL"));
        if (!hasInitialColumn) {
            throw new ValidationException("Board deve ter uma coluna inicial");
        }
        
        // Valida cada coluna
        board.getBoardColumns().forEach(EntityValidator::validateBoardColumn);
    }

    public static void validateBoardColumn(BoardColumnEntity column) {
        requireNonNull(column, "Coluna do board não pode ser nula");
        requireLength(column.getName(), 2, 50, "Nome da coluna");
        requireNonNull(column.getKind(), "Tipo da coluna não pode ser nulo");
        
        if (column.getOrder() < 0) {
            throw new ValidationException("Ordem da coluna deve ser um número positivo");
        }
    }

    public static void validateCard(CardEntity card) {
        requireNonNull(card, "Card não pode ser nulo");
        requireLength(card.getTitle(), 3, 100, "Título do card");
        requireLength(card.getDescription(), 5, 500, "Descrição do card");
        requireNonNull(card.getBoardColumn(), "Card deve estar associado a uma coluna");
    }

    public static void validateCardForMove(CardEntity card) {
        validateCard(card);
        requireNonNull(card.getId(), "ID do card é obrigatório para movimentação");
    }

    public static void validateBlockReason(String reason) {
        requireLength(reason, 5, 200, "Motivo do bloqueio");
    }
}
