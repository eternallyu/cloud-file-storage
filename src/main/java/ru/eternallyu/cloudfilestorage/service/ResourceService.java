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
        if (relativePath.endsWith("/")) {
            minioRepository.deleteFolder(fullPath);
        } else {
            minioRepository.deleteFile(fullPath);
        }
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

    public List<FileInfoDto> uploadResources(List<MultipartFile> file, String relativeDir, String username) {

        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullDir = userRoot + relativeDir;
        if (!fullDir.startsWith(userRoot)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }

        List<FileInfoDto> result = new ArrayList<>();
        for (MultipartFile fileItem : file) {
            String originalName = fileItem.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                throw new BadRequestException("Имя файла пустое");
            }
            String fullPath = fullDir + originalName;
            if (originalName.contains("/")) {
                String[] parts = originalName.split("/");
                String accum = fullDir;
                for (int i = 0; i < parts.length - 1; i++) {
                    accum += parts[i] + "/";
                    minioRepository.createDirectory(accum);
                    result.add(minioRepository.getResourceInfo(accum));
                }
            }

            minioRepository.uploadFile(fullPath, fileItem);
            result.add(minioRepository.getResourceInfo(fullPath));
        }
        return result;
    }
}
