package ru.eternallyu.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.eternallyu.cloudfilestorage.dto.file.FileInfoDto;
import ru.eternallyu.cloudfilestorage.error.NotFoundException;
import ru.eternallyu.cloudfilestorage.repository.MinioRepository;

import java.util.ArrayList;
import java.util.List;

import static ru.eternallyu.cloudfilestorage.service.DirectoryService.SLASH;
import static ru.eternallyu.cloudfilestorage.service.DirectoryService.isDirectory;
import static ru.eternallyu.cloudfilestorage.util.FileValidator.validateFiles;
import static ru.eternallyu.cloudfilestorage.util.FileValidator.validateOriginalFileName;
import static ru.eternallyu.cloudfilestorage.util.PathValidator.validatePath;
import static ru.eternallyu.cloudfilestorage.util.PathValidator.validateQuery;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioRepository minioRepository;

    public FileInfoDto getFileInfo(String relativePath, String username) {

        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullPath = userRoot + relativePath;

        validatePath(fullPath);
        checkStartsWithUserRoot(!fullPath.startsWith(userRoot));

        return minioRepository.getResourceInfo(fullPath);
    }


    public void deleteFile(String relativePath, String username) {

        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullPath = userRoot + relativePath;

        validatePath(fullPath);
        checkStartsWithUserRoot(!fullPath.startsWith(userRoot));

        if (isDirectory(relativePath)) {
            minioRepository.deleteFolder(fullPath);
        } else {
            minioRepository.deleteFile(fullPath);
        }
    }

    public InputStreamResource downloadFile(String relativePath, String username) {

        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullPath = userRoot + relativePath;

        validatePath(fullPath);
        checkStartsWithUserRoot(!fullPath.startsWith(userRoot));

        boolean isDirectory = isDirectory(relativePath);
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

        checkStartsWithUserRoot(!fullFrom.startsWith(userRoot) || !fullTo.startsWith(userRoot));

        boolean isDir = isDirectory(relativeFrom);
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
        validateFiles(file);

        String userRoot = minioRepository.getUserRootFolderName(username);
        String fullDir = userRoot + relativeDir;

        validatePath(fullDir);
        checkStartsWithUserRoot(!fullDir.startsWith(userRoot));

        List<FileInfoDto> result = new ArrayList<>();
        for (MultipartFile fileItem : file) {
            String originalName = fileItem.getOriginalFilename();

            validateOriginalFileName(originalName);

            String fullPath = fullDir + originalName;
            if (originalName.contains(SLASH)) {
                String[] parts = originalName.split(SLASH);
                String accum = fullDir;
                for (int i = 0; i < parts.length - 1; i++) {
                    accum += parts[i] + SLASH;
                    minioRepository.createDirectory(accum);
                    result.add(minioRepository.getResourceInfo(accum));
                }
            }

            minioRepository.uploadFile(fullPath, fileItem);
            result.add(minioRepository.getResourceInfo(fullPath));
        }
        return result;
    }

    private static void checkStartsWithUserRoot(boolean notStartsWithUserRoot) {
        if (notStartsWithUserRoot) {
            log.warn("File not found");
            throw new NotFoundException("Ресурс не найден");
        }
    }
}
