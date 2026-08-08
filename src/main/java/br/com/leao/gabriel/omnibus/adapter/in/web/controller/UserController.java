package br.com.leao.gabriel.omnibus.adapter.in.web.controller;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.UserResponse;
import br.com.leao.gabriel.omnibus.adapter.in.web.mapper.UserWebMapper;
import br.com.leao.gabriel.omnibus.application.usecase.GetActiveUsersCase;
import br.com.leao.gabriel.omnibus.application.usecase.GetUserCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
  private final UserWebMapper userWebMapper;
  private final GetActiveUsersCase getActiveUsersCase;
  private final GetUserCase getUserCase;

  @GetMapping()
  public ResponseEntity<List<UserResponse>> getUsers() {
    var user = getActiveUsersCase.execute();
    var responseUsers = user.stream().map(userWebMapper::toResponse).toList();
    return ResponseEntity.ok(responseUsers);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
    var user = getUserCase.execute(id);
    return ResponseEntity.ok(userWebMapper.toResponse(user));
  }
}
