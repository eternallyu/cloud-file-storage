package ru.eternallyu.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.eternallyu.cloudfilestorage.dto.file.FileInfoDto;
import ru.eternallyu.cloudfilestorage.repository.MinioRepository;

import java.util.List;

import static ru.eternallyu.cloudfilestorage.util.PathValidator.validatePath;

@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final MinioRepository minioRepository;

    public FileInfoDto createEmptyDirectory(String path, String username) {

        if (!isDirectory(path)) {
            path = path + "/";
        }

        String fullPath = getFullPath(path, username);
        validatePath(fullPath);

        minioRepository.createDirectory(fullPath);

        return minioRepository.getResourceInfo(fullPath);
    }

    public List<FileInfoDto> getDirectoryInfo(String path, String username) {

        String fullPath = getFullPath(path, username);
        validatePath(fullPath);

        return minioRepository.getFileInfoDtoList(fullPath, path);

    }

    public static boolean isDirectory(String path) {
        return path.endsWith("/");
    }

    public String getFullPath(String path, String username) {
        String userRoot = minioRepository.getUserRootFolderName(username);
        return userRoot + path;
    }
}
