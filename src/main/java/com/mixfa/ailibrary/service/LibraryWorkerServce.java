package com.mixfa.ailibrary.service;

public interface LibraryWorkerServce {
    void addToLibrary(String userId, String libraryId);

    void removeFromLibrary(String userId);
}
