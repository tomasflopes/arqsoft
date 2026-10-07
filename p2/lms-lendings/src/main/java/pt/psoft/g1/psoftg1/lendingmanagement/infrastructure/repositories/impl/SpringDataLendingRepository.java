package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.Reader;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.*;

public interface SpringDataLendingRepository extends LendingRepository, LendingRepoCustom, CrudRepository<Lending, Long> {
    @Override
    @Query("SELECT l FROM Lending l WHERE l.lendingNumber.lendingNumber = :lendingNumber")
    Optional<Lending> findByLendingNumber(String lendingNumber);

    @Override
    @Query("SELECT l FROM Lending l JOIN l.book b JOIN l.reader r WHERE b.isbn = :isbn AND r.readerNumber = :readerNumber")
    List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn);

    @Override
    @Query("SELECT COUNT(l) FROM Lending l WHERE YEAR(l.startDate) = YEAR(CURRENT_DATE)")
    int getCountFromCurrentYear();

    @Override
    @Query("SELECT l FROM Lending l JOIN Reader r ON l.reader.pk = r.pk WHERE r.readerNumber = :readerNumber AND l.returnedDate IS NULL")
    List<Lending> listOutstandingByReaderNumber(@Param("readerNumber") String readerNumber);

    @Override
    @Query(value = "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) FROM Lending l", nativeQuery = true)
    Double getAverageDuration();

    @Override
    @Query(value = "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) FROM Lending l JOIN BOOK b ON l.BOOK_PK = b.PK WHERE b.ISBN = :isbn", nativeQuery = true)
    Double getAvgLendingDurationByIsbn(@Param("isbn") String isbn);
}

interface LendingRepoCustom {
    List<Lending> getOverdue(Page page);
    List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate);
}

@RequiredArgsConstructor
class LendingRepoCustomImpl implements LendingRepoCustom {
    private final EntityManager em;

    @Override
    public List<Lending> getOverdue(Page page) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Lending> cq = cb.createQuery(Lending.class);
        final Root<Lending> root = cq.from(Lending.class);
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();
        where.add(cb.isNull(root.get("returnedDate")));
        where.add(cb.lessThan(root.get("limitDate"), LocalDate.now()));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("limitDate")));

        final TypedQuery<Lending> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        return q.getResultList();
    }

    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Lending> cq = cb.createQuery(Lending.class);
        final Root<Lending> lendingRoot = cq.from(Lending.class);
        final Join<Lending, Book> bookJoin = lendingRoot.join("book");
        final Join<Lending, Reader> readerJoin = lendingRoot.join("reader");
        cq.select(lendingRoot);

        final List<Predicate> where = new ArrayList<>();

        if (StringUtils.hasText(readerNumber))
            where.add(cb.like(readerJoin.get("readerNumber"), readerNumber));
        if (StringUtils.hasText(isbn))
            where.add(cb.like(bookJoin.get("isbn").get("isbn"), isbn));
        if (returned != null) {
            if (returned) {
                where.add(cb.isNotNull(lendingRoot.get("returnedDate")));
            } else {
                where.add(cb.isNull(lendingRoot.get("returnedDate")));
            }
        }
        if (startDate != null)
            where.add(cb.greaterThanOrEqualTo(lendingRoot.get("startDate"), startDate));
        if (endDate != null)
            where.add(cb.lessThanOrEqualTo(lendingRoot.get("startDate"), endDate));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(lendingRoot.get("lendingNumber")));

        final TypedQuery<Lending> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        return q.getResultList();
    }
}