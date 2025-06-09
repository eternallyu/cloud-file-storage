package ru.eternallyu.cloudfilestorage.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

import static ru.eternallyu.cloudfilestorage.service.DirectoryService.isDirectory;

@UtilityClass
public class ControllerUtils {
    public static void setContentDisposition(String path, HttpServletResponse response) {
        String filename;
        if (isDirectory(path)) {
            String withoutSlash = path.substring(0, path.length() - 1);
            filename = withoutSlash.substring(withoutSlash.lastIndexOf('/') + 1);
        } else {
            filename = path.substring(path.lastIndexOf('/') + 1);
        }

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
    }
}
