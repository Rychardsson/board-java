package br.com.dio.search;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Resultado paginado de busca
 */
@Data
@AllArgsConstructor
public class SearchResult<T> {
    
    private List<T> items;
    private long totalCount;
    private int pageSize;
    private int currentPage;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public static <T> SearchResult<T> of(List<T> items, long totalCount, int pageSize, int currentPage) {
        boolean hasNext = (long) (currentPage + 1) * pageSize < totalCount;
        boolean hasPrevious = currentPage > 0;
        
        return new SearchResult<>(items, totalCount, pageSize, currentPage, hasNext, hasPrevious);
    }
    
    public int getTotalPages() {
        return (int) Math.ceil((double) totalCount / pageSize);
    }
    
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
