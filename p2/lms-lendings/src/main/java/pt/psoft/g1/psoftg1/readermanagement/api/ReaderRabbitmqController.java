package pt.psoft.g1.psoftg1.readermanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ReaderRabbitmqController {

    private final ReaderService readerService;

    @RabbitListener(queues = "#{autoDeleteQueue_Reader_Created.name}")
    public void receiveReaderCreatedMsg(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            ReaderViewAMQP readerViewAMQP = objectMapper.readValue(jsonReceived, ReaderViewAMQP.class);

            System.out.println(" [x] Received Reader Created by AMQP: " + jsonReceived);
            try {
                readerService.create(readerViewAMQP);
                System.out.println(" [x] New reader inserted from AMQP: " + readerViewAMQP.getReaderNumber());
            } catch (Exception e) {
                System.out.println(" [x] Reader already exists. No need to store it. Error: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving reader event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Reader_Updated.name}")
    public void receiveReaderUpdated(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            ReaderViewAMQP readerViewAMQP = objectMapper.readValue(jsonReceived, ReaderViewAMQP.class);

            System.out.println(" [x] Received Reader Updated by AMQP: " + jsonReceived);
            try {
                readerService.update(readerViewAMQP);
                System.out.println(" [x] Reader updated from AMQP: " + readerViewAMQP.getReaderNumber());
            } catch (Exception e) {
                System.out.println(" [x] Reader does not exist or wrong version. Nothing stored. Error: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving reader event from AMQP: '" + ex.getMessage() + "'");
        }
    }
}