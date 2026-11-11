package com.project.ohl.adapter.persistence.letter_sign_pair;

import com.project.ohl.adapter.persistence.PersistentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "letter_sign_pairs")
public class LetterSignPairEntity extends PersistentEntity {
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = Integer.MAX_VALUE;
    @Column(name = "letter", nullable = false, unique = true)
    private String letter;
    @Column(name = "media_key", nullable = false)
    private String mediaKey;
}
