package br.com.leao.gabriel.omnibus;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies the module structure declared across {@code kernel}, {@code identity}, and
 * {@code notification}.
 *
 * <p>This is the test that actually enforces every boundary decision made while splitting the
 * former monolithic package structure into modules: that {@code kernel} has no dependency back on
 * {@code identity} or {@code notification}, that {@code notification} only ever reaches into
 * {@code identity.event} (never {@code identity}'s domain model, ports, or persistence), and that
 * no module accesses another's internal types without going through a declared
 * {@code @NamedInterface} or a shared module.
 */
class ModularityTests {

  private final ApplicationModules modules = ApplicationModules.of(OmnibusApplication.class);

  @Test
  void verifiesModularStructure() {
    modules.verify();
  }
}
