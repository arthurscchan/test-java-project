package io.github.adamkrocz;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecException;

public class SlsaVerificationTask extends DefaultTask {
  private final String slsaDirectory = "slsa";

  private List<String> urls;
  private List<ResolvedArtifact> dependencies;

  @TaskAction
  public void verify() {
    Project project = this.getProject();
    try {
      this.retrieveArtifactUrl(project.getRepositories());
      this.retrieveAllDependencies(project.getConfigurations());
      this.downloadSlsaFiles(project.getBuildDir());
      this.verifySlsaFile(project, project.getBuildDir());
    } catch (IOException e) {
      project.getLogger().lifecycle("Skipping slsa-verification: failed to complete request.");
    }
  }

  private void retrieveArtifactUrl(Collection<ArtifactRepository> repositories) {
    urls = new LinkedList<String>();

    for (ArtifactRepository repository : repositories) {
      if (repository instanceof MavenArtifactRepository) {
        String url = ((MavenArtifactRepository) repository).getUrl().toString();
        if (url.startsWith("http")) {
          urls.add(url);
        }
      }
    }
  }

  private void retrieveAllDependencies(ConfigurationContainer container) {
    Configuration configuration = container.getByName("compileClasspath");
    configuration.setTransitive(true);

    dependencies = new LinkedList<ResolvedArtifact>(
        configuration.getResolvedConfiguration().getResolvedArtifacts());
  }

  private List<String> retrieveSlsaUrls(ModuleVersionIdentifier id) {
    List<String> slsaUrls = new LinkedList<String>();

    StringBuilder slsaPath = new StringBuilder();
    slsaPath.append(id.getGroup().replace(".", "/") + "/");
    slsaPath.append(id.getName() + "/");
    slsaPath.append(id.getVersion() + "/");
    slsaPath.append(id.getName() + "-" + id.getVersion() + "-jar.intoto.build.slsa");

    for (String url : urls) {
      slsaUrls.add(url + slsaPath.toString());
    }

    return slsaUrls;
  }

  private void downloadSlsaFiles(File buildDir) throws IOException {
    Path slsaDirectoryPath = Paths.get(buildDir.getAbsolutePath(), slsaDirectory);
    Files.createDirectories(slsaDirectoryPath);

    for (ResolvedArtifact dependency : dependencies) {
      for (String url : this.retrieveSlsaUrls(dependency.getModuleVersion().getId())) {
        Path slsaFilePath =
            Paths.get(slsaDirectoryPath.toString(), url.substring(url.lastIndexOf('/') + 1));

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream out = new FileOutputStream(slsaFilePath.toFile())) {
          byte buffer[] = new byte[1024];
          int bytesRead;
          while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
            out.write(buffer, 0, bytesRead);
          }
        } catch (IOException e) {
          // No slsa file from this url, skipping this url
        }
      }
    }
  }

  private void verifySlsaFile(Project project, File buildDir) {
    Path slsaDirectoryPath = Paths.get(buildDir.getAbsolutePath(), slsaDirectory);

    for (ResolvedArtifact dependency : dependencies) {
      ModuleVersionIdentifier id = dependency.getModuleVersion().getId();
      Path slsaFilePath = Paths.get(slsaDirectoryPath.toString(),
          id.getName() + "-" + id.getVersion() + "-jar.intoto.build.slsa");
      if (slsaFilePath.toFile().isFile()) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
          String[] args = {"verify-artifact", "--provenance-path",
              slsaFilePath.toFile().getAbsolutePath(), "--source-uri", "./",
              dependency.getFile().getAbsolutePath()};
          project.exec(execSpec -> {
            execSpec.setExecutable(System.getenv("GOHOME") + "bin/slsa-verifier");
            execSpec.setArgs(Arrays.asList(args));
            execSpec.setStandardOutput(out);
          });
        } catch (ExecException e) {
          // TODO: Fix the exception handling when slsa-verifier is ready.
        }
        project.getLogger().lifecycle(out.toString());
      } else {
        String moduleString = id.getGroup() + ":" + id.getName() + ":" + id.getVersion();
        project.getLogger().lifecycle(
            "Skipping slsa verification for " + moduleString + ": No slsa file found.");
      }
    }
  }
}
