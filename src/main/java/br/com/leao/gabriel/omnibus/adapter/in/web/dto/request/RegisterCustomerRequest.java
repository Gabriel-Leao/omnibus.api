package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import br.com.leao.gabriel.omnibus.adapter.in.web.validation.minimumage.MinimumAge;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordConfirmable;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordMatches;
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
public record RegisterCustomerRequest(
    @NotBlank(message = "Name can't be empty")
        @Size(min = 3, max = 150, message = "name must have between 3 and 150 characters")
        String name,
    @NotBlank(message = "Email can't be empty")
        @Email(message = "Invalid e-mail")
        @Size(max = 200, message = "Email should have at most 200 characters")
        String email,
    @NotBlank(message = "Password can't be empty")
        @Size(
            max = 72,
            min = 8,
            message = "Password must have a length between 8 and 72 characters")
        String password,
    @NotBlank(message = "confirm password can't be empty") String confirmPassword,
    @PastOrPresent(message = "birth date should be a date in the past")
        @MinimumAge(value = 18, message = "minimum age to register is 18")
        LocalDate birthDate,
    @URL(message = "Photo url must be a valid URL") String photoUrl)
    implements PasswordConfirmable {}
