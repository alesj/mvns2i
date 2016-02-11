/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.ce.mvns2i;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.bridge.MavenRepositorySystem;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MavenUtils {
    private static final Logger log = Logger.getLogger(MavenUtils.class.getName());
    private PlexusContainer container;

    public MavenUtils() throws Exception {
        ContainerConfiguration configuration = new DefaultContainerConfiguration();
        configuration.setClassPathScanning(PlexusConstants.SCANNING_ON);
        container = new DefaultPlexusContainer(configuration);
    }

    private <T> T create(Class<T> clazz) throws Exception {
        return container.lookup(clazz);
    }

    public String getDeploymentDir(String projectDir) throws Exception {
        ProjectBuilder projectBuilder = create(ProjectBuilder.class);

        File pomFile = new File(projectDir, "pom.xml");

        MavenExecutionRequest mer = new DefaultMavenExecutionRequest();

        File mvnDir;
        String m2dir = System.getProperty("maven.repo");
        if (m2dir != null) {
            mvnDir = new File(m2dir);
        } else {
            mvnDir = new File(System.getProperty("user.home"), ".m2/repository");
        }
        if (mvnDir.exists() == false) {
            //noinspection ResultOfMethodCallIgnored
            mvnDir.mkdirs();
        }
        log.info(String.format("Using Maven repository: %s", mvnDir));

        ProjectBuildingRequest request = mer.getProjectBuildingRequest();

        MavenArtifactRepository artifactRepository = new MavenArtifactRepository(
            "local",
            mvnDir.toURI().toString(),
            create(ArtifactRepositoryLayout.class),
            new ArtifactRepositoryPolicy(),
            new ArtifactRepositoryPolicy()
        );
        request.setLocalRepository(artifactRepository);

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setOffline(!Boolean.getBoolean("online")); // use offline by default

        LocalRepository localRepository = new LocalRepository(mvnDir);
        LocalRepositoryManager localRepositoryManager = create(LocalRepositoryManagerFactory.class).newInstance(session, localRepository);
        session.setLocalRepositoryManager(localRepositoryManager);
        request.setRepositorySession(session);

        MavenRepositorySystem mrs = create(MavenRepositorySystem.class);
        ArtifactRepository remoteRepository = mrs.createDefaultRemoteRepository(mer);
        List<ArtifactRepository> repositories = Arrays.asList(artifactRepository, remoteRepository);
        request.setRemoteRepositories(repositories);

        MavenProject project = projectBuilder.build(pomFile, request).getProject();
        Checker checker = new Checker();
        recurse(projectBuilder, request, checker, new File(projectDir), "", project);

        return checker.result();
    }

    private void recurse(ProjectBuilder projectBuilder, ProjectBuildingRequest request, Checker checker, File parentDir, String prefix, MavenProject project) throws Exception {
        List<String> modules = project.getModules();
        for (String module : modules) {
            File moduleDir = new File(parentDir, module);
            File modulePomFile = new File(moduleDir, "pom.xml");
            MavenProject subProject = projectBuilder.build(modulePomFile, request).getProject();
            String packaging = subProject.getPackaging();
            if ("pom".equals(packaging)) {
                recurse(projectBuilder, request, checker, moduleDir, prefix + module + "/", subProject);
            } else {
                checker.addType(packaging, prefix + module);
            }
        }
    }
}
