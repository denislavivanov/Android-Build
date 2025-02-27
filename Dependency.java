import org.w3c.dom.Node;


public class Dependency {

    private String group;
    private String artifact;
    private String version;
    private String scope;
    public  String type;
    public  int    repoIndex;

    public Dependency(Node group, Node artifact, 
                      Node version, Node scope) throws Exception {
        this.group     = group.getTextContent();
        this.artifact  = artifact.getTextContent();
        this.version   = version.getTextContent();
        this.scope     = scope == null ? "compile" : scope.getTextContent();
        this.repoIndex = 0;

        if (this.version.charAt(0) == '[')
            this.version = this.version.substring(1, this.version.length()-1);

        if (this.version.contains("$"))
            throw new Exception("Mvn variables NOT supported!");
    }

    public Dependency(String name) {
        String[] parts = name.split(":");

        this.group    = parts[0];
        this.artifact = parts[1];
        this.version  = parts[2];
    }

    @Override
    public String toString() {
        return group + ":" + artifact + ":" + version;
    }

    public String getScope() {
        return scope;
    }

    public String getName() {
        return group + ":" + artifact;
    }

    public String getVersion() {
        return version;
    }
}