package org.side.mjm.domain.user;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.side.mjm.common.converter.Converter;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.custom.ServiceException;
import org.side.mjm.common.response.structure.ItemResponse;
import org.side.mjm.config.jwt.TokenProvider;
import org.side.mjm.config.jwt.record.TokenResponse;
import org.side.mjm.config.message.MessageConfig;
import org.side.mjm.config.rsa.RsaProvider;
import org.side.mjm.domain.user.record.*;
import org.side.mjm.entity.m_user;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final MessageConfig messageConfig;

    private final UserRepository userRepository;
    private final UserMapper userMapper = UserMapper.INSTANCE;

    private final PasswordEncoder encoder;
    private final RsaProvider rsaProvider;
    private final TokenProvider tokenProvider;

    @Transactional
    public ItemResponse<String> checkNewId(UserCheckRequest parameter) {
        boolean result = userRepository.existsById(parameter.userId());
        return ItemResponse.<String>builder()
                .status(messageConfig.getCode("SUCCESS.CODE"))
                .message(messageConfig.getMsg(result? "SEARCH.SUCCESS.MSG" : "SEARCH.FAIL.MSG"))
                .item(result ? "N" : "Y")
                .build();
    }
    
    @Transactional
    public ItemResponse<Long> createUser(UserCreateRequest parameter) {
        m_user m_user = userMapper.toEntity(parameter);
        m_user.setPasswordUpdateDate(LocalDate.now(ZoneId.of("Asia/Seoul")));
        userRepository.save(m_user);
        return ItemResponse.<Long>builder()
                .status(messageConfig.getCode("SUCCESS.CODE"))
                .message(messageConfig.getMsg("INSERT.SUCCESS.MSG"))
                .item(1L)
                .build();
    }


    @Transactional
    public ItemResponse<Long> deleteUser(UserCheckRequest parameter) {
        m_user entity = userRepository.findById(parameter.userId())
                .orElseThrow(() -> new EntityNotFoundException(parameter.userId()));
        userRepository.delete(entity);

        return ItemResponse.<Long>builder()
                .status(messageConfig.getCode("SUCCESS.CODE"))
                .message(messageConfig.getMsg("DELETE.SUCCESS.MSG"))
                .item(1L)
                .build();
    }

    @Transactional
    public ItemResponse<UserSearchResponse> getMyInformation(HttpServletRequest request) {

        String accessToken = tokenProvider.getTokenFromCookie(request);
        String userId = "";
        if (StringUtils.hasText(accessToken)) {
            userId = tokenProvider.getUid(accessToken);
        }
        String id = userId;
        m_user user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(id));

        return ItemResponse.<UserSearchResponse>builder()
                .status(messageConfig.getCode("SUCCESS.CODE"))
                .message(messageConfig.getMsg("SEARCH.SUCCESS.MSG"))
                .item(userMapper.toSearchResponse(user))
                .build();
    }

//
//    @Transactional
//    public ItemResponse<UserPasswordModifyResponse> modifyUserPassword(UserPasswordModifyRequest parameter) {
//
//            m_user entity = userRepository.findById(parameter.userId())
//                    .orElseThrow(() -> new EntityNotFoundException(parameter.userId()));
//
//            //front에서 들어온 비밀번호 decode
//            String decodePassword = "";
//            String decodeNewPassword1 = "";
//            String decodeNewPassword2 = "";
//            try {
//                decodePassword = rsaProvider.decrypt(parameter.password());
//                decodeNewPassword1 = rsaProvider.decrypt(parameter.newPassword1());
//                decodeNewPassword2 = rsaProvider.decrypt(parameter.newPassword2());
//            } catch (IllegalArgumentException e) {
//                LOGGER.error("Password decode fail with RSA, request password : {}", parameter.password());
//                throw new ServiceException(CommonErrorCode.NOT_AUTHENTICATION, e);
//            }
//
//            //비밀번호가 맞지 않으면 throw Error, 아니면 계속 진행
//            if (!encoder.matches(decodePassword.trim(), entity.getPassword())) {
//                LOGGER.error("Incorrect Password : " + parameter.userId());
//                throw new ServiceException(CommonErrorCode.WRONG_PASSWORD, "비밀번호가 맞지 않습니다");
//            }
//
//            //새 비밀번호 1, 2가 맞지 않으면 error
//            if (!decodeNewPassword1.equals(decodeNewPassword2)) {
//                LOGGER.error("wrong new password. check the new password 1 and 2");
//                throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, "새 비밀번호가 맞지 않습니다.");
//            }
//
//            if (decodePassword.equals(decodeNewPassword1)) {
//                LOGGER.error("same password as before");
//                throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, "이전과 같은 비밀번호입니다.");
//            }
//
//            if (!PasswordUtils.validPassword(PasswordLevel.LEVEL2, decodeNewPassword1)) {
//                LOGGER.error("same password as before");
//                throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, "8자리 이상, 하나 이상의 문자, 숫자, 특수문자를 사용해야합니다.");
//            }
//
//            //비밀번호 정보 encode 및 저장
//            String encodedNewPswd = encoder.encode(decodeNewPassword1.trim());
//            entity.setPassword(encodedNewPswd);
//            entity.setPasswordUpdateDate(Converter.getCurrentLocalDate());
//            entity.setLoginErrorCount(0);
//            userRepository.saveAndFlush(entity);
//
//            return ItemResponse.<UserPasswordModifyResponse>builder()
//                    .status(messageConfig.getCode("SUCCESS.CODE"))
//                    .message(messageConfig.getMsg("UPDATE.SUCCESS.MSG"))
//                    .item(userMapper.toPasswordModifyResponse(entity))
//                    .build();
//    }
//
//    @Transactional
//    public ItemResponse<UserPasswordModifyResponse> modifyUserPasswordReset(UserPasswordResetModifyRequest parameter) {
//
//        m_user entity = userRepository.findById(parameter.userId())
//                .orElseThrow(() -> new EntityNotFoundException(parameter.userId()));
//        m_user updatedEntity = userMapper.updateFromRequest(parameter, entity);
//        userRepository.saveAndFlush(updatedEntity);
//
//        return ItemResponse.<UserPasswordModifyResponse>builder()
//                .status(messageConfig.getCode("SUCCESS.CODE"))
//                .message(messageConfig.getMsg("UPDATE.SUCCESS.MSG"))
//                .item(userMapper.toPasswordModifyResponse(updatedEntity))
//                .build();
//    }
//

//
//    @Transactional
//    public ItemResponse<Long> modifyMyInformation(MyInformationModifyRequest parameter, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServiceException {
//
//        String accessToken = tokenProvider.getTokenFromCookie(httpServletRequest);
//        String userId = tokenProvider.getUid(accessToken);
//        m_user entity = userRepository.findById(userId)
//             .orElseThrow(() -> new EntityNotFoundException(userId));
//        String beforeUserName = entity.getUserName();
//
//        m_user updatedRequestEntity = userMapper.updateFromRequest(parameter, entity);
//          // Token 정보에 포함된 정보 갱신 시 Token 갱신
//        String refreshToken = null;
//        if (!beforeUserName.equals(parameter.userName())) {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            TokenResponse tokenResponse = tokenProvider.createToken(authentication, false, updatedRequestEntity);
//            refreshToken = tokenResponse.refreshToken();
//            updatedRequestEntity.setAccessToken(tokenResponse.token());
//            updatedRequestEntity.setRefreshToken(refreshToken);
//            tokenProvider.renewalAccessTokenInCookie(httpServletResponse, tokenResponse.token());
//        }
//
//        m_user updatedEntity = userRepository.saveAndFlush(updatedRequestEntity);
//
//        return ItemResponse.<Long>builder()
//                .status(messageConfig.getCode("SUCCESS.CODE"))
//                .message(messageConfig.getMsg("UPDATE.SUCCESS.MSG"))
//                .item(1L)
//                .build();
//    }

}
