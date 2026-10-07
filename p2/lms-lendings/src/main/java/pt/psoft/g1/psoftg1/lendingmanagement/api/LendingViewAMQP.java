package pt.psoft.g1.psoftg1.lendingmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Schema(description = "A Lending form for AMQP communication")
@NoArgsConstructor
@AllArgsConstructor
public class LendingViewAMQP {

    @NotNull
    private String lendingNumber; 

    @NotNull
    private String readerNumber;

    @NotNull
    private String bookId;

    @NotNull
    private String startDate;

    @NotNull
    private String limitDate;

    private String returnedDate;

    private String commentary;
}