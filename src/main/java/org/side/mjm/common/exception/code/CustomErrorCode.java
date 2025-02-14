package org.side.mjm.common.exception.code;

import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class CustomErrorCode implements ErrorCode {

    private String code;
    private String msg;

    @Override
    public String getResultCode() {
        return this.code;
    }

    @Override
    public String getResultMsg() {
        return this.msg;
    }
}
