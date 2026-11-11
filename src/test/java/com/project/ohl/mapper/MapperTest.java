package com.project.ohl.mapper;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.mapper.FeedbackMapper;
import com.project.ohl.application.mapper.NotificationMapper;
import com.project.ohl.application.mapper.UserMapper;
import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.model.FeedbackDto;
import com.project.ohl.application.model.NotificationDto;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.domain.model.Category;
import com.project.ohl.domain.model.Section;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static com.project.ohl.TestIds.id;
import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {
    private final CategoryMapper categories = Mappers.getMapper(CategoryMapper.class);
    private final UserMapper users = Mappers.getMapper(UserMapper.class);
    private final NotificationMapper notifications = Mappers.getMapper(NotificationMapper.class);
    private final FeedbackMapper feedback = Mappers.getMapper(FeedbackMapper.class);

    @Test
    void categoryMappingPreservesParentIdWithoutCopyingTheParentGraph() {
        var model = Category.builder().id(id(17)).name("Child")
                .isExpanded(true).isFavorite(false)
                .parent(Category.builder().id(id(3)).name("Parent").build())
                .section(Section.builder().id(id(9)).name("Video").build()).build();
        var dto = categories.convertToDtoList(List.of(model)).getFirst();
        assertThat(dto.getId()).isEqualTo(id(17));
        assertThat(dto.getParentId()).isEqualTo(id(3));
        assertThat(dto.getSectionId()).isEqualTo(id(9));
        assertThat(dto.getIsExpanded()).isTrue();
        assertThat(dto.getIsFavorite()).isFalse();
        var restored = categories.convertToEntity(dto);
        assertThat(restored.getParent().getId()).isEqualTo(id(3));
        assertThat(restored.getParent().getName()).isNull();
        assertThat(restored.getSection().getId()).isEqualTo(id(9));
        assertThat(restored.getSection().getName()).isNull();
    }

    @Test
    void nullForeignKeysRemainNullReferences() {
        var category = categories.convertToEntity(new CategoryDto());
        assertThat(category.getParent()).isNull();
        assertThat(category.getSection()).isNull();
        var user = users.convertToEntity(new UserDto());
        assertThat(user.getInstitution()).isNull();
        assertThat(user.getRole()).isNull();
        var notification = notifications.convertToEntity(new NotificationDto());
        assertThat(notification.getSender()).isNull();
        assertThat(notification.getReceiver()).isNull();
        assertThat(feedback.convertToEntity(new FeedbackDto()).getUser()).isNull();
        assertThat(categories.convertToDto(Category.builder().id(id(17)).build()).getParentId()).isNull();
    }

    @Test
    void distinctRelatedIdsAreMappedToTheCorrectReferences() {
        var user = users.convertToEntity(UserDto.builder().institutionId(id(19)).roleId(id(4)).build());
        assertThat(user.getInstitution().getId()).isEqualTo(id(19));
        assertThat(user.getRole().getId()).isEqualTo(id(4));
        var notification = notifications.convertToEntity(NotificationDto.builder().senderId(id(43)).receiverId(id(56)).build());
        assertThat(notification.getSender().getId()).isEqualTo(id(43));
        assertThat(notification.getReceiver().getId()).isEqualTo(id(56));
        var dto = notifications.convertToDto(notification);
        assertThat(dto.getSenderId()).isEqualTo(id(43));
        assertThat(dto.getReceiverId()).isEqualTo(id(56));
        assertThat(feedback.convertToEntity(FeedbackDto.builder().userId(id(71)).build()).getUser().getId()).isEqualTo(id(71));
    }
}
