package com.project.ohl.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Section {

    private UUID id;
    private String name;
    private String code;
    private String mediaKey;
    private String iconKey;
    private Integer iconId;
    private Integer imageId;
}
