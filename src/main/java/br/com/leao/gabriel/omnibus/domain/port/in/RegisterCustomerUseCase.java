package br.com.leao.gabriel.omnibus.domain.port.in;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import java.time.LocalDate;

public interface RegisterCustomerUseCase {

  Customer execute(String name, String email, String rawPassword, LocalDate birthDate,
      String photoUrl);
}