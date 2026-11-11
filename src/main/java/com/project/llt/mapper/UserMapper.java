package com.project.llt.mapper;

import com.project.llt.user.User;
import com.project.llt.user.UserDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static UserDto convertToDto(ModelMapper modelMapper, User user) {
        return modelMapper.map(user, UserDto.class);
    }

    public static User convertToEntity(ModelMapper modelMapper, UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}
