package pt.psoft.g1.psoftg1.readermanagement.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ReaderDetailsTest {
  @Mock
  private Reader mockReader;

  @BeforeAll
  static void setupAll() {
    BirthDate.setMinAge(0);
  }

  @Test
  void ensureValidReaderDetailsAreCreated() {
    assertDoesNotThrow(() -> new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false, null, null));
  }

  @Test
  void ensureExceptionIsThrownForNullReader() {
    assertThrows(IllegalArgumentException.class, () -> new ReaderDetails(123, null, "2010-01-01", "912345678", true, false, false, null, null));
  }

  @Test
  void ensureExceptionIsThrownForNullPhoneNumber() {
    assertThrows(IllegalArgumentException.class, () -> new ReaderDetails(123, mockReader, "2010-01-01", null, true, false, false, null, null));
  }

  @Test
  void ensureExceptionIsThrownForNoGdprConsent() {
    assertThrows(IllegalArgumentException.class, () -> new ReaderDetails(123, mockReader, "2010-01-01", "912345678", false, false, false, null, null));
  }

  @Test
  void ensureGdprConsentIsTrue() {
    ReaderDetails readerDetails = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false, null, null);
    assertTrue(readerDetails.isGdprConsent());
  }

  @Test
  void ensurePhotoCanBeNull_AkaOptional() {
    ReaderDetails readerDetails = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false, null, null);
    assertNull(readerDetails.getPhoto());
  }

  @Test
  void ensureValidPhoto() {
    ReaderDetails readerDetails = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false, "readerPhotoTest.jpg", null);
    Photo photo = readerDetails.getPhoto();

    // This is here to force the test to fail if the photo is null
    assertNotNull(photo);

    String readerPhoto = photo.getPhotoFile();
    assertEquals("readerPhotoTest.jpg", readerPhoto);
  }

  @Test
  void ensureInterestListCanBeNullOrEmptyList_AkaOptional() {
    ReaderDetails readerDetailsNullInterestList = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false, "readerPhotoTest.jpg", null);
    assertNull(readerDetailsNullInterestList.getInterestList());

    ReaderDetails readerDetailsInterestListEmpty = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false, "readerPhotoTest.jpg", new ArrayList<>());
    assertEquals(0, readerDetailsInterestListEmpty.getInterestList().size());
  }

  @Test
  void ensureInterestListCanTakeAnyValidGenre() {
    Genre g1 = new Genre("genre1");
    Genre g2 = new Genre("genre2");
    List<Genre> genreList = new ArrayList<>();
    genreList.add(g1);
    genreList.add(g2);

    ReaderDetails readerDetails = new ReaderDetails(123, mockReader, "2010-01-01", "912345678", true, false, false, "readerPhotoTest.jpg", genreList);
    assertEquals(2, readerDetails.getInterestList().size());
  }
}
