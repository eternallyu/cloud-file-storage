package ru.eternallyu.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.eternallyu.cloudfilestorage.dto.file.FileInfoDto;
import ru.eternallyu.cloudfilestorage.error.BadRequestException;
import ru.eternallyu.cloudfilestorage.error.ResourceNotFoundException;
import ru.eternallyu.cloudfilestorage.repository.MinioRepository;

import java.util.ArrayList;
import java.util.List;

import static ru.eternallyu.cloudfilestorage.util.PathValidator.validatePath;
import static ru.eternallyu.cloudfilestorage.util.PathValidator.validateQuery;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioRepository minioRepository;

    public FileInfoDto getFileInfo(String relativePath, String username) {
        validatePath(relativePath);

        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullPath = userRoot + relativePath;

        if (!fullPath.startsWith(userRoot)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }

        return minioRepository.getResourceInfo(fullPath);
    }

    public void deleteFile(String relativePath, String username) {
        validatePath(relativePath);
        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullPath = userRoot + relativePath;
        if (!fullPath.startsWith(userRoot)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }
        minioRepository.deleteFile(fullPath);
    }

    public InputStreamResource downloadFile(String relativePath, String username) {
        validatePath(relativePath);
        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullPath = userRoot + relativePath;
        if (!fullPath.startsWith(userRoot)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }
        boolean isDirectory = relativePath.endsWith("/");
        if (isDirectory) {
            return minioRepository.downloadFolder(fullPath);
        } else {
            return minioRepository.downloadFile(fullPath);
        }
    }

    public void moveOrRenameResource(String relativeFrom, String relativeTo, String username) {
        validatePath(relativeFrom);
        validatePath(relativeTo);

        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullFrom = userRoot + relativeFrom;
        String fullTo = userRoot + relativeTo;

        if (!fullFrom.startsWith(userRoot) || !fullTo.startsWith(userRoot)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }

        boolean isDir = relativeFrom.endsWith("/");
        if (isDir) {
            minioRepository.renameFolder(fullFrom, fullTo);
        } else {
            minioRepository.renameFile(fullFrom, fullTo);
        }
    }

    public List<FileInfoDto> searchByQuery(String query, String username) {
        validateQuery(query);
        return minioRepository.searchInUserSpace(query, username);
    }

    public List<FileInfoDto> uploadResource(MultipartFile file, String relativeDir, String username) {
        validatePath(relativeDir);
        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullDir = userRoot + relativeDir;
        if (!fullDir.startsWith(userRoot)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new BadRequestException("Имя файла пустое");
        }
        String fullPath = fullDir + originalName;
        minioRepository.uploadFile(fullPath, file);

        List<FileInfoDto> result = new ArrayList<>();
        if (originalName.contains("/")) {
            String subDirRelative = originalName.substring(0, originalName.indexOf('/') + 1);
            String fullSubDir = fullDir + subDirRelative;
            result.add(minioRepository.getResourceInfo(fullSubDir));
        }
        result.add(minioRepository.getResourceInfo(fullPath));
        return result;
    }
}
