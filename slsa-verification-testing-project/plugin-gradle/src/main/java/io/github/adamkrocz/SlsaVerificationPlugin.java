package io.github.adamkrocz;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SlsaVerificationPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getTasks().create("verify", SlsaVerificationTask.class);
  }
}
