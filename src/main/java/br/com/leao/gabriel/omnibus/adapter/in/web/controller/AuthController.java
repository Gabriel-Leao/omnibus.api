package br.com.leao.gabriel.omnibus.adapter.in.web.controller;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.RegisterCustomerRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.CustomerResponse;
import br.com.leao.gabriel.omnibus.adapter.in.web.mapper.CustomerWebMapper;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.port.in.RegisterCustomerUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

  private final RegisterCustomerUseCase registerCustomerUseCase;
  private final CustomerWebMapper customerWebMapper;

  @PostMapping("/register")
  public ResponseEntity<CustomerResponse> createCustomer(
      @Valid
      @RequestBody RegisterCustomerRequest requestData) {
    Customer customer =
        registerCustomerUseCase.execute(
            requestData.name(),
            requestData.email(),
            requestData.password(),
            requestData.birthDate(),
            requestData.photoUrl());
    CustomerResponse response = customerWebMapper.toResponse(customer);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
