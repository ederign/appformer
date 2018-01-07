package org.uberfire.spaces;

public interface SpacesAPI {

    //refactor space for a entity?
    //schmes for a entyt?
    //fix overload
    //fix names
    static String resolveFileSystem(String scheme,
                                    String space,
                                    String fsName) {
        String uri = scheme + "://" + space + "/" + fsName;
        uri = uri.replaceAll(" ",
                            "%20");
        return uri;
    }

    static String resolveFileSystem(Scheme scheme,
                                    Space space,
                                    String fsName) {
        String uri = scheme + "://" + space + "/" + fsName;
        uri = uri.replaceAll(" ",
                            "%20");
        return uri;
    }

    static String resolveFileSystem(Scheme scheme,
                                    String space,
                                    String fsName) {
        //TODO check regex of FS name
        String uri = scheme + "://" + space + "/" + fsName;
        return uri;
    }

    public static String sanitizeFileSystemName(final String fileSystemName) {
        // Only [A-Za-z0-9_\-.] are valid so strip everything else out
        return fileSystemName != null ? fileSystemName.replaceAll("[^A-Za-z0-9_\\-.]",
                                                            "") : fileSystemName;
    }

    enum Space {

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

    enum Scheme {
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
