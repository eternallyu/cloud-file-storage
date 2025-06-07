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

        if (!path.endsWith("/")) {
            path = path + "/";
        }

        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullPath = userRoot + path;

        validatePath(fullPath);

        minioRepository.createDirectory(fullPath);

        return minioRepository.getResourceInfo(fullPath);
    }

    public List<FileInfoDto> getDirectoryInfo(String path, String username) {

        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullPath  = userRoot + path;

        return minioRepository.getFileInfoDtoList(fullPath, path);

    }
}
