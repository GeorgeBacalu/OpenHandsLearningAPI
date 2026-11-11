package com.project.llt.mock;

import com.project.llt.notification.Notification;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.llt.mock.UserMock.getMockedUser1;
import static com.project.llt.mock.UserMock.getMockedUser2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationMock {

    public static List<Notification> getMockedNotifications() {
        return List.of(getMockedNotification1(), getMockedNotification2());
    }

    public static Notification getMockedNotification1() {
        return Notification.builder()
              .id(1L)
              .message("test_notification_message1")
              .isRead(false)
              .sentAt(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
              .sender(getMockedUser1())
              .receiver(getMockedUser2())
              .build();
    }

    public static Notification getMockedNotification2() {
        return Notification.builder()
              .id(2L)
              .message("test_notification_message2")
              .isRead(false)
              .sentAt(LocalDateTime.of(2000, 1, 2, 0, 0, 0))
              .sender(getMockedUser2())
              .receiver(getMockedUser1())
              .build();
    }
}
