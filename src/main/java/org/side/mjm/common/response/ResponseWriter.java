package org.side.mjm.common.response;

import jakarta.servlet.ServletResponse;
import org.side.mjm.common.response.structure.ErrorResponse;
import org.side.mjm.common.variable.CommonVariables;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;

public class ResponseWriter {

    public static void setResponseWriter(
            ServletResponse response, String resultCode, String resultMsg) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(CommonVariables.GSON.toJson(ErrorResponse.builder()
                    .status(resultCode)
                    .message(resultMsg)
                    .build()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }
}
