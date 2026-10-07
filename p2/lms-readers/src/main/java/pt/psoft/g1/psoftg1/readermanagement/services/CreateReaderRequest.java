package pt.psoft.g1.psoftg1.readermanagement.services;

import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

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
public class CreateReaderRequest {
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

    @NonNull
    @NotBlank
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String birthDate;

    @NonNull
    @NotBlank
    private String phoneNumber;

    @Nullable
    @Getter
    @Setter
    private MultipartFile photo;

    @Setter
    private boolean gdpr;

    @Setter
    private boolean marketing;

    @Setter
    private boolean thirdParty;

    @Nullable
    @Getter
    @Setter
    private List<String> interestList;

    public boolean getThirdParty() {
        return thirdParty;
    }

    public boolean getMarketing() {
        return marketing;
    }

    public boolean getGdpr() {
        return gdpr;
    }
}
