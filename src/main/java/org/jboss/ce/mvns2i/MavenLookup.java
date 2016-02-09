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
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class MavenLookup implements Lookup {
    public String getDeploymentDir(String[] args) throws Exception {
        File mavenLibs = new File(args[1]);
        if (mavenLibs.exists() == false) {
            throw new IllegalArgumentException("No such Maven libs dir: " + mavenLibs);
        }

        File[] jars = mavenLibs.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            urls[i] = jars[i].toURI().toURL();
        }

        final ClassLoader cl = new URLClassLoader(urls, MavenLookup.class.getClassLoader());
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
