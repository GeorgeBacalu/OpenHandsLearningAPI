package com.project.ohl.mock;

import com.project.ohl.domain.model.Notification;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.ohl.TestIds.id;
import static com.project.ohl.mock.UserMock.getMockedUser1;
import static com.project.ohl.mock.UserMock.getMockedUser2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationMock {

    public static List<Notification> getMockedNotifications() {
        return List.of(getMockedNotification1(), getMockedNotification2());
    }

    public static Notification getMockedNotification1() {
        return Notification.builder()
              .id(id(1))
              .message("test_notification_message1")
              .isRead(false)
              .sentAt(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
              .sender(getMockedUser1())
              .receiver(getMockedUser2())
              .build();
    }

    public static Notification getMockedNotification2() {
        return Notification.builder()
              .id(id(2))
              .message("test_notification_message2")
              .isRead(false)
              .sentAt(LocalDateTime.of(2000, 1, 2, 0, 0, 0))
              .sender(getMockedUser2())
              .receiver(getMockedUser1())
              .build();
    }
}
