package pt.psoft.g1.psoftg1.readermanagement.publishers;

import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

/**
 * Interface para publicação de eventos de Reader via AMQP
 */
public interface ReaderEventsPublisher {

  ReaderViewAMQP sendReaderCreated(ReaderDetails readerDetails);

  ReaderViewAMQP sendReaderUpdated(ReaderDetails readerDetails, Long currentVersion);

  ReaderViewAMQP sendReaderDeleted(ReaderDetails readerDetails, Long currentVersion);
}
