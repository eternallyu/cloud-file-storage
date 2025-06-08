package ru.eternallyu.cloudfilestorage.util;

import org.springframework.stereotype.Component;
import ru.eternallyu.cloudfilestorage.error.BadRequestException;

@Component
public class PathValidator {

    public static void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new BadRequestException("Пустой путь к файлу");
        }

        if (path.contains("..") || path.contains("//")) {
            throw new BadRequestException("Неверный путь к файлу");
        }
    }

    public static void validateQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Пустой запрос");
        }
    }
}
