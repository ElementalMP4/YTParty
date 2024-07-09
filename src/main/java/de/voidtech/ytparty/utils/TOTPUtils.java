package main.java.de.voidtech.ytparty.utils;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;

import java.time.Duration;

public class TOTPUtils {

    public static String generateCode(String secret) {
        TOTPGenerator totp = new TOTPGenerator.Builder(secret.getBytes())
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(HMACAlgorithm.SHA512);
                })
                .withPeriod(Duration.ofSeconds(30))
                .build();
        return totp.now();
    }

}
