package com.project.ohl.application.port.in;

import com.project.ohl.application.model.Account;
import com.project.ohl.application.model.Registration;
import java.util.UUID;

public interface AccountService {
    Account authenticate(String email, String password);
    Account register(Registration registration);
    Account current(UUID id);
}
