package com.mixfa.ailibrary.service.user;

import com.mixfa.ailibrary.model.user.Account;

/**
 * Service interface for Account management operations
 */
public interface AccountService {
    /**
     * Find an account by ID or throw an exception if not found
     * 
     * @param id the account ID
     * @return the account
     * @throws java.util.NoSuchElementException if the account is not found
     */
    Account findOrThrow(String id);

    /**
     * Gets an existing account or creates a new one if it doesn't exist
     * 
     * @param id the account ID
     * @param email the email
     * @param name the username
     * @return the existing or newly created account
     */
    Account getOrCreateAccount(String id, String email, String name);
}
