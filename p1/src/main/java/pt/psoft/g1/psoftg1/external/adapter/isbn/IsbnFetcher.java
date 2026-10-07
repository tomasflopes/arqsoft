package pt.psoft.g1.psoftg1.external.adapter.isbn;

import pt.psoft.g1.psoftg1.bookmanagement.model.Isbn;

public interface IsbnFetcher {
  Isbn fetchIsbnByTitle(String title);
}
