package org.side.mjm.common.exception.code;

import lombok.Getter;

@Getter
public enum CommonErrorCode implements ErrorCode {

    /*ERR_01 -> Server exception*/
    SERVICE_ERROR("ER_SV_01", "요청한 서비스에 문제가 발생했습니다. 잠시 후에 다시 시도해 주세요."), /*Unchecked Exception*/
    LOGIC_ERROR("ER_SV_02", "데이터를 처리하는데 실패했습니다."), /*Checked exception*/
    NO_DATA("NO_DT", "데이터가 존재하지 않습니다."),
    EXISTING_DATA("EX_DT", "이미 존재하는 데이터 입니다."),
    /*ERR_02 ->  Client exception*/
    URI_NOT_FOUND("ER_CT_01", "요청하신 URI를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED("ER_CT_02", "요청 메소드를 지원하지 않습니다."),
    UNSUPPORTED_MEDIA_TYPE("ER_CT_03", "요청하신 컨텐트 타입을 지원하지 않습니다."),
    INVALID_PARAMETER("ER_CT_04", "적합하지 않은 인자가 전달되었습니다."),
    REQUIRED_PARAMETER("ER_CT_05", "적합하지 않은 인자가 전달되었습니다."),
    NULL_POINTER("ER_CT_06", "적합하지 않은 인자가 전달되었습니다."),
    ENTITY_NOT_FOUND("ER_CT_07", "적합하지 않은 인자가 전달되었습니다."),
    ENTITY_DUPLICATED("ER_CT_08", "적합하지 않은 인자가 전달되었습니다."),
    /*ERR_03 -> Authentication exception*/
    NOT_AUTHENTICATION("ER_AT_01", "자격 증명에 실패하였습니다."),
    DUPLICATION_LOGIN("ER_AT_02", "자격 증명에 실패하였습니다."),
    EXPIRED_TOKEN("ER_AT_03", "자격 증명에 실패하였습니다."),
    NO_MATCHING_USER("ER_AT_04", "자격 증명에 실패하였습니다."),
    WRONG_PASSWORD("ER_AT_05", "자격 증명에 실패하였습니다."),
    LOCKED_MEMBER("ER_AT_06", "잠긴 회원입니다. 관리자에게 문의하세요."),
    /*ERR_04 -> Authorization*/
    FILE_WRITE_EXCEPTION("ER_AT_07", "파일을 쓰는데 실패했습니다."),
    FORBIDDEN("ER_FD", "사용자 접근이 거부 되었습니다.");

    private final String resultCode;
    private final String resultMsg;

    CommonErrorCode(String rc, String rm) {
        resultCode = rc;
        resultMsg = rm;
    }
}
