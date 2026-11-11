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
import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {
    private final CategoryMapper categories = Mappers.getMapper(CategoryMapper.class);
    private final UserMapper users = Mappers.getMapper(UserMapper.class);
    private final NotificationMapper notifications = Mappers.getMapper(NotificationMapper.class);
    private final FeedbackMapper feedback = Mappers.getMapper(FeedbackMapper.class);

    @Test
    void categoryMappingPreservesParentIdWithoutCopyingTheParentGraph() {
        var model = Category.builder().id(17L).name("Child").imageId(42)
                .isExpanded(true).isFavorite(false)
                .parent(Category.builder().id(3L).name("Parent").build())
                .section(Section.builder().id(9L).name("Video").build()).build();
        var dto = categories.convertToDtoList(List.of(model)).getFirst();
        assertThat(dto.getId()).isEqualTo(17L);
        assertThat(dto.getParentId()).isEqualTo(3L);
        assertThat(dto.getSectionId()).isEqualTo(9L);
        assertThat(dto.getIsExpanded()).isTrue();
        assertThat(dto.getIsFavorite()).isFalse();
        var restored = categories.convertToEntity(dto);
        assertThat(restored.getParent().getId()).isEqualTo(3L);
        assertThat(restored.getParent().getName()).isNull();
        assertThat(restored.getSection().getId()).isEqualTo(9L);
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
        assertThat(categories.convertToDto(Category.builder().id(17L).build()).getParentId()).isNull();
    }

    @Test
    void distinctRelatedIdsAreMappedToTheCorrectReferences() {
        var user = users.convertToEntity(UserDto.builder().institutionId(19L).roleId(4L).build());
        assertThat(user.getInstitution().getId()).isEqualTo(19L);
        assertThat(user.getRole().getId()).isEqualTo(4L);
        var notification = notifications.convertToEntity(NotificationDto.builder().senderId(43L).receiverId(56L).build());
        assertThat(notification.getSender().getId()).isEqualTo(43L);
        assertThat(notification.getReceiver().getId()).isEqualTo(56L);
        var dto = notifications.convertToDto(notification);
        assertThat(dto.getSenderId()).isEqualTo(43L);
        assertThat(dto.getReceiverId()).isEqualTo(56L);
        assertThat(feedback.convertToEntity(FeedbackDto.builder().userId(71L).build()).getUser().getId()).isEqualTo(71L);
    }
}
