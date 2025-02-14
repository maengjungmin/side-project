package org.side.mjm.common.variable;

import com.google.gson.Gson;
import org.side.mjm.common.contextHolder.ApplicationContextHolder;


public class CommonVariables {
    /*SecurityConfig, JWTFilter 에서 사용*/
    public static final String[] IGNORE_URIS = {"/login", "/logout", "/user/check", "/user/create", "/public-key", "/favicon.ico", "/error"};
    /*SecurityConfig 에서 사용*/
    public static final String[] SWAGGER_URIS = {"swagger-ui.html", "/swagger-ui/**", "/api-docs/**"};
    public static final String CONTEXT_PATH = CommonVariables.getPropertyValue("server.servlet.context-path");
    public static Gson GSON = new Gson();
    public static final String[] DEFAULT_AUTH_ID = {"AD_01"};   //default 권한 id

    public static String getPropertyValue(String key) {
        return ApplicationContextHolder.getContext().getEnvironment().getProperty(key);
    }
}