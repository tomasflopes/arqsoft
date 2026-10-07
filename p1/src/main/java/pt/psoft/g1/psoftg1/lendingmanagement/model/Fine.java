package pt.psoft.g1.psoftg1.lendingmanagement.model;

import java.util.Objects;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

/**
 * The {@code Fine} class models a fine applied when a lending is past its due
 * date.
 * <p>
 * It stores its current value, and the associated {@code Lending}.
 * 
 * @author rmfranca
 */
@Getter
@Entity
@GenericGenerator(name = "id-generator", strategy = "pt.psoft.g1.psoftg1.configuration.IdGenerationConfig")
public class Fine {
  @Id
  @GeneratedValue(generator = "id-generator")
  private String pk;

  @PositiveOrZero
  @Column(updatable = false)
  private int fineValuePerDayInCents;

  /** Fine value in Euro cents */
  @PositiveOrZero
  int centsValue;

  @Setter
  @OneToOne(optional = false, orphanRemoval = true)
  @JoinColumn(name = "lending_pk", nullable = false, unique = true)
  private Lending lending;

  /**
   * Constructs a new {@code Fine} object. Sets the current value of the fine,
   * as well as the fine value per day at the time of creation.
   * 
   * @param lending transaction which generates this fine.
   */
  public Fine(Lending lending) {
    if (lending.getDaysDelayed() <= 0)
      throw new IllegalArgumentException("Lending is not overdue");
    fineValuePerDayInCents = lending.getFineValuePerDayInCents();
    centsValue = fineValuePerDayInCents * lending.getDaysDelayed();
    this.lending = Objects.requireNonNull(lending);
  }

  /** Protected empty constructor for ORM only. */
  protected Fine() {
  }
}
