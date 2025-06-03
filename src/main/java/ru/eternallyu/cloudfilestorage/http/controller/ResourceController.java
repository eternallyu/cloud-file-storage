package ru.eternallyu.cloudfilestorage.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.eternallyu.cloudfilestorage.dto.file.FileInfoDto;
import ru.eternallyu.cloudfilestorage.security.CustomUserDetails;
import ru.eternallyu.cloudfilestorage.service.ResourceService;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

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

    @GetMapping("/download")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    InputStreamResource
    downloadFile(
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        String username = customUserDetails.getUsername();
        return resourceService.downloadFile(path, username);
    }
}
