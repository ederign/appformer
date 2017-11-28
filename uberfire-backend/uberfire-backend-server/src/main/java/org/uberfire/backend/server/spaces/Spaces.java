/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.uberfire.backend.server.spaces;

import java.net.URI;
import javax.enterprise.context.ApplicationScoped;

import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;

@ApplicationScoped
@Startup(StartupType.BOOTSTRAP)
public class Spaces {

    //refactor space for a entity?
    //schmes for a entyt?
    public URI resolveFileSystemURI(String scheme,
                                    String space,
                                    String fsName) {
        String uri = scheme + "://" + space + "/" + fsName;

        return URI.create(uri);
    }

    public URI resolveFileSystemURI(Scheme scheme,
                                    Space space,
                                    String fsName) {
        String uri = scheme + "://" + space + "/" + fsName;

        return URI.create(uri);
    }

    public enum Space {

        DEFAULT("system");

        private final String name;

        Space(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Scheme {
        //TODO load from providers
        DEFAULT("default"),
        GIT("git"),
        FILE("file");

        private final String name;

        Scheme(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
