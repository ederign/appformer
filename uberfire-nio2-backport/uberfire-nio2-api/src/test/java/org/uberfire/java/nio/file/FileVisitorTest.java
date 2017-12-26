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
package org.uberfire.java.nio.file;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.uberfire.java.nio.IOException;
import org.uberfire.java.nio.file.attribute.BasicFileAttributes;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

public class FileVisitorTest extends AbstractBaseTest {

    final AtomicInteger preDir = new AtomicInteger();
    final AtomicInteger postDir = new AtomicInteger();
    final AtomicInteger fileC = new AtomicInteger();
    final AtomicInteger failFile = new AtomicInteger();

    final FileVisitor<Path> customVisitor = new FileVisitor<Path>() {

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                                                 BasicFileAttributes attrs) throws IOException {
            preDir.addAndGet(1);
            return FileVisitor.super.preVisitDirectory(dir,
                                                       attrs);
        }

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs) throws IOException {
            fileC.addAndGet(1);
            return FileVisitor.super.visitFile(file,
                                               attrs);
        }

        @Override
        public FileVisitResult visitFileFailed(Path file,
                                               IOException exc) throws IOException {
            failFile.addAndGet(1);
            return FileVisitor.super.visitFileFailed(file,
                                                     exc);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir,
                                                  IOException exc) throws IOException {
            postDir.addAndGet(1);
            return FileVisitor.super.postVisitDirectory(dir,
                                                        exc);
        }
    };

    @Test
    public void testWalker() {

        final Path dir = newTempDir(null);

        final Path file1 = Files.createTempFile(dir,
                                                "foo",
                                                "bar");
        Files.createTempFile(dir,
                             "foo",
                             "bar");

        cleanupVisitor();
        Files.walkFileTree(dir,
                           customVisitor);

        assertThat(preDir.get()).isEqualTo(1);
        assertThat(postDir.get()).isEqualTo(1);
        assertThat(fileC.get()).isEqualTo(2);
        assertThat(failFile.get()).isEqualTo(0);

        cleanupVisitor();
        Files.walkFileTree(file1,
                           customVisitor);

        assertThat(preDir.get()).isEqualTo(0);
        assertThat(postDir.get()).isEqualTo(0);
        assertThat(fileC.get()).isEqualTo(1);
        assertThat(failFile.get()).isEqualTo(0);
    }

    @Test
    public void testWalkerDeep2() {
        final Path dir = newTempDir(null);
        final Path subDir = newTempDir(dir);
        final Path subSubDir = newTempDir(subDir);
        newTempDir(subSubDir);

        cleanupVisitor();
        Files.walkFileTree(dir,
                           customVisitor);

        assertThat(preDir.get()).isEqualTo(4);
        assertThat(postDir.get()).isEqualTo(4);
        assertThat(fileC.get()).isEqualTo(0);
        assertThat(failFile.get()).isEqualTo(0);
    }

    @Test
    public void testWalkerDeep1() {
        final Path dir = newTempDir(null);
        final Path subDir = newTempDir(dir);
        final Path subSubDir = newTempDir(subDir);
        final Path subSubSubDir = newTempDir(subSubDir);

        Files.createTempFile(dir,
                             "foo",
                             "bar");
        Files.createTempFile(dir,
                             "foo",
                             "bar");

        cleanupVisitor();
        Files.walkFileTree(dir,
                           customVisitor);

        assertThat(preDir.get()).isEqualTo(4);
        assertThat(postDir.get()).isEqualTo(4);
        assertThat(fileC.get()).isEqualTo(2);
        assertThat(failFile.get()).isEqualTo(0);
    }

    @Test
    public void testException() {
        final Path dir = newTempDir(null);

        final Path file = Files.createTempFile(dir,
                                               "foo",
                                               "bar");

        final IOException myException = new IOException();

        try {
            customVisitor.visitFileFailed(file,
                                          myException);
            fail("should throw an exception");
        } catch (Exception ex) {
            assertThat(ex).isEqualTo(myException);
        }

        try {
            customVisitor.postVisitDirectory(file,
                                             myException);
            fail("should throw an exception");
        } catch (Exception ex) {
            assertThat(ex).isEqualTo(myException);
        }
    }

    @Test
    public void postVisitDirectoryNull1() {
        final Path dir = newTempDir(null);
        final Path file = Files.createTempFile(dir,
                                               "foo",
                                               "bar");

        customVisitor.postVisitDirectory(dir,
                                         null);
    }

    @Test(expected = IOException.class)
    public void postVisitDirectoryWithException() {
        customVisitor.postVisitDirectory(null,
                                         new IOException());
    }

    protected void cleanupVisitor() {
        preDir.set(0);
        postDir.set(0);
        fileC.set(0);
        failFile.set(0);
    }
}
