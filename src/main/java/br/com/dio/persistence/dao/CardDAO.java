package br.com.dio.persistence.dao;

import br.com.dio.dto.CardDetailsDTO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.search.CardSearchCriteria;
import com.mysql.cj.jdbc.StatementImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;
import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class CardDAO {

    private Connection connection;

    public CardEntity insert(final CardEntity entity) throws SQLException {
        var sql = "INSERT INTO CARDS (title, description, board_column_id) values (?, ?, ?);";
        try(var statement = connection.prepareStatement(sql)){
            var i = 1;
            statement.setString(i ++, entity.getTitle());
            statement.setString(i ++, entity.getDescription());
            statement.setLong(i, entity.getBoardColumn().getId());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }
        }
        return entity;
    }

    public void moveToColumn(final Long columnId, final Long cardId) throws SQLException{
        var sql = "UPDATE CARDS SET board_column_id = ? WHERE id = ?;";
        try(var statement = connection.prepareStatement(sql)){
            var i = 1;
            statement.setLong(i ++, columnId);
            statement.setLong(i, cardId);
            statement.executeUpdate();
        }
    }

    public Optional<CardDetailsDTO> findById(final Long id) throws SQLException {
        var sql =
                """
                SELECT c.id,
                       c.title,
                       c.description,
                       b.blocked_at,
                       b.block_reason,
                       c.board_column_id,
                       bc.name,
                       (SELECT COUNT(sub_b.id)
                               FROM BLOCKS sub_b
                              WHERE sub_b.card_id = c.id) blocks_amount
                  FROM CARDS c
                  LEFT JOIN BLOCKS b
                    ON c.id = b.card_id
                   AND b.unblocked_at IS NULL
                 INNER JOIN BOARDS_COLUMNS bc
                    ON bc.id = c.board_column_id
                  WHERE c.id = ?;
                """;
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next()){
                var dto = new CardDetailsDTO(
                        resultSet.getLong("c.id"),
                        resultSet.getString("c.title"),
                        resultSet.getString("c.description"),
                        nonNull(resultSet.getString("b.block_reason")),
                        toOffsetDateTime(resultSet.getTimestamp("b.blocked_at")),
                        resultSet.getString("b.block_reason"),
                        resultSet.getInt("blocks_amount"),
                        resultSet.getLong("c.board_column_id"),
                        resultSet.getString("bc.name")
                );
                return Optional.of(dto);
            }
        }
        return Optional.empty();
    }

    /**
     * Busca cards com base nos critérios fornecidos
     */
    public List<CardEntity> findByCriteria(CardSearchCriteria criteria) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.id, c.title, c.description, c.board_column_id, ");
        sql.append("bc.name as column_name, bc.kind as column_kind, bc.order_position, ");
        sql.append("b.blocked_at, b.block_reason ");
        sql.append("FROM CARDS c ");
        sql.append("INNER JOIN BOARDS_COLUMNS bc ON bc.id = c.board_column_id ");
        sql.append("LEFT JOIN BLOCKS b ON c.id = b.card_id AND b.unblocked_at IS NULL ");
        
        List<Object> parameters = new ArrayList<>();
        appendWhereClause(sql, criteria, parameters);
        
        sql.append(" ORDER BY c.id DESC ");
        sql.append(" LIMIT ? OFFSET ?");
        parameters.add(criteria.getLimit());
        parameters.add(criteria.getOffset());
        
        log.debug("Executando query de busca: {}", sql.toString());
        
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, parameters);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CardEntity> cards = new ArrayList<>();
                
                while (resultSet.next()) {
                    CardEntity card = mapResultSetToCard(resultSet);
                    cards.add(card);
                }
                
                return cards;
            }
        }
    }
    
    /**
     * Conta o total de cards que atendem aos critérios
     */
    public long countByCriteria(CardSearchCriteria criteria) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT c.id) ");
        sql.append("FROM CARDS c ");
        sql.append("INNER JOIN BOARDS_COLUMNS bc ON bc.id = c.board_column_id ");
        sql.append("LEFT JOIN BLOCKS b ON c.id = b.card_id AND b.unblocked_at IS NULL ");
        
        List<Object> parameters = new ArrayList<>();
        appendWhereClause(sql, criteria, parameters);
        
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, parameters);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : 0;
            }
        }
    }
    
    private void appendWhereClause(StringBuilder sql, CardSearchCriteria criteria, List<Object> parameters) {
        List<String> conditions = new ArrayList<>();
        
        // Filtro por texto no título
        if (criteria.getTitleContains() != null && !criteria.getTitleContains().trim().isEmpty()) {
            conditions.add("c.title LIKE ?");
            parameters.add("%" + criteria.getTitleContains().trim() + "%");
        }
        
        // Filtro por texto na descrição
        if (criteria.getDescriptionContains() != null && !criteria.getDescriptionContains().trim().isEmpty()) {
            conditions.add("c.description LIKE ?");
            parameters.add("%" + criteria.getDescriptionContains().trim() + "%");
        }
        
        // Filtro por colunas específicas
        if (criteria.hasBoardFilter()) {
            String placeholders = criteria.getBoardIds().stream()
                .map(id -> "?")
                .reduce((a, b) -> a + "," + b)
                .orElse("");
            conditions.add("bc.board_id IN (" + placeholders + ")");
            criteria.getBoardIds().forEach(parameters::add);
        }
        
        // Filtro por colunas específicas
        if (criteria.hasColumnFilter()) {
            String placeholders = criteria.getBoardColumnIds().stream()
                .map(id -> "?")
                .reduce((a, b) -> a + "," + b)
                .orElse("");
            conditions.add("c.board_column_id IN (" + placeholders + ")");
            criteria.getBoardColumnIds().forEach(parameters::add);
        }
        
        // Filtro por status de bloqueio
        if (criteria.getIsBlocked() != null) {
            if (criteria.getIsBlocked()) {
                conditions.add("b.id IS NOT NULL");
            } else {
                conditions.add("b.id IS NULL");
            }
        }
        
        // Filtro por motivo de bloqueio
        if (criteria.getBlockReasonContains() != null && !criteria.getBlockReasonContains().trim().isEmpty()) {
            conditions.add("b.block_reason LIKE ?");
            parameters.add("%" + criteria.getBlockReasonContains().trim() + "%");
        }
        
        // Filtro por data de criação
        if (criteria.getCreatedAfter() != null) {
            conditions.add("c.created_at >= ?");
            parameters.add(Timestamp.valueOf(criteria.getCreatedAfter()));
        }
        
        if (criteria.getCreatedBefore() != null) {
            conditions.add("c.created_at <= ?");
            parameters.add(Timestamp.valueOf(criteria.getCreatedBefore()));
        }
        
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }
    }
    
    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object param = parameters.get(i);
            if (param instanceof String) {
                statement.setString(i + 1, (String) param);
            } else if (param instanceof Long) {
                statement.setLong(i + 1, (Long) param);
            } else if (param instanceof Integer) {
                statement.setInt(i + 1, (Integer) param);
            } else if (param instanceof Timestamp) {
                statement.setTimestamp(i + 1, (Timestamp) param);
            } else if (param instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) param);
            }
        }
    }
    
    private CardEntity mapResultSetToCard(ResultSet rs) throws SQLException {
        CardEntity card = new CardEntity();
        card.setId(rs.getLong("id"));
        card.setTitle(rs.getString("title"));
        card.setDescription(rs.getString("description"));
        
        // Mapeia a coluna do board
        BoardColumnEntity column = new BoardColumnEntity();
        column.setId(rs.getLong("board_column_id"));
        column.setName(rs.getString("column_name"));
        // Note: column.setKind precisa ser implementado baseado no campo kind
        
        card.setBoardColumn(column);
        
        return card;
    }

}
