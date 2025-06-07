package ru.eternallyu.cloudfilestorage.dto.file;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long size;

    private String type;

}
