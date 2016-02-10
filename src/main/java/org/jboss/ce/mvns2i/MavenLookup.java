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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class MavenLookup implements Lookup {

    private static void addJars(List<URL> urls, File dir) throws Exception {
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                addJars(urls, file);
            } else if (file.isFile() && file.getName().endsWith(".jar")) {
                urls.add(file.toURI().toURL());
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void addThis(List<URL> urls) throws Exception {
        final File tempFile = File.createTempFile("marker-", ".tmp");
        tempFile.deleteOnExit();

        File tempDir = tempFile.getParentFile();
        File classDir = new File(tempDir, "mvns2i/org/jboss/ce/mvns2i");
        if (classDir.exists() == false) {
            classDir.mkdirs();
        }
        File classFile = new File(classDir, "MavenUtils.class");
        try (InputStream is = getThisAsStream()) {
            Files.copy(is, classFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        urls.add(new File(tempDir, "mvns2i/").toURI().toURL());
    }

    private InputStream getThisAsStream() {
        ClassLoader cl = getClass().getClassLoader();
        return cl.getResourceAsStream("org/jboss/ce/mvns2i/MavenUtils.class");
    }

    public String getDeploymentDir(String[] args) throws Exception {
        File mavenLibs = new File(args[1]);
        if (mavenLibs.exists() == false) {
            throw new IllegalArgumentException("No such Maven libs dir: " + mavenLibs);
        }

        List<URL> urls = new ArrayList<>();
        addJars(urls, new File(mavenLibs, "boot"));
        addJars(urls, new File(mavenLibs, "lib"));
        addJars(urls, new File(mavenLibs, "conf"));
        addThis(urls);

        final ClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), null); // null parent
        final ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        try {
            Class<?> muClass = cl.loadClass("org.jboss.ce.mvns2i.MavenUtils");
            Object muInstance = muClass.newInstance();
            Method method = muClass.getMethod("getDeploymentDir", String.class);
            return (String) method.invoke(muInstance, args[0]);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }
}
