package ru.eternallyu.cloudfilestorage.repository;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import ru.eternallyu.cloudfilestorage.config.minio.MinioProperties;
import ru.eternallyu.cloudfilestorage.dto.file.FileInfoDto;
import ru.eternallyu.cloudfilestorage.entity.User;
import ru.eternallyu.cloudfilestorage.error.ResourceAlreadyExistsException;
import ru.eternallyu.cloudfilestorage.error.ResourceNotFoundException;
import ru.eternallyu.cloudfilestorage.error.StorageException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Repository
@RequiredArgsConstructor
public class MinioRepository {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    private final UserRepository userRepository;

    @PostConstruct
    public void createBucketIfNotExist() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build());
            }
        } catch (Exception exception) {
            throw new StorageException("Error during creation bucket: " + exception.getMessage());
        }

    }

    public void createDirectory(String fullPath) {
        checkEmptinessOfPath(fullPath);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fullPath)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .contentType("application/x-directory")
                            .build()
            );
        } catch (Exception exception) {
            throw new StorageException("Error during creation directory: " + exception.getMessage());
        }
    }


    public void uploadFile(String path, MultipartFile file) {

        checkEmptinessOfPath(path);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(path)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception exception) {
            throw new StorageException("Error during upload file: " + exception.getMessage());
        }
    }

    public InputStreamResource downloadFile(String path) {
        try {
            return new InputStreamResource(minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(path)
                            .build())
            );
        } catch (ErrorResponseException exception) {
            throwResourceNotFoundExceptionIfNotFound(path, exception);
            throw new StorageException("MinIO error during downloadFile: " + exception.errorResponse().message());
        } catch (Exception exception) {
            throw new StorageException("Error during download file: " + exception.getMessage());
        }
    }

    public void deleteFile(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(path)
                            .build()
            );
        } catch (ErrorResponseException exception) {

            throwResourceNotFoundExceptionIfNotFound(path, exception);
            throw new StorageException("MinIO error during removeFile: " + exception.errorResponse().message());
        } catch (Exception exception) {
            throw new StorageException("Error during download file: " + exception.getMessage());
        }
    }

    public Iterable<Result<Item>> getDirectoriesResources(String prefix) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .prefix(prefix)
                        .recursive(false)
                        .build()
        );
    }

    public void renameFile(String oldPath, String newPath) {
        checkEmptinessOfPath(newPath);

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(oldPath)
                    .build());
        } catch (ErrorResponseException exception) {
            throwResourceNotFoundExceptionIfNotFound(oldPath, exception);
            throw new StorageException("MinIO error: " + exception.errorResponse().message());
        } catch (Exception exception) {
            throw new StorageException("Error checking old path: " + exception.getMessage());
        }

        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder()
                                    .bucket(minioProperties.getBucket())
                                    .object(oldPath)
                                    .build())
                            .bucket(minioProperties.getBucket())
                            .object(newPath)
                            .build()
            );
        } catch (ErrorResponseException exception) {

            throwResourceNotFoundExceptionIfNotFound(oldPath, exception);
            throw new StorageException("MinIO error during downloadFile: " + exception.errorResponse().message());
        } catch (Exception exception) {
            throw new StorageException("Error during download file: " + exception.getMessage());
        }
        deleteFile(oldPath);
    }

    public void renameFolder(String oldPath, String newPath) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .prefix(oldPath).recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                String oldObjectName = item.objectName();
                String newObjectName = oldObjectName.replaceFirst(oldPath, newPath);

                renameFile(oldObjectName, newObjectName);

            }
        } catch (Exception exception) {
            throw new StorageException("Error during rename folder: " + exception.getMessage());
        }
    }

    public InputStreamResource downloadFolder(String path) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .prefix(path)
                            .recursive(true)
                            .build()
            );

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
                for (Result<Item> result : results) {
                    Item item = result.get();
                    String fullObjectName = item.objectName();
                    String relativeObjectName = fullObjectName.substring(path.length());

                    if (fullObjectName.endsWith("/")) {
                        ZipEntry zipEntry = new ZipEntry(relativeObjectName);
                        zipOutputStream.putNextEntry(zipEntry);
                        zipOutputStream.closeEntry();
                    } else {
                        ZipEntry entry = new ZipEntry(relativeObjectName);
                        zipOutputStream.putNextEntry(entry);

                        try (InputStream is = minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(minioProperties.getBucket())
                                        .object(fullObjectName)
                                        .build()
                        )) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = is.read(buffer)) > 0) {
                                zipOutputStream.write(buffer, 0, len);
                            }
                        }
                        zipOutputStream.closeEntry();
                    }
                }
            }
            byte[] zipBytes = byteArrayOutputStream.toByteArray();

            return new InputStreamResource(new ByteArrayInputStream(zipBytes));
        } catch (ErrorResponseException exception) {

            throwResourceNotFoundExceptionIfNotFound(path, exception);
            throw new StorageException("MinIO error during downloadFile: " + exception.errorResponse().message());
        } catch (Exception exception) {
            throw new StorageException("Error during download file: " + exception.getMessage());
        }
    }

    public FileInfoDto getResourceInfo(String path) {
        boolean isDirectory = isDirectory(path);

        try {
            StatObjectResponse statObjectArgs = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(path)
                    .build());

            String parentPath = getParentPath(path);
            String name = getFileName(path);

            return new FileInfoDto(parentPath,
                    name,
                    isDirectory ? null : statObjectArgs.size(),
                    isDirectory ? "DIRECTORY" : "FILE");
        } catch (ErrorResponseException exception) {

            throwResourceNotFoundExceptionIfNotFound(path, exception);
            throw new StorageException("MinIO error during getting file: " + exception.errorResponse().message());
        } catch (Exception exception) {
            throw new StorageException("Error during getting file: " + exception.getMessage());
        }
    }

    public List<FileInfoDto> searchInUserSpace(String query, String username) {

        String userPathPrefix = getUserRootFolderName(username);

        List<FileInfoDto> fileInfoDtos = new ArrayList<>();

        try {
            Iterable<Result<Item>> allItems = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .prefix(userPathPrefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : allItems) {
                Item item = result.get();
                String fullObjectName = item.objectName();

                boolean isDirectory = isDirectory(fullObjectName);

                String relative = fullObjectName.substring(userPathPrefix.length());

                String namePart = getNamePart(isDirectory, relative);

                if (namePart.toLowerCase().contains(query.toLowerCase())) {
                    int lastSlash = relative.lastIndexOf('/');
                    String parentRelativePath = (lastSlash >= 0)
                            ? relative.substring(0, lastSlash + 1)
                            : "";

                    FileInfoDto dto = FileInfoDto.builder()
                            .path(parentRelativePath)
                            .name(namePart)
                            .size(isDirectory ? null : item.size())
                            .type(isDirectory ? "DIRECTORY" : "FILE")
                            .build();

                    fileInfoDtos.add(dto);
                }
            }
        } catch (Exception exception) {
            throw new StorageException("Error during search in user space: " + exception.getMessage());
        }

        return fileInfoDtos;
    }

    public static String getNamePart(boolean isDirectory, String relative) {
        String namePart;
        if (isDirectory) {
            String withoutSlash = relative.substring(0, relative.length() - 1);
            namePart = withoutSlash.substring(withoutSlash.lastIndexOf('/') + 1);
        } else {
            namePart = relative.substring(relative.lastIndexOf('/') + 1);
        }
        return namePart;
    }


    public String getUserRootFolderName(String name) {
        Optional<User> user = userRepository.findByUsername(name);
        if (user.isPresent()) {
            Integer userId = user.get().getId();
            return String.format("user-%d-files/", userId);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    private static String getFileName(String path) {
        String parentPath = getParentPath(path);
        String withoutParent = path.substring(parentPath.length());
        return withoutParent.endsWith("/") ?
                withoutParent.substring(0, withoutParent.length() - 1) :
                withoutParent;
    }

    private static String getParentPath(String path) {
        if (!path.contains("/")) return "";
        return path.substring(0, path.lastIndexOf('/') + 1);
    }

    private static boolean isDirectory(String path) {
        return path.endsWith("/");
    }

    private static void throwResourceNotFoundExceptionIfNotFound(String path, ErrorResponseException exception) {
        if ("NoSuchKey".equals(exception.errorResponse().code())) {
            throw new ResourceNotFoundException("File '" + path + "' not found");
        }
    }

    private void checkEmptinessOfPath(String newPath) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(newPath)
                    .build()
            );

            throw new ResourceAlreadyExistsException("Resource already exists");
        } catch (ErrorResponseException exception) {
            if (!"NoSuchKey".equals(exception.errorResponse().code())) {
                throw new ResourceNotFoundException("Minio error: " + exception.errorResponse().message());
            }
        } catch (Exception exception) {
            throw new StorageException("Error checking new path: " + exception.getMessage());
        }
    }
}
