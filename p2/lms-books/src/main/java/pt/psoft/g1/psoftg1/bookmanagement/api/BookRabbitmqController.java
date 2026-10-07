package pt.psoft.g1.psoftg1.bookmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQP;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;
import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class BookRabbitmqController {

    @Autowired
    private final BookService bookService;
    
    @Autowired
    private final GenreService genreService;

    @RabbitListener(queues = "#{selfQueue_Book_Created.name}")
    public void receiveBookCreatedMsg(Message msg) {

        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

            System.out.println(" [x] Received Book Created by AMQP: " + msg + ".");
            try {
                bookService.create(bookViewAMQP);
                System.out.println(" [x] New book inserted from AMQP: " + msg + ".");
            } catch (Exception e) {
                System.out.println(" [x] Book already exists. No need to store it.");
            }
        }
        catch(Exception ex) {
            System.out.println(" [x] Exception receiving book event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{selfQueue_Book_Updated.name}")
    public void receiveBookUpdated(Message msg) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

            System.out.println(" [x] Received Book Updated by AMQP: " + msg + ".");
            try {
                bookService.update(bookViewAMQP);
                System.out.println(" [x] Book updated from AMQP: " + msg + ".");
            } catch (Exception e) {
                System.out.println(" [x] Book does not exists or wrong version. Nothing stored.");
            }
        }
        catch(Exception ex) {
            System.out.println(" [x] Exception receiving book event from AMQP: '" + ex.getMessage() + "'");
        }
    }
    
    @RabbitListener(queues = "#{selfQueue_Genre_Created.name}")
    public void receiveGenreCreated(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            GenreViewAMQP genreViewAMQP = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);
            
            System.out.println(" [x] Received Genre Created by AMQP: " + genreViewAMQP.getGenre());
            try {
                genreService.save(new Genre(genreViewAMQP.getGenre()));
                System.out.println(" [x] New genre inserted from AMQP: " + genreViewAMQP.getGenre());
            } catch (Exception e) {
                System.out.println(" [x] Genre already exists. No need to store it.");
            }
        } catch(Exception ex) {
            System.out.println(" [x] Exception receiving genre event from AMQP: '" + ex.getMessage() + "'");
        }
    }
    
    @RabbitListener(queues = "#{selfQueue_Genre_Updated.name}")
    public void receiveGenreUpdated(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            GenreViewAMQP genreViewAMQP = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);
            
            System.out.println(" [x] Received Genre Updated by AMQP: oldGenre=" + genreViewAMQP.getOldGenre() + " -> newGenre=" + genreViewAMQP.getGenre());
            
            // Agora o evento de UPDATE inclui {oldGenre, genre}
            // Podemos buscar o genre antigo e atualizá-lo
            if (genreViewAMQP.getOldGenre() != null) {
                genreService.findByString(genreViewAMQP.getOldGenre()).ifPresent(oldGenre -> {
                    try {
                        // Atualiza o nome do genre
                        // Os Books mantêm a referência porque o PK do Genre não muda
                        oldGenre.updateGenre(genreViewAMQP.getGenre());
                        genreService.save(oldGenre);
                        System.out.println(" [x] Genre updated from AMQP: " + genreViewAMQP.getOldGenre() + " -> " + genreViewAMQP.getGenre());
                    } catch (Exception e) {
                        System.out.println(" [x] Error updating genre: " + e.getMessage());
                    }
                });
            }
            
        } catch(Exception ex) {
            System.out.println(" [x] Exception receiving genre event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{selfQueue_Genre_Deleted.name}")
    public void receiveGenreDeleted(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            GenreViewAMQP genreViewAMQP = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);

            System.out.println(" [x] Received Genre Deleted by AMQP: " + genreViewAMQP.getGenre());
            genreService.findByString(genreViewAMQP.getGenre()).ifPresent(genreService::delete);
        } catch(Exception ex) {
            System.out.println(" [x] Exception receiving genre deleted event from AMQP: '" + ex.getMessage() + "'");
        }
    }
}