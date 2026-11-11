package com.project.llt.notification;

import com.project.llt.exception.ResourceNotFoundException;
import com.project.llt.user.User;
import com.project.llt.user.UserService;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static com.project.llt.constants.ExceptionMessageConstants.NOTIFICATION_NOT_FOUND;
import static com.project.llt.constants.IdentifierConstants.INVALID_ID;
import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.NotificationMapper.convertToDto;
import static com.project.llt.mock.NotificationMock.getMockedNotification1;
import static com.project.llt.mock.NotificationMock.getMockedNotification2;
import static com.project.llt.mock.NotificationMock.getMockedNotifications;
import static com.project.llt.mock.UserMock.getMockedUser1;
import static com.project.llt.mock.UserMock.getMockedUser2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private NotificationDao notificationDao;

    @Mock
    private UserService userService;

    @Spy
    private ModelMapper modelMapper;

    @Mock
    private Clock clock;

    private static final ZonedDateTime zonedDateTime = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Bucharest"));

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private Notification notification1;
    private Notification notification2;
    private List<Notification> notifications;
    private User user1;
    private User user2;
    private NotificationDto notificationDto1;
    private NotificationDto notificationDto2;
    private List<NotificationDto> notificationDtos;

    @BeforeEach
    void setUp() {
        notification1 = getMockedNotification1();
        notification2 = getMockedNotification2();
        notifications = getMockedNotifications();
        user1 = getMockedUser1();
        user2 = getMockedUser2();
        notificationDto1 = convertToDto(modelMapper, notification1);
        notificationDto2 = convertToDto(modelMapper, notification2);
        notificationDtos = notifications.stream().map(notification -> convertToDto(modelMapper, notification)).toList();
    }

    @Test
    void getAllNotifications_shouldReturnListOfNotifications() {
        given(notificationDao.findAll()).willReturn(notifications);
        List<NotificationDto> result = notificationService.getAllNotifications();
        assertThat(result).isEqualTo(notificationDtos);
    }

    @Test
    void getNotificationById_withValidId_shouldReturnNotificationWithGivenId() {
        given(notificationDao.findById(anyLong())).willReturn(Optional.ofNullable(notification1));
        NotificationDto result = notificationService.getNotificationById(VALID_ID);
        assertThat(result).isEqualTo(notificationDto1);
    }

    @Test
    void getNotificationById_witInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> notificationService.getNotificationById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(NOTIFICATION_NOT_FOUND, INVALID_ID));
    }

    @Test
    void saveNotification_shouldAddNotificationToList() {
        given(clock.getZone()).willReturn(zonedDateTime.getZone());
        given(clock.instant()).willReturn(zonedDateTime.toInstant());
        notification1.setSentAt(zonedDateTime.toLocalDateTime());
        given(notificationDao.save(any(Notification.class))).willReturn(notification1);
        NotificationDto result = notificationService.saveNotification(notificationDto1);
        verify(notificationDao).save(notificationCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, notificationCaptor.getValue()));
    }

    @Test
    void updateNotificationById_withValidId_shouldUpdateNotificationWithGivenId() {
        Notification notification = notification2; notification.setId(VALID_ID);
        notification.setSentAt(notification1.getSentAt());
        given(notificationDao.findById(anyLong())).willReturn(Optional.ofNullable(notification1));
        given(userService.getUserEntityById(notificationDto2.getSenderId())).willReturn(user2);
        given(userService.getUserEntityById(notificationDto2.getReceiverId())).willReturn(user1);
        given(notificationDao.update(any(Notification.class))).willReturn(notification);
        NotificationDto result = notificationService.updateNotificationById(notificationDto2, VALID_ID);
        verify(notificationDao).update(notificationCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, notificationCaptor.getValue()));
    }

    @Test
    void updateNotificationById_witInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> notificationService.getNotificationById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(NOTIFICATION_NOT_FOUND, INVALID_ID));
        verify(notificationDao, never()).update(any(Notification.class));
    }

    @Test
    void deleteNotificationById_withValidId_shouldRemoveNotificationWithGivenIdFromList() {
        given(notificationDao.findById(anyLong())).willReturn(Optional.ofNullable(notification1));
        notificationService.deleteNotificationById(VALID_ID);
        verify(notificationDao).delete(notification1);
    }

    @Test
    void deleteNotificationById_witInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> notificationService.deleteNotificationById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(NOTIFICATION_NOT_FOUND, INVALID_ID));
        verify(notificationDao, never()).delete(any(Notification.class));
    }
}
