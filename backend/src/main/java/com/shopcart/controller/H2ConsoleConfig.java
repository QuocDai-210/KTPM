package com.shopcart.controller;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
@Profile("!postgres")
@ConditionalOnProperty(name = "shopcart.h2-console.enabled", havingValue = "true", matchIfMissing = true)
public class H2ConsoleConfig {
  @Bean(initMethod = "start", destroyMethod = "stop")
  Server h2WebServer() throws SQLException {
    return Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
  }

  @Controller
  static class H2ConsoleRedirectController {
    @GetMapping({"/h2-console", "/h2-console/"})
    String redirectToH2Console() {
      return "redirect:http://localhost:8082";
    }
  }
}
