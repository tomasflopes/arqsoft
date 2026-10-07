package pt.psoft.g1.psoftg1.usermanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para comunicação AMQP de Users
 * Contém apenas os dados essenciais necessários no microserviço de Readers
 */
@Data
@Schema(description = "A User for AMQP communication")
@NoArgsConstructor
public class UserViewAMQP {

    @NotNull
    private Long id;

    @NotNull
    private String username;

    @NotNull
    private String fullName;

    @NotNull
    private Long version;

    public UserViewAMQP(Long id, String username, String fullName, Long version) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.version = version;
    }
}
