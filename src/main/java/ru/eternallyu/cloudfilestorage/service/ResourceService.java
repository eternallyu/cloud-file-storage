package ru.eternallyu.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import ru.eternallyu.cloudfilestorage.dto.file.FileInfoDto;
import ru.eternallyu.cloudfilestorage.error.ResourceNotFoundException;
import ru.eternallyu.cloudfilestorage.repository.MinioRepository;

import static ru.eternallyu.cloudfilestorage.util.PathValidator.validatePath;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioRepository minioRepository;

    public FileInfoDto getFileInfo(String path, String username) {
        validatePath(path);

        checkUserFolderName(path, username);

        return minioRepository.getResourceInfo(path);
    }

    public void deleteFile(String path, String username) {
        validatePath(path);

        checkUserFolderName(path, username);

        minioRepository.deleteFile(path);
    }

    private void checkUserFolderName(String path, String username) {
        String userRoot = minioRepository.getUserRootFolderName(username);
        if (!path.startsWith(userRoot)) {
            throw new ResourceNotFoundException("The file is not found");
        }
    }

    public InputStreamResource downloadFile(String path, String username) {
        return null;
    }
}
