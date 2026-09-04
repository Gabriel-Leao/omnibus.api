package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import br.com.leao.gabriel.omnibus.adapter.in.web.validation.minimumage.MinimumAge;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordConfirmable;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.hibernate.validator.constraints.URL;

/**
 * Represents and validates the data required to register a customer.
 *
 * @param name the name
 *
 * @param email the email
 *
 * @param password the password
 *
 * @param confirmPassword the confirmPassword
 *
 * @param birthDate the birthDate
 *
 * @param photoUrl the photoUrl
 */
@PasswordMatches
@Schema(description = "Data required to register a customer")
public record RegisterCustomerRequest(
    @Schema(description = "Full name", example = "Maria Silva", minLength = 3, maxLength = 150)
        @NotBlank(message = "Name can't be empty")
        @Size(min = 3, max = 150, message = "name must have between 3 and 150 characters")
        String name,
    @Schema(description = "Email address", example = "maria.silva@exemplo.com", maxLength = 200)
        @NotBlank(message = "Email can't be empty")
        @Email(message = "Invalid e-mail")
        @Size(max = 200, message = "Email should have at most 200 characters")
        String email,
    @Schema(
            description = "Password",
            example = "Password@123",
            format = "password",
            minLength = 8,
            maxLength = 72)
        @NotBlank(message = "Password can't be empty")
        @Size(
            max = 72,
            min = 8,
            message = "Password must have a length between 8 and 72 characters")
        String password,
    @Schema(description = "Password confirmation", example = "Password@123", format = "password")
        @NotBlank(message = "confirm password can't be empty")
        String confirmPassword,
    @Schema(description = "Date of birth", example = "1990-05-20", format = "date")
        @PastOrPresent(message = "birth date should be a date in the past")
        @MinimumAge(value = 18, message = "minimum age to register is 18")
        LocalDate birthDate,
    @Schema(
            description = "Public URL of the customer's photo",
            example = "https://exemplo.com/foto.jpg")
        @URL(message = "Photo url must be a valid URL")
        String photoUrl)
    implements PasswordConfirmable {}
