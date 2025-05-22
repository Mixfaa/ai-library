package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.service.AdminAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Slf4j
@Service
public class AdminAuthenticatorImpl implements AdminAuthenticator {
    private final Map<String, String> adminEmails = new HashMap<>();

    public AdminAuthenticatorImpl() {
        try {
            var bundle = ResourceBundle.getBundle("adminemails");
            var adminNameIter = bundle.getKeys().asIterator();
            while (adminNameIter.hasNext()) {
                var name = adminNameIter.next();
                var email = bundle.getString(name);

                adminEmails.put(name, email);
            }
        } catch (Exception e) {
            log.error("Failed to load admin emails", e);
        }
    }

    @Override
    public boolean isAdmin(String email) {
        var isAdmin = adminEmails.values().stream().anyMatch(adminEmail -> adminEmail.equals(email));

        if (isAdmin)
            log.info("Admin authenticated: {}", email);

        return isAdmin;
    }
}
