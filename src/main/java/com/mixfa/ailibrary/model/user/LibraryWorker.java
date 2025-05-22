package com.mixfa.ailibrary.model.user;

import com.mixfa.ailibrary.model.Library;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document("account")
public class LibraryWorker extends Account {
    @DBRef
    private final Library library;

    @Override
    public boolean isWorkerOfLibrary(Library library) {
        return this.library.name().equals(library.name());
    }

    public LibraryWorker(Account account, Library library) {
        super(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getRole()
        );
        this.library = library;
    }

    @PersistenceCreator
    public LibraryWorker(String id, String username, String email, Role role, Library library) {
        super(id, username, email, role);
        this.library = library;
    }
}
