package vn.ptit.libman8.config;

import java.math.BigDecimal;
import java.time.*;

public final class PolicyConfig8 {
  private PolicyConfig8() {}

  public static final BigDecimal DAILY_LATE_FEE = new BigDecimal("5000.00");

  public static LocalDateTime now() {
    return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).withNano(0);
  }

  public static String code(String prefix) {
    return prefix + java.util.UUID.randomUUID().toString().replace("-", "");
  }
}
