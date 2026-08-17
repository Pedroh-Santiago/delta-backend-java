package br.com.deltaglobalbank.sharedauth.revocation;

import java.util.UUID;

public final class RevocationKeys {

    private RevocationKeys() {
    }

    public static String jti(String jti) {
        return "revoked:jti:" + jti;
    }

    public static String userSince(UUID userId) {
        return "revoked:user:" + userId + ":since";
    }
}
