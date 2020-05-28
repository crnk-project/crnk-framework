package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Document;
import io.crnk.core.repository.InMemoryResourceRepository;

public class DocumentRepository extends InMemoryResourceRepository<Document, Long> {

    public DocumentRepository() {
        super(Document.class);
    }
}
