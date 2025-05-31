package ru.eternallyu.cloudfilestorage.repository;

import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import ru.eternallyu.cloudfilestorage.config.minio.MinioProperties;
import ru.eternallyu.cloudfilestorage.entity.User;
import ru.eternallyu.cloudfilestorage.error.StorageException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

    public void createUserRootFolder(String name) {
        String userRootFolderName = getUserRootFolderName(name);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(userRootFolderName)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .contentType("application/x-directory")
                            .build()
            );
        } catch (Exception exception) {
            throw new StorageException("Error during creation root folder: " + exception.getMessage());
        }
    }

    public void createUserEmptyDirectory(String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .contentType("application/x-directory")
                            .build()
            );
        } catch (Exception exception) {
            throw new StorageException("Error during creation directory: " + exception.getMessage());
        }
    }

    public void uploadFile(String path, MultipartFile file) {
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
        } catch (Exception exception) {
            throw new StorageException("Error during delete file: " + exception.getMessage());
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
        } catch (Exception exception) {
            throw new StorageException("Error during rename file: " + exception.getMessage());
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
        } catch (Exception exception) {
            throw new StorageException("Error during download folder: " + exception.getMessage());
        }
    }

    private String getUserRootFolderName(String name) {
        Optional<User> user = userRepository.findByUsername(name);
        if (user.isPresent()) {
            Integer userId = user.get().getId();
            return String.format("user-%d-files/", userId);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
