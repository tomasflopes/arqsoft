package pt.psoft.g1.psoftg1.readermanagement.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "A Reader from AMQP communication")
public class ReaderViewAMQP {
    @NotNull
    private String readerNumber;

    @NotNull
    private Long id;

    private String name;

    private String email;
}