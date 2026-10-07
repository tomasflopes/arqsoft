package pt.psoft.g1.psoftg1.usermanagement.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.usermanagement.model.User;

@Mapper(componentModel = "spring")
public abstract class UserViewMapper {

    @Mapping(target = "fullName", source = "name")
    public abstract UserView toUserView(User user);

    public abstract List<UserView> toUserView(List<User> users);
}