package ru.eternallyu.cloudfilestorage.service;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.eternallyu.cloudfilestorage.config.minio.MinioProperties;
import ru.eternallyu.cloudfilestorage.dto.file.FileInfoDto;
import ru.eternallyu.cloudfilestorage.error.BadRequestException;
import ru.eternallyu.cloudfilestorage.error.ResourceNotFoundException;
import ru.eternallyu.cloudfilestorage.error.StorageException;
import ru.eternallyu.cloudfilestorage.repository.MinioRepository;

import java.util.ArrayList;
import java.util.List;

import static ru.eternallyu.cloudfilestorage.util.PathValidator.validatePath;
import static ru.eternallyu.cloudfilestorage.util.PathValidator.validateQuery;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MinioRepository minioRepository;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

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
        validatePath(path);

        checkUserFolderName(path, username);

        InputStreamResource downloadedResource;

        boolean isDirectory = path.endsWith("/");
        if (isDirectory) {
            downloadedResource = minioRepository.downloadFolder(path);
        } else {
            downloadedResource = minioRepository.downloadFile(path);
        }
        return downloadedResource;
    }

    public void moveOrRenameResource(String from, String to, String username) {
        validatePath(from);
        validatePath(to);

        checkUserFolderName(from, username);
        checkUserFolderName(to, username);

        boolean isDirectory = from.endsWith("/");
        if (isDirectory) {
            minioRepository.renameFolder(from, to);
        } else {
            minioRepository.renameFile(from, to);
        }
    }

    public List<FileInfoDto> searchByQuery(String query, String username) {

        validateQuery(query);

        return minioRepository.searchInUserSpace(query, username);
    }

    public List<FileInfoDto> uploadResource(MultipartFile file, String path, String username) {
        validatePath(path);

        checkUserFolderName(path, username);

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("File name is empty");
        }

        String fullPath = path + fileName;
        minioRepository.uploadFile(fullPath, file);

        List<FileInfoDto> result = new ArrayList<>();

        if (fileName.contains("/")) {
            String folderRelative = fileName.substring(0, fileName.indexOf('/') + 1);
            String folderFullPath = path + folderRelative;

            FileInfoDto folderInfo = minioRepository.getResourceInfo(folderFullPath);
            result.add(folderInfo);
        }

        FileInfoDto fileInfo = minioRepository.getResourceInfo(fullPath);
        result.add(fileInfo);

        return result;
    }

    public List<FileInfoDto> getDirectoryInfo(String path, String username) {
        validatePath(path);

        checkUserFolderName(path, username);

        String userRoot = minioRepository.getUserRootFolderName(username);

        return getFileInfoDtoList(path, userRoot);
    }

    public FileInfoDto createEmptyDirectory(String path, String username) {
        validatePath(path);
        if (!path.endsWith("/")) {
            throw new BadRequestException("Path for creating directory must end with '/'");
        }

        checkUserFolderName(path, username);

        String parent = path.substring(0, path.lastIndexOf('/', path.length() - 2) + 1);

        boolean parentExists;
        try {

            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(parent)
                            .build()
            );
            parentExists = true;
        } catch (ErrorResponseException exception) {
            if ("NoSuchKey".equals(exception.errorResponse().code())) {
                Iterable<Result<Item>> children = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(minioProperties.getBucket())
                                .prefix(parent)
                                .recursive(false)
                                .build()
                );
                parentExists = children.iterator().hasNext();
            } else {
                throw new StorageException("MinIO error: " + exception.errorResponse().message());
            }
        } catch (Exception exception) {
            throw new StorageException("Error checking parent directory: " + exception.getMessage());
        }

        if (!parentExists) {
            throw new ResourceNotFoundException("Parent directory not found");
        }

        minioRepository.createDirectory(path);

        return minioRepository.getResourceInfo(path);
    }

    private List<FileInfoDto> getFileInfoDtoList(String path, String userRoot) {
        Iterable<Result<Item>> objectsInDir = minioRepository.getDirectoriesResources(path);

        List<FileInfoDto> result = new ArrayList<>();
        for (Result<Item> maybeItem : objectsInDir) {
            try {
                Item item = maybeItem.get();
                String fullObjectName = item.objectName();
                boolean isDirectory = item.isDir();

                String relative = fullObjectName.substring(userRoot.length());

                int lastSlash = relative.lastIndexOf('/');
                String parentRelativePath = (lastSlash >= 0)
                        ? relative.substring(0, lastSlash + 1)
                        : "";

                String namePart = MinioRepository.getNamePart(isDirectory, parentRelativePath);

                Long sizeOrNull = isDirectory ? null : item.size();

                String type = isDirectory ? "DIRECTORY" : "FILE";

                FileInfoDto dto = FileInfoDto.builder()
                        .path(parentRelativePath)
                        .name(namePart)
                        .size(sizeOrNull)
                        .type(type)
                        .build();
                result.add(dto);
            } catch (Exception exception) {
                throw new StorageException("Minio error while listing directory");
            }
        }
        return result;
    }
}
