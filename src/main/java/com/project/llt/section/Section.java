package com.project.llt.section;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Section {

    private Long id;
    private String name;
    private Integer iconId;
    private Integer imageId;
}
