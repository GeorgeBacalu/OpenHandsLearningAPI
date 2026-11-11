package com.project.llt.notification;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.NotificationMapper.convertToDto;
import static com.project.llt.mock.NotificationMock.getMockedNotification1;
import static com.project.llt.mock.NotificationMock.getMockedNotification2;
import static com.project.llt.mock.NotificationMock.getMockedNotifications;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationService notificationService;

    @Spy
    private ModelMapper modelMapper;

    private NotificationDto notificationDto1;
    private NotificationDto notificationDto2;
    private List<NotificationDto> notificationDtos;

    @BeforeEach
    void setUp() {
        notificationDto1 = convertToDto(modelMapper, getMockedNotification1());
        notificationDto2 = convertToDto(modelMapper, getMockedNotification2());
        notificationDtos = getMockedNotifications().stream().map(Notification -> convertToDto(modelMapper, Notification)).toList();
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
