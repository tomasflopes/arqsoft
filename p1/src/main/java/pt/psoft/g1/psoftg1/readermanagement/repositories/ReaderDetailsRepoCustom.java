package pt.psoft.g1.psoftg1.readermanagement.repositories;

import java.util.List;

import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.shared.services.Page;

public interface ReaderDetailsRepoCustom {
  List<ReaderDetails> searchReaderDetails(Page page, SearchReadersQuery query);
}
