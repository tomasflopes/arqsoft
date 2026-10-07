package pt.psoft.g1.psoftg1.shared.infrastructure.sevice;

import java.security.SecureRandom;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

public class TimestampGenerator implements StringIdentifierGenerator {
  private static final SecureRandom random = new SecureRandom();

  @Override
  public String generate(SharedSessionContractImplementor session, Object object) {
    long timestamp = System.currentTimeMillis();
    int suffix = random.nextInt(0x1000000); // 6 dígitos hexadecimais (0xFFFFFF + 1)
    return timestamp + String.format("%06x", suffix);
  }
}
