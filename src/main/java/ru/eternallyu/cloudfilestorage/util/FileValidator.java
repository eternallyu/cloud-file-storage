package ru.eternallyu.cloudfilestorage.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import ru.eternallyu.cloudfilestorage.error.BadRequestException;

import java.util.List;

@Slf4j
@UtilityClass
public class FileValidator {

    public static final int MAX_FILES_COUNT = 10;
    public static final int MAX_FILE_SIZE = 5242880;

    public static void validateFiles(List<MultipartFile> files) {
        if (files.size() > MAX_FILES_COUNT) {
            throw new BadRequestException("Слишком много файлов");
        }

        for (MultipartFile fileItem : files) {
            if (fileItem.isEmpty()) {
                throw new BadRequestException("Один из загружаемых файлов пуст");
            }

            if (fileItem.getSize() > MAX_FILE_SIZE) {
                throw new BadRequestException("Размер одного из загружаемых файлов слишком большой");
            }
        }
    }

    public static void validateOriginalFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            log.warn("File name is null or empty");
            throw new BadRequestException("Имя файла пустое");
        }
    }

}
