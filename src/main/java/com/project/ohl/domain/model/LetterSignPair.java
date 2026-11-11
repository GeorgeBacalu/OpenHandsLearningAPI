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
public class LetterSignPair {

    private UUID id;
    private String letter;
    private Integer imageId;
}
