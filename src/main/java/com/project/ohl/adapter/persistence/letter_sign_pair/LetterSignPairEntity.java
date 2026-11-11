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
    @Column(name = "letter")
    private String letter;
    @Column(name = "image_id")
    private Integer imageId;
}
