package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.application.usecase.GetActiveUsersCase;
import br.com.leao.gabriel.omnibus.domain.model.User;
import br.com.leao.gabriel.omnibus.domain.port.out.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetActiveUsersService implements GetActiveUsersCase {
  private final UserRepository userRepository;

  @Override
  public List<User> execute() {
    return userRepository.findAll();
  }
}
