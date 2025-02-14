package org.side.mjm.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.code.ErrorCode;
import org.side.mjm.common.response.structure.ErrorResponse;
import org.side.mjm.common.response.structure.ItemResponse;
import org.side.mjm.config.jwt.record.TokenResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@OpenAPIDefinition(
        info = @Info(title = "SIDE PROJECT API Documentation",
                description = """
                        
                         - 표준 API v1.3.0
                        """, version = "v1.0"),
        servers = @Server(url = "/side-project-api")
)
@SecurityScheme(
        name = "JWT Token",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@Configuration
public class SwaggerConfig {
    public static final List<String> ignoreMethods = List.of("login", "getPublicKey");

    @Bean
    public GroupedOpenApi version1APi() {
        return GroupedOpenApi.builder()
                .group("v1.0")
                .pathsToMatch("/**")
                .addOperationCustomizer(operationCustomizer())
                .build();
    }

    @Bean
    public GroupedOpenApi version2APi() {
        return GroupedOpenApi.builder().group("v2.0").pathsToMatch("/v2/**").build();
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            ApiResponses apiResponses = operation.getResponses();
            if (apiResponses == null) {
                apiResponses = new ApiResponses();
                operation.setResponses(apiResponses);
            }
            ApiResponse response = apiResponses.get("200");
            apiResponses.put("OK", response);
            apiResponses.remove("200");
            apiResponses.remove("500");
            apiResponses.putAll(getDefaultResponses(handlerMethod));
            return operation;
        };
    }

    private Map<String, ApiResponse> getDefaultResponses(HandlerMethod handlerMethod) {
        LinkedHashMap<String, ApiResponse> responses = new LinkedHashMap<>();
        boolean isAuthentication = true;
        responses.put("ERR_CT", clientError());
        if (ignoreMethods.contains(handlerMethod.getMethod().getName())) {
            isAuthentication = false;
        }
        responses.put("ERR_AT", authenticationError(isAuthentication));
        responses.put("ERR_FD", forbiddenError());
        responses.put("ERR_SV", serverError());

        return responses;
    }

    private ApiResponse clientError() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("""
                Bad Request
                - 요청한 정보가 올바른지 확인한다.

                상세 코드
                - 01 : 요청하신 URI를 찾을 수 없습니다.
                - 02 : 요청 메소드를 지원하지 않습니다.
                - 03 : 요청하신 컨텐트 타입을 지원하지 않습니다.
                - 04 : 적합하지 않은 인자가 전달되었습니다.
                - 05 : 필수 파라메터가 존재하지 않습니다.
                - 06 : 접근할 수 없는 정보에 접근이 발생합니다.
                - 07 : 해당하는 ID를 찾을 수 없습니다.
                - 08 : 이미 존재하는 ID 입니다.
                """);
        addContent(apiResponse, CommonErrorCode.URI_NOT_FOUND);
        return apiResponse;
    }

    private ApiResponse forbiddenError() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("""
                Forbidden
                - 권한이 올바른지 확인한다.
                """);
        addContent(apiResponse, CommonErrorCode.FORBIDDEN);
        return apiResponse;
    }

    private ApiResponse serverError() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("""
                Internal Server Error (Unchecked Exception)
                - API 담당자에게 오류 확인을 요청한다.
                                
                상세 코드
                 - 01 : 요청하신 서비스에 문제가 있습니다.
                 - 02 : 데이터를 처리하는데 실패하였습니다.
                """);
        addContent(apiResponse, CommonErrorCode.SERVICE_ERROR);
        return apiResponse;
    }

    private ApiResponse authenticationError(boolean isAuthentication) {
        ApiResponse apiResponse = new ApiResponse();
        String description;
        if (isAuthentication) {
            description = """
                    Unauthorized
                    - 인증 관련 정보를 확인한다.
                    - 메시지는 '자격 증명에 실패하였습니다.' 로 동일.
                                    
                    상세 코드
                                    
                    - 01 : 인증 정보 없음
                    - 02 : 중복 로그인
                    - 03 : 토큰 만료
                    """;
        } else {
            description = """
                    Unauthorized
                    - 인증 관련 정보를 확인한다.
                    - 메시지는 '자격 증명에 실패하였습니다.' 로 동일.
                                    
                    상세 코드
                                    
                    - 04 : 일치하는 사용자 없음
                    - 05 : 패스워드 오류
                    """;
        }
        apiResponse.setDescription(description);
        addContent(apiResponse, CommonErrorCode.NOT_AUTHENTICATION);
        return apiResponse;
    }

    @SuppressWarnings("rawtypes")
    private void addContent(ApiResponse apiResponse, ErrorCode errorCode) {
        Content content = new Content();
        MediaType mediaType = new MediaType();
        Schema schema = new Schema<>();
        schema.$ref("#/components/schemas/ErrorResponse");
        mediaType.schema(schema).example(ErrorResponse.builder()
                .status(errorCode.getResultCode())
                .message(errorCode.getResultMsg())
                .build());
        content.addMediaType("application/json", mediaType);
        apiResponse.setContent(content);
    }
}
