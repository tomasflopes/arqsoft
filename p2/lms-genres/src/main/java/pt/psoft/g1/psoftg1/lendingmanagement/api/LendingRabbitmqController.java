package pt.psoft.g1.psoftg1.lendingmanagement.api;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.lendingmanagement.services.CreateLendingRequest;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;

@Service
@RequiredArgsConstructor
public class LendingRabbitmqController {

  @Autowired
  private final LendingService lendingService;

  @RabbitListener(queues = "#{autoDeleteQueue_Lending_Created.name}")
  public void receiveLendingCreatedMsg(Message msg) {
    handleLendingCreated(msg);
  }

  public void handleLendingCreated(Message msg) {
    try {
      String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);

      ObjectMapper objectMapper = new ObjectMapper();
      LendingViewAMQP lendingViewAMQP = objectMapper.readValue(jsonReceived, LendingViewAMQP.class);

      System.out.println(" [x] Received Lending Created by AMQP: " + msg + ".");
      try {
        CreateLendingRequest request = new CreateLendingRequest(
            lendingViewAMQP.getBookId(),
            lendingViewAMQP.getReaderNumber());
        lendingService.create(request);
        System.out.println(" [x] New Lending inserted from AMQP: " + msg + ".");
      } catch (Exception e) {
        System.out.println(" [x] Lending already exists. No need to store it.");
      }
    } catch (Exception ex) {
      System.out.println(" [x] Exception receiving Lending event from AMQP: '" + ex.getMessage() + "'");
    }
  }

  @RabbitListener(queues = "#{autoDeleteQueue_Lending_Updated.name}")
  public void receiveLendingUpdated(Message msg) {
    handleLendingUpdated(msg);
  }

  public void handleLendingUpdated(Message msg) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();

      String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
      LendingViewAMQP lendingViewAMQP = objectMapper.readValue(jsonReceived, LendingViewAMQP.class);

      System.out.println(" [x] Received Lending Updated by AMQP: " + msg + ".");
      try {
        lendingService.update(lendingViewAMQP);
        System.out.println(" [x] Lending updated from AMQP: " + msg + ".");
      } catch (Exception e) {
        System.out.println(" [x] Lending does not exists or wrong version. Nothing stored.");
      }
    } catch (Exception ex) {
      System.out.println(" [x] Exception receiving lending event from AMQP: '" + ex.getMessage() + "'");
    }
  }
}
