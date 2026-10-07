package pt.psoft.g1.psoftg1.shared.infrastructure.sevice;

import java.security.SecureRandom;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

public class Base65Generator implements StringIdentifierGenerator {
    private static final SecureRandom random = new SecureRandom();
    // Alfabeto base65 (0-9, A-Z, a-z, +, -, _)
    private static final char[] BASE65 = (
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+-_!"
    ).toCharArray();

    @Override
    public String generate(SharedSessionContractImplementor session, Object object) {
        byte[] bytes = new byte[9];
        random.nextBytes(bytes);
        String res = encodeBase65(bytes);
        return res;
    }

    private String encodeBase65(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int value = 0;
        int bits = 0;
        for (byte b : bytes) {
            value = (value << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 6) {
              bits -= 6;
              int idx = Math.floorMod((value >> bits), 65);
              sb.append(BASE65[idx]);
          }
        }
        if (bits > 0) {
          int idx = Math.floorMod((value << (6 - bits)), 65);
          sb.append(BASE65[idx]);
      }
        return sb.toString();
    }
}