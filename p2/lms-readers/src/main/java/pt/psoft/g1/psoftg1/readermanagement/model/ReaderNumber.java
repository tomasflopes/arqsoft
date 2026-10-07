package pt.psoft.g1.psoftg1.readermanagement.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ReaderNumber implements Serializable {

    @Column(name = "READER_NUMBER")
    private String readerNumber;

    public ReaderNumber(int year, int number) {
        this.readerNumber = year + "/" + number;
    }

    public ReaderNumber(int number) {
        this.readerNumber = 2025 + "/" + number;
    }

    protected ReaderNumber() {}

    @Override
    public String toString() {
        return readerNumber;
    }

    public String getReaderNumber() {
        return readerNumber;
    }
}