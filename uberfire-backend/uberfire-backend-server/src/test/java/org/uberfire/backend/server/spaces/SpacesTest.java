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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpacesTest {

    Spaces spaces;

    @Before
    public void setup() {
        spaces = new Spaces();
    }

    @Test
    public void labs() {
        URI uri = URI.create("default://bla");
        System.out.println(uri.getScheme());

        uri = URI.create("git://bla");
        System.out.println(uri.getScheme());
    }

    @Test
    public void resolveFileSystemURI() {
        assertEquals("default://system/system",
                     spaces.resolveFileSystemURI("default",
                                                 "system",
                                                 "system").toString());

        assertEquals("default://system/system",
                     spaces.resolveFileSystemURI(Spaces.Scheme.DEFAULT,
                                                 Spaces.Space.DEFAULT,
                                                 "system").toString());
    }
}