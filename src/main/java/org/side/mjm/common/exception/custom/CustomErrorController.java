package org.side.mjm.common.exception.custom;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.response.structure.ErrorResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping(value = "/error")
    public ResponseEntity<ErrorResponse> error(HttpServletRequest request, HttpServletResponse response) {
        throw new ServiceException(CommonErrorCode.SERVICE_ERROR);
    }
}
