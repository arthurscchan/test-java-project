// Copyright 2023 SLSA Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.github.adamkorcz;

import java.io.File;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;

public class TestSlsaVerificationMojo extends AbstractMojoTestCase {
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        // required
        super.setUp();
    }

    /** {@inheritDoc} */
    protected void tearDown() throws Exception {
        // required
        super.tearDown();
    }

    /**
     * @throws Exception if any
     */
    public void testCommandInjection() throws Exception {
        File pom = new File(getBasedir(), "src/test/resources/unit/project-to-test/pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        MavenProject project = readMavenProject(new File("src/test/resources/unit/project-to-test"));
        SlsaVerificationMojo mojo = (SlsaVerificationMojo) lookupConfiguredMojo(project, "verify");
        assertNotNull(mojo);

        setVariableValueToObject(mojo, "verifierPath", ";test");
        try {
            mojo.execute();

            // Fail if no exception thrown
            fail("Command injection not blocked.");
        } catch (MojoFailureException e) {
          // known exception
        }
        setVariableValueToObject(mojo, "verifierPath", "|test");
        try {
            mojo.execute();

            // Fail if no exception thrown
            fail("Command injection not blocked.");
        } catch (MojoFailureException e) {
          // known exception
        }
    }

    protected MavenProject readMavenProject(File basedir) throws ProjectBuildingException, Exception {
        File pom = new File(basedir, "pom.xml");
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setBaseDirectory(basedir);
        ProjectBuildingRequest configuration = request.getProjectBuildingRequest();
        configuration.setRepositorySession(new DefaultRepositorySystemSession());
        MavenProject project = lookup(ProjectBuilder.class).build(pom, configuration).getProject();
        assertNotNull(project);
        return project;
    }
}
