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

import org.uberfire.java.nio.IOException;
import org.uberfire.java.nio.file.attribute.BasicFileAttributes;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

public class FileTree {

    public static void walk(final Path start,
                            final FileVisitor<? super Path> visitor,
                            final int maxDepth) throws IOException {
        checkNotNull("start",
                     start);
        checkNotNull("visitor",
                     visitor);
        walk(start,
             0,
             visitor,
             maxDepth);
    }

    private static FileVisitResult walk(final Path start,
                                        final int depth,
                                        final FileVisitor<? super Path> visitor,
                                        final int maxDepth)
            throws IOException {

        BasicFileAttributes startAttributes;
        DirectoryStream<? extends Path> directoryStream;

        try {
            startAttributes = loadFileAttributes(start);
            if (isAFileVisit(depth,
                             maxDepth,
                             startAttributes)) {
                return visitor.visitFile(start,
                                         startAttributes);
            }
            directoryStream = loadDirectoryStream(start);
        } catch (VisitFailedException e) {
            return visitor.visitFileFailed(start,
                                           e.getOriginalException());
        }

        IOException postVisitDirectoryException = null;

        try {
            FileVisitResult visitResult = visitor.preVisitDirectory(start,
                                                                       startAttributes);
            if (!shouldIContinue(visitResult)) {
                return visitResult;
            }

            try {
                for (final Path directory : directoryStream) {
                    visitResult = walk(directory,
                                          depth + 1,
                                          visitor,
                                          maxDepth);
                    if (shouldITerminate(visitResult)) {
                        return visitResult;
                    }
                    if (shouldISkipSiblings(visitResult)) {
                        break;
                    }
                }
            } catch (IOException ex) {
                postVisitDirectoryException = ex;
            }
        } finally {
            try {
                directoryStream.close();
            } catch (IOException ex) {
                if (postVisitDirectoryException == null) {
                    postVisitDirectoryException = ex;
                }
            }
        }

        return visitor.postVisitDirectory(start,
                                          postVisitDirectoryException);
    }

    private static boolean shouldIContinue(FileVisitResult preVisitResult) {
        return preVisitResult == FileVisitResult.CONTINUE;
    }

    private static boolean shouldISkipSiblings(FileVisitResult preVisitResult) {
        return preVisitResult == FileVisitResult.SKIP_SIBLINGS;
    }

    private static boolean shouldITerminate(FileVisitResult preVisitResult) {
        return preVisitResult == null || preVisitResult == FileVisitResult.TERMINATE;
    }

    private static DirectoryStream<Path> loadDirectoryStream(Path file) throws VisitFailedException {
        try {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(file);
            return directoryStream;
        } catch (IOException ioException) {
            throw new VisitFailedException(ioException);
        }
    }

    private static boolean isAFileVisit(int depth,
                                        int maxDepth,
                                        BasicFileAttributes fileAttributes) {
        return depthIsValid(depth,
                            maxDepth) || !isADirectory(fileAttributes);
    }

    private static boolean isADirectory(BasicFileAttributes fileAttributes) {
        return fileAttributes.isDirectory();
    }

    private static boolean depthIsValid(int depth,
                                        int maxDepth) {
        return depth >= maxDepth;
    }

    private static BasicFileAttributes loadFileAttributes(Path file) throws VisitFailedException {
        try {
            BasicFileAttributes fileAttributes = Files.readAttributes(file,
                                                                      BasicFileAttributes.class);
            return fileAttributes;
        } catch (IOException ioException) {
            throw new VisitFailedException(ioException);
        }
    }

    public static class VisitFailedException extends Exception {

        private final IOException originalException;

        public VisitFailedException(IOException originalException) {
            this.originalException = originalException;
        }

        public IOException getOriginalException() {
            return originalException;
        }
    }
}
