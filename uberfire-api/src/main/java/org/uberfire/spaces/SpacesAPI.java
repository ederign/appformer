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

        return uri;
    }

    static String resolveFileSystem(Scheme scheme,
                                    Space space,
                                    String fsName) {
        String uri = scheme + "://" + space + "/" + fsName;

        return uri;
    }

    static String resolveFileSystem(Scheme scheme,
                                    String space,
                                    String fsName) {
        String uri = scheme + "://" + space + "/" + fsName;

        return uri;
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
