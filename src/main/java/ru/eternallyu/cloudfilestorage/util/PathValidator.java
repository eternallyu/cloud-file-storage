package ru.eternallyu.cloudfilestorage.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.eternallyu.cloudfilestorage.error.BadRequestException;

@Slf4j
@Component
public class PathValidator {

    public static void validatePath(String path) {
        if (path == null || path.isBlank()) {
            log.error("Invalid path, null or blank");
            throw new BadRequestException("Пустой путь к файлу");
        }

        if (path.contains("..") || path.contains("//")) {
            log.error("Invalid path, contains invalid characters");
            throw new BadRequestException("Неверный путь к файлу");
        }
    }

    public static void validateQuery(String query) {
        if (query == null || query.isBlank()) {
            log.error("Invalid query, null or blank");
            throw new BadRequestException("Пустой запрос");
        }
    }
}
