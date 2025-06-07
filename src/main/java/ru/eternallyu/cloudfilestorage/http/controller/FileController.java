package ru.eternallyu.cloudfilestorage.http.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class FileController {

    private final ResourceService resourceService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    FileInfoDto
    getFileInfo(
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        String username = customUserDetails.getUsername();
        return resourceService.getFileInfo(path, username);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteFile(
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        String username = customUserDetails.getUsername();
        resourceService.deleteFile(path, username);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<FileInfoDto> createFile(
            @RequestParam("path") String path,
            @RequestPart("object") List<MultipartFile> file,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        long maxFileSize = 5 * 1024 * 1024;
        if (file.size() > maxFileSize) {
            throw new MaxUploadSizeExceededException(maxFileSize);
        }
        String username = customUserDetails.getUsername();
        return resourceService.uploadResources(file, path, username);
    }


    @GetMapping("/download")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    InputStreamResource
    downloadFile(
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            HttpServletResponse response
    ) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        setContentDisposition(path, response);

        String username = customUserDetails.getUsername();

        return resourceService.downloadFile(path, username);
    }

    @GetMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    FileInfoDto
    moveOrRenameResource(
            @RequestParam String from,
            @RequestParam String to,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        String username = customUserDetails.getUsername();

        resourceService.moveOrRenameResource(from, to, username);

        return resourceService.getFileInfo(to, username);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    List<FileInfoDto>
    search(
            @RequestParam String query,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        String username = customUserDetails.getUsername();

        return resourceService.searchByQuery(query, username);
    }

    private static void setContentDisposition(String path, HttpServletResponse response) {
        String filename;
        if (path.endsWith("/")) {
            String withoutSlash = path.substring(0, path.length() - 1);
            filename = withoutSlash.substring(withoutSlash.lastIndexOf('/') + 1);
        } else {
            filename = path.substring(path.lastIndexOf('/') + 1);
        }

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
    }
}
