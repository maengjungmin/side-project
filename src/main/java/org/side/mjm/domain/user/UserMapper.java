package org.side.mjm.domain.user;

import org.side.mjm.common.converter.Converter;
import org.side.mjm.common.converter.enumeration.DateType;
import org.side.mjm.domain.user.record.*;
import org.side.mjm.entity.m_user;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", imports = {Converter.class, DateType.class})
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mappings({
            @Mapping(target = "passwordUpdateDate", expression = "java(Converter.localDateToString(entity.getPasswordUpdateDate(), DateType.YYYYMMDD_FORMAT))"),
    })
    UserSearchResponse toSearchResponse(m_user entity);

    m_user toEntity(UserCreateRequest userCreateRequest);

//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
//    @Mappings({
//            @Mapping(target = "authority.authorityId", source = "authorityId")
//    })
//    m_user updateFromRequest(UserModifyRequest userModifyRequest, @MappingTarget m_user entity);
//
//    @Mappings({
//            @Mapping(target = "passwordUpdateDate", expression = "java(Converter.localDateToString(entity.getPasswordUpdateDate(), DateType.YYYYMMDD_FORMAT))")
//    })
//    UserPasswordModifyResponse toPasswordModifyResponse(m_user entity);
//
//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//    @Mappings({
//            @Mapping(target = "passwordUpdateDate", expression = "java(Converter.getCurrentLocalDate())")
//    })
//    m_user updateFromRequest(UserPasswordResetModifyRequest userPasswordResetModifyRequest, @MappingTarget m_user entity);
//
//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
//    m_user updateFromRequest(MyInformationModifyRequest myInformationModifyRequest, @MappingTarget m_user entity);
//
//    @Mappings({
//            @Mapping(target = "authorityId", source = "authority.authorityId")
//    })
//    UserAuthorityCreateResponse toUserAuthorityCreateResponse(m_user entity);
//
//    List<UserAuthorityCreateResponse> toUserAuthorityCreateResponseList(List<m_user> entityList);
//
//    @Mappings({
//            @Mapping(target = "authority.authorityId", source = "authorityId")
//    })
//    m_user toEntity(UserAuthorityCreateRequest userAuthorityCreateRequest);
//
//    default List<m_user> toEntityCreateRequestList(List<UserAuthorityCreateRequest> userAuthorityCreateRequest, String authorityId) {
//        List<m_user> entityList = new ArrayList<>();
//
//        if (userAuthorityCreateRequest == null) {
//            return null;
//        }
//
//        userAuthorityCreateRequest.forEach(userAuthority -> {
//            entityList.add(toEntity(new UserAuthorityCreateRequest(authorityId, userAuthority.userId())));
//        });
//
//        return entityList;
//    }
}
