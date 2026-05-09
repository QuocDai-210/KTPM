package com.shopcart.database.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.entity.Coupon;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;

public final class CouponSeedData {
  private static final String COUPONS_SEED_PATH = "database/seeds/coupons.json";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private CouponSeedData() {}

  public static Map<String, Coupon> loadCoupons() {
    try (InputStream inputStream = new ClassPathResource(COUPONS_SEED_PATH).getInputStream()) {
      List<Coupon> coupons = objectMapper.readValue(inputStream, new TypeReference<>() {});
      return coupons.stream().collect(Collectors.toMap(Coupon::getCode, coupon -> coupon));
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot load coupon seed data from " + COUPONS_SEED_PATH, exception);
    }
  }
}
