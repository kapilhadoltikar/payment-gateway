package com.paymentgateway.common.security;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EncryptionServiceTest {

    private final EncryptionService encryptionService = new EncryptionService();

    @Test
    void encryptAndDecrypt_WorksCorrectly() {
        String plainText = "Sensitive Data 123";
        String encrypted = encryptionService.encrypt(plainText);

        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);

        String decrypted = encryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void generateToken_ReturnsDifferentValues() {
        String token1 = encryptionService.generateToken();
        String token2 = encryptionService.generateToken();

        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void decrypt_ThrowsExceptionForInvalidData() {
        assertThatThrownBy(() -> encryptionService.decrypt("invalid-base64"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void encrypt_ThrowsExceptionForNullInput() {
        assertThatThrownBy(() -> encryptionService.encrypt(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Encryption failed");
    }
}
