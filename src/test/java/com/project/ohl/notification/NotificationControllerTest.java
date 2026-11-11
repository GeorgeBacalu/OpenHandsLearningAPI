package com.project.ohl.notification;

import com.project.ohl.application.mapper.NotificationMapper;
import org.mapstruct.factory.Mappers;
import com.project.ohl.adapter.web.NotificationController;
import com.project.ohl.application.model.NotificationDto;
import com.project.ohl.application.port.in.NotificationService;
import com.project.ohl.domain.model.Notification;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.mock.NotificationMock.getMockedNotification1;
import static com.project.ohl.mock.NotificationMock.getMockedNotification2;
import static com.project.ohl.mock.NotificationMock.getMockedNotifications;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private final NotificationMapper mapper = Mappers.getMapper(NotificationMapper.class);

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationService notificationService;

    private NotificationDto notificationDto1;
    private NotificationDto notificationDto2;
    private List<NotificationDto> notificationDtos;

    @BeforeEach
    void setUp() {
        notificationDto1 = mapper.convertToDto(getMockedNotification1());
        notificationDto2 = mapper.convertToDto(getMockedNotification2());
        notificationDtos = getMockedNotifications().stream().map(Notification -> mapper.convertToDto(Notification)).toList();
    }

    @Test
    void getAllNotifications_shouldReturnListOfNotifications() {
        given(notificationService.getAllNotifications()).willReturn(notificationDtos);
        ResponseEntity<List<NotificationDto>> response = notificationController.getAllNotifications();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(notificationDtos);
    }

    @Test
    void getNotificationById_shouldReturnNotificationWithGivenId() {
        given(notificationService.getNotificationById(anyLong())).willReturn(notificationDto1);
        ResponseEntity<NotificationDto> response = notificationController.getNotificationById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(notificationDto1);
    }

    @Test
    void saveNotification_shouldAddNotificationToList() {
        given(notificationService.saveNotification(any(NotificationDto.class))).willReturn(notificationDto1);
        ResponseEntity<NotificationDto> response = notificationController.saveNotification(notificationDto1);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(notificationDto1);
    }

    @Test
    void updateNotificationById_shouldUpdateNotificationWithGivenId() {
        NotificationDto notificationDto = notificationDto2; notificationDto.setId(VALID_ID);
        given(notificationService.updateNotificationById(any(NotificationDto.class), anyLong())).willReturn(notificationDto);
        ResponseEntity<NotificationDto> response = notificationController.updateNotificationById(notificationDto2, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(notificationDto);
    }

    @Test
    void deleteNotificationById_shouldRemoveNotificationWithGivenIdFromList() {
        ResponseEntity<Void> response = notificationController.deleteNotificationById(VALID_ID);
        verify(notificationService).deleteNotificationById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
