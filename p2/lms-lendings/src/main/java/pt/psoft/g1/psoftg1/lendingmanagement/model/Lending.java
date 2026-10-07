package pt.psoft.g1.psoftg1.lendingmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.StaleObjectStateException;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.readermanagement.model.Reader;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames={"LENDING_NUMBER"})})
public class Lending {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Getter
    private LendingNumber lendingNumber;

    @NotNull
    @Getter
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Book book;

    @NotNull
    @Getter
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Reader reader; // Substituído ReaderDetails por Reader

    @NotNull
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    @Getter
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @Getter
    private LocalDate limitDate;

    @Temporal(TemporalType.DATE)
    @Getter
    private LocalDate returnedDate;

    @Version
    @Getter
    private long version;

    @Size(min = 0, max = 1024)
    @Column(length = 1024)
    private String commentary = null;

    @Transient
    private Integer daysUntilReturn;

    @Transient
    private Integer daysOverdue;

    @Getter
    private int fineValuePerDayInCents;

    public Lending(Book book, Reader reader, int seq, int lendingDuration, int fineValuePerDayInCents) {
        try {
            this.book = Objects.requireNonNull(book);
            this.reader = Objects.requireNonNull(reader);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null objects passed to lending");
        }
        this.lendingNumber = new LendingNumber(seq);
        this.startDate = LocalDate.now();
        this.limitDate = LocalDate.now().plusDays(lendingDuration);
        this.returnedDate = null;
        this.fineValuePerDayInCents = fineValuePerDayInCents;
        setDaysUntilReturn();
        setDaysOverdue();
    }

    public static Lending newBootstrappingLending(Book book,
                                                  Reader reader,
                                                  int year,
                                                  int seq,
                                                  LocalDate startDate,
                                                  LocalDate returnedDate,
                                                  int lendingDuration,
                                                  int fineValuePerDayInCents) {
        Lending lending = new Lending();

        try {
            lending.book = Objects.requireNonNull(book);
            lending.reader = Objects.requireNonNull(reader);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null objects passed to lending");
        }
        lending.lendingNumber = new LendingNumber(year, seq);
        lending.startDate = startDate;
        lending.limitDate = startDate.plusDays(lendingDuration);
        lending.fineValuePerDayInCents = fineValuePerDayInCents;
        lending.returnedDate = returnedDate;
        return lending;
    }

    private void setDaysUntilReturn() {
        int daysUntilReturn = (int) ChronoUnit.DAYS.between(LocalDate.now(), this.limitDate);
        if (this.returnedDate != null || daysUntilReturn < 0) {
            this.daysUntilReturn = null;
        } else {
            this.daysUntilReturn = daysUntilReturn;
        }
    }
    
    private void setDaysOverdue() {
        int days = getDaysDelayed();
        if (days > 0) {
            this.daysOverdue = days;
        } else {
            this.daysOverdue = null;
        }
    }
    protected Lending() {
        // Construtor vazio para uso pelo ORM
    }
    public int getDaysDelayed() {
        if (this.returnedDate != null) {
            return Math.max((int) ChronoUnit.DAYS.between(this.limitDate, this.returnedDate), 0);
        } else {
            return Math.max((int) ChronoUnit.DAYS.between(this.limitDate, LocalDate.now()), 0);
        }
    }
    public Optional<Integer> getFineValueInCents() {
        Optional<Integer> fineValueInCents = Optional.empty();
        int days = getDaysDelayed();
        if (days > 0) {
            fineValueInCents = Optional.of(this.fineValuePerDayInCents * days);
        }
        return fineValueInCents;
    }
    public void setReturned(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }
    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }
}