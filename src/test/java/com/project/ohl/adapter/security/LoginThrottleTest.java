package com.project.ohl.adapter.security;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginThrottleTest {
    @Test
    void simultaneousRequestsCannotExceedTheLoginLimit() throws Exception {
        var throttle = new LoginThrottle();
        var start = new CountDownLatch(1);
        var results = new ArrayList<Future<Boolean>>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 20; i++) results.add(executor.submit(() -> {
                start.await();
                try { throttle.check("same-ip:account@example.test"); return true; }
                catch (ResponseStatusException exception) { return false; }
            }));
            start.countDown();
            int accepted = 0;
            for (var result : results) if (result.get()) accepted++;
            assertThat(accepted).isEqualTo(5);
        }
        throttle.succeeded("same-ip:account@example.test");
        throttle.check("same-ip:account@example.test");
    }

    @Test
    void registrationLimitIsSharedByAllEmailsFromAnAddress() {
        var throttle = new LoginThrottle();
        for (int i = 0; i < 30; i++) throttle.checkRegistration("same-ip");
        assertThatThrownBy(() -> throttle.checkRegistration("same-ip"))
                .isInstanceOf(ResponseStatusException.class);
        throttle.checkRegistration("another-ip");
    }
}
