package com.studybuddyai.mapper;

import com.studybuddyai.dto.UserRegistrationDto;
import com.studybuddyai.dto.UserDto;
import com.studybuddyai.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    User userRegistrationDtoToUser(UserRegistrationDto user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    UserDto userToUserDto(User user);
}