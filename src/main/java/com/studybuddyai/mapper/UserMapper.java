package com.studybuddyai.mapper;

import com.studybuddyai.dto.UserRegistrationDto;
import com.studybuddyai.dto.UserDto;
import com.studybuddyai.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User userRegistrationDtoToUser(UserRegistrationDto user);

    UserDto userToUserDto(User user);
}