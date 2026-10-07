package pt.psoft.g1.psoftg1.lendingmanagement.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SearchLendingQuery;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LendingControllerTest {
  @Mock
  private LendingService lendingService;
  @Mock
  private ReaderService readerService;
  @Mock
  private UserService userService;
  @Mock
  private ConcurrencyService concurrencyService;
  @Mock
  private LendingViewMapper lendingViewMapper;

  @InjectMocks
  private LendingController controller;

  @Test
  public void shouldReturnAverageLendingDuration() {
    Double avg = 7.5;
    LendingsAverageDurationView view = new LendingsAverageDurationView();

    when(lendingService.getAverageDuration()).thenReturn(avg);
    when(lendingViewMapper.toLendingsAverageDurationView(avg)).thenReturn(view);

    ResponseEntity<LendingsAverageDurationView> response = controller.getAvgDuration();

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(view, response.getBody());
  }

  @Test
  public void shouldReturnOverdueLendingsSuccessfully() {
    Page page = new Page(1, 5);
    Lending lending = mock(Lending.class);
    LendingView view = mock(LendingView.class);

    when(lendingService.getOverdue(page)).thenReturn(List.of(lending));
    when(lendingViewMapper.toLendingView(List.of(lending))).thenReturn(List.of(view));

    ListResponse<LendingView> response = controller.getOverdueLendings(page);

    assertNotNull(response);
    assertFalse(response.getItems().isEmpty());
    assertEquals(view, response.getItems().get(0));
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowWhenNoOverdueLendingsFound() {
    Page page = new Page(1, 5);
    when(lendingService.getOverdue(page)).thenReturn(Collections.emptyList());
    controller.getOverdueLendings(page);
  }

  @Test
  public void shouldSearchLendingsSuccessfully() {
    SearchRequest<SearchLendingQuery> request = new SearchRequest<>();
    request.setPage(new Page(1, 10));
    request.setQuery(new SearchLendingQuery());

    Lending lending = mock(Lending.class);
    LendingView view = mock(LendingView.class);

    when(lendingService.searchLendings(request.getPage(), request.getQuery()))
      .thenReturn(List.of(lending));
    when(lendingViewMapper.toLendingView(List.of(lending)))
      .thenReturn(List.of(view));

    ListResponse<LendingView> result = controller.searchReaders(request);

    assertNotNull(result);
    assertEquals(1, result.getItems().size());
    assertEquals(view, result.getItems().get(0));
  }
}
