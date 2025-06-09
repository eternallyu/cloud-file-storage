package ru.eternallyu.cloudfilestorage.http.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import ru.eternallyu.cloudfilestorage.dto.file.FileInfoDto;
import ru.eternallyu.cloudfilestorage.security.CustomUserDetails;
import ru.eternallyu.cloudfilestorage.service.ResourceService;

import java.util.List;

import static ru.eternallyu.cloudfilestorage.util.ControllerUtils.setContentDisposition;

@Slf4j
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class FileController {

    private final ResourceService resourceService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    FileInfoDto getFileInfo(@RequestParam String path, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String username = customUserDetails.getUsername();
        log.info("Getting file info, username={}", username);
        return resourceService.getFileInfo(path, username);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteFile(@RequestParam String path, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String username = customUserDetails.getUsername();
        log.info("Deleting file, username={}", username);
        resourceService.deleteFile(path, username);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<FileInfoDto> createFile(@RequestParam("path") String path, @RequestPart("object") List<MultipartFile> file, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String username = customUserDetails.getUsername();
        log.info("Creating file, username={}", username);
        return resourceService.uploadResources(file, path, username);
    }


    @GetMapping("/download")
    @ResponseStatus(HttpStatus.OK)
    InputStreamResource downloadFile(@RequestParam String path, @AuthenticationPrincipal CustomUserDetails customUserDetails, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        setContentDisposition(path, response);

        String username = customUserDetails.getUsername();
        log.info("Downloading file, username={}", username);

        return resourceService.downloadFile(path, username);
    }

    @GetMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    FileInfoDto moveOrRenameResource(@RequestParam String from, @RequestParam String to, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String username = customUserDetails.getUsername();
        log.info("Moving or renaming file, username={}", username);

        resourceService.moveOrRenameResource(from, to, username);
        return resourceService.getFileInfo(to, username);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    List<FileInfoDto> search(@RequestParam String query, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String username = customUserDetails.getUsername();
        log.info("Searching file, username={}", username);
        return resourceService.searchByQuery(query, username);
    }
}
