package ru.eternallyu.cloudfilestorage.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoDto {

    private String path;

    private String name;

    private Long size;

    private String type;

}
