package org.side.mjm.domain.authority.rsa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.side.mjm.common.response.structure.ItemResponse;
import org.side.mjm.config.message.MessageConfig;
import org.side.mjm.config.rsa.RsaProvider;
import org.side.mjm.domain.authority.rsa.record.PublicKeyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.util.Base64;

@RestController
@Tag(name = "[API-000] RSA public key 요청")
@RequiredArgsConstructor
public class RsaController {
    private final RsaProvider rsaProvider;
    private final MessageConfig messageConfig;

    @PostMapping(value = "/public-key", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "보안이 필요한 정보를 암호화 하기 위한 RSA publicKey 요청", description = """
            # No Parameter
                        
            ※ 보안이 필요한 데이터를 public key 로 암호화 후 전송 한다. ex) password
                        
            """,
            operationId = "API-000-01")
    public ResponseEntity<ItemResponse<PublicKeyResponse>> getPublicKey() {
        PublicKey key = rsaProvider.getPublicKey();
        String keyString = Base64.getEncoder().encodeToString(key.getEncoded());
        PublicKeyResponse publicKeyResponseDTO = PublicKeyResponse.builder().publicKey(keyString).build();

        return ResponseEntity.ok()
                .body(ItemResponse.<PublicKeyResponse>builder()
                        .status(messageConfig.getCode("SUCCESS.CODE"))
                        .message(messageConfig.getMsg("SEARCH.SUCCESS.MSG"))
                        .item(publicKeyResponseDTO)
                        .build());
    }
}