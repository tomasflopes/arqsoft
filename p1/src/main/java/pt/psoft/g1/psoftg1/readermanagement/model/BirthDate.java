package pt.psoft.g1.psoftg1.readermanagement.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;

@Embeddable
@NoArgsConstructor
@PropertySource({"classpath:config/library.properties"})
public class BirthDate {
    private static final String DATE_FORMAT_REXGEX_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    @Value("${minimumReaderAge}")
    private static int MIN_AGE;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    LocalDate birthDate;

    public BirthDate(int year, int month, int day) {
        setBirthDate(year, month, day);
    }

    public static void setMinAge(@Value("${minimumReaderAge}") int minAge) {
        BirthDate.MIN_AGE = minAge;
    }

    public BirthDate(String birthDate) {
        if (!birthDate.matches(DATE_FORMAT_REXGEX_PATTERN)) {
            throw new IllegalArgumentException("Provided birth date is not in a valid format. Use yyyy-MM-dd");
        }

        String[] dateParts = birthDate.split("-");

        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);

        setBirthDate(year, month, day);
    }

    private void setBirthDate(int year, int month, int day) {
        LocalDate minimumAgeDate = LocalDate.now().minusYears(MIN_AGE);
        LocalDate userDate = LocalDate.of(year, month, day);
        if (userDate.isAfter(minimumAgeDate)) {
            throw new AccessDeniedException("User must be, at least, " + MIN_AGE + "years old");
        }

        this.birthDate = userDate;
    }

    @JsonCreator
    public BirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @JsonValue
    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public String toString() {
        return String.format("%d-%d-%d", this.birthDate.getYear(), this.birthDate.getMonthValue(),
                this.birthDate.getDayOfMonth());
    }
}
