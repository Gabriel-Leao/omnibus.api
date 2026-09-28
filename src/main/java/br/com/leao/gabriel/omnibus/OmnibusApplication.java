package br.com.leao.gabriel.omnibus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Main class used to initialise the application.
 */
@Modulithic(sharedModules = "kernel")
@SpringBootApplication
public class OmnibusApplication {

  /**
   * Starts the Omnibus API application.
   *
   * @param args command-line arguments passed to the application
   */
  public static void main(String[] args) {
    SpringApplication.run(OmnibusApplication.class, args);
  }
}
