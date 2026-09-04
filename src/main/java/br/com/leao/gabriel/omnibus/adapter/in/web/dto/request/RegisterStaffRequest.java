package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import br.com.leao.gabriel.omnibus.adapter.in.web.validation.enumvalue.EnumValue;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordConfirmable;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordMatches;
import br.com.leao.gabriel.omnibus.domain.model.StaffDepartment;
import br.com.leao.gabriel.omnibus.domain.model.StaffRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Represents and validates the data required to register a staff member.
 *
 * @param name the name
 *
 * @param email the email
 *
 * @param password the password
 *
 * @param confirmPassword the confirmPassword
 *
 * @param role the role
 *
 * @param employeeCode the employeeCode
 *
 * @param department the department
 *
 * @param hiredAt the hiredAt
 */
@PasswordMatches
@Schema(description = "Data required to register a staff member")
public record RegisterStaffRequest(
    @Schema(description = "Full name", example = "João Silva")
        @NotBlank(message = "Name can't be empty")
        @Size(min = 3, max = 150, message = "name must have between 3 and 150 characters")
        String name,
    @Schema(description = "Email address", example = "joao.silva@exemplo.com")
        @NotBlank(message = "Email can't be empty")
        @Email(message = "Invalid e-mail")
        @Size(max = 200, message = "Email should have at most 200 characters")
        String email,
    @Schema(description = "Password", example = "Password@123", format = "password", minLength = 8)
        @NotBlank(message = "Password can't be empty")
        @Size(
            max = 72,
            min = 8,
            message = "Password must have a length between 8 and 72 characters")
        String password,
    @Schema(description = "Password confirmation", example = "Password@123", format = "password")
        @NotBlank(message = "confirm password can't be empty")
        String confirmPassword,
    @Schema(description = "Staff role", example = "VIEWER")
        @NotBlank(message = "Role can't be empty")
        @EnumValue(enumClass = StaffRole.class, message = "Invalid role value")
        String role,
    @NotBlank(message = "Employee code can't be empty") String employeeCode,
    @Schema(description = "Staff department")
        @NotBlank(message = "Department can't be empty")
        @EnumValue(enumClass = StaffDepartment.class, message = "Invalid department")
        String department,
    @PastOrPresent(message = "hired at should be a past or present date") LocalDate hiredAt)
    implements PasswordConfirmable {}
