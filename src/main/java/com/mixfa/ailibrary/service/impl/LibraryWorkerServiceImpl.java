package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.LibraryWorker;
import com.mixfa.ailibrary.model.user.Role;
import com.mixfa.ailibrary.service.LibraryService;
import com.mixfa.ailibrary.service.LibraryWorkerServce;
import com.mixfa.ailibrary.service.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryWorkerServiceImpl implements LibraryWorkerServce {
    private final LibraryService libraryService;
    private final AccountRepo accountRepo;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void addToLibrary(String userId, String libraryId) {
        var library = libraryService.findOrThrow(libraryId);
        var user = accountRepo.findById(userId).get();

        if (user.getRole() == Role.WORKER || user.getRole() == Role.ADMIN)
            throw ExceptionType.userAlreadyWorker();

        var updatedUser = new LibraryWorker(user.withRole(Role.WORKER), library);
        accountRepo.save(updatedUser);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void removeFromLibrary(String userId) {
        var user = accountRepo.findById(userId).orElseThrow();
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.USER) return;

        var updatedUser = new Account(user.getId(), user.getUsername(), user.getEmail(), Role.USER);
        accountRepo.save(updatedUser);
    }
}
