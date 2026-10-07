package pt.psoft.g1.psoftg1.readermanagement.infraestructure.publishers.impl;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQPMapper;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.publishers.ReaderEventsPublisher;
import pt.psoft.g1.psoftg1.shared.model.ReaderEvents;

@Service
@RequiredArgsConstructor
public class ReaderEventsRabbitmqPublisherImpl implements ReaderEventsPublisher {
  @Autowired
  private RabbitTemplate template;

  @Autowired
  private DirectExchange direct;

  @Autowired
  private final ReaderViewAMQPMapper readerViewAMQPMapper;

  @Override
  public ReaderViewAMQP sendReaderCreated(ReaderDetails readerDetails) {
    return sendReaderEvent(readerDetails, 1L, ReaderEvents.READER_CREATED);
  }

  @Override
  public ReaderViewAMQP sendReaderUpdated(ReaderDetails readerDetails, Long currentVersion) {
    return sendReaderEvent(readerDetails, currentVersion, ReaderEvents.READER_UPDATED);
  }

  @Override
  public ReaderViewAMQP sendReaderDeleted(ReaderDetails readerDetails, Long currentVersion) {
    return sendReaderEvent(readerDetails, currentVersion, ReaderEvents.READER_DELETED);
  }

  private ReaderViewAMQP sendReaderEvent(ReaderDetails readerDetails, Long currentVersion, String readerEventType) {

    System.out.println("Send Reader event to AMQP Broker: " + readerDetails.getReader().getUsername());

    try {
      ReaderViewAMQP readerViewAMQP = readerViewAMQPMapper.toReaderViewAMQP(readerDetails);
      readerViewAMQP.setVersion(currentVersion);

      ObjectMapper objectMapper = new ObjectMapper();
      String readerViewAMQPinString = objectMapper.writeValueAsString(readerViewAMQP);

      this.template.convertAndSend(direct.getName(), readerEventType, readerViewAMQPinString);

      return readerViewAMQP;
    } catch (Exception ex) {
      System.out.println(" [x] Exception sending reader event: '" + ex.getMessage() + "'");

      return null;
    }
  }
}
