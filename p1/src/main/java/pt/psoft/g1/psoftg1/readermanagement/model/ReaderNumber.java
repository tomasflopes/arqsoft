package pt.psoft.g1.psoftg1.readermanagement.model;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonCreator;


@Embeddable
public class ReaderNumber implements Serializable {
  @Column(name = "READER_NUMBER")
  private String readerNumber;

  public ReaderNumber(int year, int number) {
    this.readerNumber = year + "/" + number;
  }

  public ReaderNumber(int number) {
    this.readerNumber = LocalDate.now().getYear() + "/" + number;
  }

  protected ReaderNumber() {
  }

  @JsonCreator
  public ReaderNumber(String value) {
      this.readerNumber = value;
  }

  @JsonValue
  public String getValue() {
      return readerNumber;
  }

  @Override
  public String toString() {
    return this.readerNumber;
  }
}
