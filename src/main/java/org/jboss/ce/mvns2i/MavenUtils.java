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
import java.util.List;

import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
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
 * TODO -- still needs impl
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class MavenUtils {
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

        ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
        request.setLocalRepository(new MavenArtifactRepository());
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

        File mvnDir = new File(projectDir, ".m2");
        if (mvnDir.exists() == false) {
            mvnDir.mkdir();
        }

        LocalRepository localRepository = new LocalRepository(mvnDir);
        LocalRepositoryManager localRepositoryManager = create(LocalRepositoryManagerFactory.class).newInstance(session, localRepository);
        session.setLocalRepositoryManager(localRepositoryManager);
        request.setRepositorySession(session);

        MavenProject project = projectBuilder.build(pomFile, request).getProject();
        List<String> modules = project.getModules();

        Checker checker = new Checker();

        for (String module : modules) {
            // TODO
        }

        return checker.result();
    }
}
