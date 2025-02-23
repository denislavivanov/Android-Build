


public class Dependency {

    private String  group;
    private String  artifact;
    private String  version;
    private boolean isJar;

    public Dependency(String group, String artifact, 
                      String version, boolean isJar) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.isJar = isJar;
    }

    @Override
    public String toString() {
        return group + ":" + artifact + ":" + version;
    }

    public String getPackage() {
        return group + ":" + artifact;
    }

    public int getVersion() {
        String[] parts = version.split("\\.");
        int result = 0;

        for (String part : parts) {
            result += Integer.valueOf(part);
        }

        return result;
    }

    public String getVersionStr() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}