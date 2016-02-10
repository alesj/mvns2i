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
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class XmlLookup implements Lookup {
    public String getDeploymentDir(String[] args) throws Exception {
        File projectDir = new File(args[0]);
        File pomXml = new File(projectDir, "pom.xml");

        Element root = XmlUtils.parseXml(pomXml).getDocumentElement();

        final Set<String> modules = new HashSet<>();

        Element modulesElt = XmlUtils.getChildElement(root, "modules");
        if (modulesElt != null) {
            for (Element moduleElt : XmlUtils.getChildren(modulesElt, "module")) {
                modules.add(XmlUtils.getBody(moduleElt));
            }
        }

        Element profilesElt = XmlUtils.getChildElement(root, "profiles");
        if (profilesElt != null) {
            for (Element profileElt : XmlUtils.getChildren(profilesElt, "profile")) {
                String id = XmlUtils.getChildElementBody(profileElt, "id");
                if ("default".equalsIgnoreCase(id)) {
                    modulesElt = XmlUtils.getChildElement(profileElt, "modules");
                    if (modulesElt != null) {
                        for (Element moduleElt : XmlUtils.getChildren(modulesElt, "module")) {
                            modules.add(XmlUtils.getBody(moduleElt));
                        }
                    }
                    break;
                }
            }
        }

        Checker checker = new Checker();

        for (String module : modules) {
            File moduleDir = new File(projectDir, module);
            File modulePom = new File(moduleDir, "pom.xml");
            Element modulePomElt = XmlUtils.parseXml(modulePom).getDocumentElement();
            String packaging = XmlUtils.getChildElementBody(modulePomElt, "packaging", true);
            checker.addType(packaging, module);
        }

        return checker.result();
    }
}
