package com.savio.ytplaylistvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YtPlaylistVaultApplication {

  public static void main(String[] args) {
    SpringApplication.run(YtPlaylistVaultApplication.class, args);
  }
}
