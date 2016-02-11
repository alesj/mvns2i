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

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class Checker {
    private final String defaultProfile = System.getProperty("profile", "default");

    private Set<Map.Entry<String, String>> ears = new HashSet<>();
    private Set<Map.Entry<String, String>> wars = new HashSet<>();
    private Set<Map.Entry<String, String>> jars = new HashSet<>();

    public String getDefaultProfile() {
        return defaultProfile;
    }

    public void addType(String type, String module) {
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(module, type);
        if ("ear".equalsIgnoreCase(type)) {
            addEar(entry);
        } else if ("war".equalsIgnoreCase(type)) {
            addWar(entry);
        } else if ("jar".equalsIgnoreCase(type)) {
            addJar(entry);
        }
    }

    private void addEar(Map.Entry<String, String> entry) {
        ears.add(entry);
    }

    private void addWar(Map.Entry<String, String> entry) {
        wars.add(entry);
    }

    private void addJar(Map.Entry<String, String> entry) {
        jars.add(entry);
    }

    public String result() {
        String result = check(ears);
        if (result == null) {
            result = check(wars);
            if (result == null) {
                result = check(jars);
            }
        }

        if (result == null) {
            throw new IllegalStateException("No default module found!");
        }

        return result;
    }

    private static String check(Set<Map.Entry<String, String>> set) {
        int size = set.size();
        switch (size) {
            case 0:
                return null;
            case 1:
                return set.iterator().next().getKey();
            default:
                throw new IllegalStateException("Ambiguous artifacts: " + set);
        }
    }
}
