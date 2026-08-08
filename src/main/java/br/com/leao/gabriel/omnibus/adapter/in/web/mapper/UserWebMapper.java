package br.com.leao.gabriel.omnibus.adapter.in.web.mapper;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.UserResponse;
import br.com.leao.gabriel.omnibus.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserWebMapper {

//    default User toDomain(RegisterUserRequest request,
//                          String passwordHash) {
//
//        return User.registerAsUser(
//                request.name(),
//                request.email(),
//                passwordHash,
//                request.photoUrl(),
//                request.birthDate()
//        );
//    }

    UserResponse toResponse(User user);
}