package pt.psoft.g1.psoftg1.bookmanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.io.Serializable;

@Embeddable
public class Isbn implements Serializable {
    @Column(name = "ISBN")
    @NotNull
    @NotBlank
    @Getter
    private String value;

    public Isbn(String value) {
        this.value = value;
    }

    protected Isbn() {}

    @Override
    public String toString() {
        return value;
    }
}