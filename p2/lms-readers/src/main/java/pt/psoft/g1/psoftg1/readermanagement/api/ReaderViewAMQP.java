package pt.psoft.g1.psoftg1.readermanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Data
@Schema(description = "A Reader for AMQP communication")
@NoArgsConstructor
public class ReaderViewAMQP {

    @NotNull
    private String readerNumber;

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private Long version;

    @Setter
    @Getter
    private Map<String, Object> _links = new HashMap<>();

    public ReaderViewAMQP(String readerNumber, String name, String email) {
        this.readerNumber = readerNumber;
        this.name = name;
        this.email = email;
    }
}