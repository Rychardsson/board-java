package br.com.dio.validation;

import br.com.dio.exception.ValidationException;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import static lombok.AccessLevel.PRIVATE;

/**
 * Utilitário para validações comuns da aplicação
 */
@NoArgsConstructor(access = PRIVATE)
public final class ValidationUtils {

    /**
     * Valida se o objeto não é nulo
     */
    public static <T> T requireNonNull(T object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
        return object;
    }

    /**
     * Valida se a string não é nula ou vazia
     */
    public static String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }

    /**
     * Valida se o número é positivo
     */
    public static Long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw new ValidationException(message);
        }
        return value;
    }

    /**
     * Valida se a coleção não é nula ou vazia
     */
    public static <T extends Collection<?>> T requireNonEmpty(T collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new ValidationException(message);
        }
        return collection;
    }

    /**
     * Valida se o valor atende a uma condição específica
     */
    public static <T> T requireCondition(T value, Predicate<T> condition, String message) {
        requireNonNull(value, "Valor não pode ser nulo");
        if (!condition.test(value)) {
            throw new ValidationException(message);
        }
        return value;
    }

    /**
     * Valida se o texto tem um tamanho mínimo e máximo
     */
    public static String requireLength(String value, int minLength, int maxLength, String fieldName) {
        requireNonBlank(value, fieldName + " não pode ser vazio");
        if (value.length() < minLength) {
            throw new ValidationException(fieldName + " deve ter pelo menos " + minLength + " caracteres");
        }
        if (value.length() > maxLength) {
            throw new ValidationException(fieldName + " não pode ter mais que " + maxLength + " caracteres");
        }
        return value;
    }

    /**
     * Valida se dois objetos são iguais
     */
    public static <T> void requireEquals(T expected, T actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new ValidationException(message);
        }
    }
}
