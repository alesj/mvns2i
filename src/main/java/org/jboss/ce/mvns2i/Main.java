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

import java.util.Arrays;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Main {
    private static void illegalArgs(String[] args) {
        throw new IllegalArgumentException("Invalid arguments: " + Arrays.toString(args));
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 1) {
            illegalArgs(args);
        }

        int n = args.length;
        Lookup lookup = null;
        switch (n) {
            case 1:
                lookup = new XmlLookup();
                break;
            case 2:
                lookup = new MavenLookup();
                break;
            default:
                illegalArgs(args);
        }
        System.out.println(lookup.getDeploymentDir(args));
    }
}
