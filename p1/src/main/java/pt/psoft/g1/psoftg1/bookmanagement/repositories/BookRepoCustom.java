package pt.psoft.g1.psoftg1.bookmanagement.repositories;

import java.util.List;


import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;

public interface BookRepoCustom {
    List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, SearchBooksQuery query);
}