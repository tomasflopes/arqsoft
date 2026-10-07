package pt.psoft.g1.psoftg1.readermanagement.services;

import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReaderWithUserRequest {
    
    // User fields
    @NotBlank
    @Email
    @NonNull
    private String username;

    @NotBlank
    @NonNull
    private String password;

    @NotBlank
    @NonNull
    private String fullName;

    // Reader-specific fields
    @NonNull
    @NotBlank
    private String birthDate;

    @NonNull
    @NotBlank
    private String phoneNumber;

    @Nullable
    @Getter
    @Setter
    private byte[] photo;

    @Setter
    private boolean gdpr;

    @Nullable
    private List<String> interestList;
}
