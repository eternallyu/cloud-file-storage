package ru.eternallyu.cloudfilestorage.http.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.eternallyu.cloudfilestorage.dto.file.FileInfoDto;
import ru.eternallyu.cloudfilestorage.security.CustomUserDetails;
import ru.eternallyu.cloudfilestorage.service.DirectoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<FileInfoDto> getDirectoryInfo(@RequestParam(defaultValue = "") String path, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String username = customUserDetails.getUsername();
        log.info("Getting directory info: username={}", username);
        return directoryService.getDirectoryInfo(path, username);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    FileInfoDto createEmptyDirectory(@RequestParam @NotBlank String path, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String username = customUserDetails.getUsername();
        log.info("Creating directory: username={}", username);
        return directoryService.createEmptyDirectory(path, username);
    }
}
