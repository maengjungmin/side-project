package org.side.mjm.domain.authority.rsa.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "RSA PublicKey 응답 DTO")
@Builder
public record PublicKeyResponse(
        @Schema(description = "RSA Public key", example = "MIIBIjANBgkqhkiG9w0BAQEFAAOC...")
        String publicKey) {
}