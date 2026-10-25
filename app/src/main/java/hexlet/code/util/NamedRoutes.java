package hexlet.code.util;

public final class NamedRoutes {
    private NamedRoutes() {}

    public static String rootPath() {
        return "/";
    }

    public static String urlsPath() {
        return "/urls";
    }

    public static String urlPath(Object id) {
        return urlsPath() + "/" + id;
    }

    public static String urlChecksPath(Object id) {
        return urlPath(id) + "/checks";
    }
}
