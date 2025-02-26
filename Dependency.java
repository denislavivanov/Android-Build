import org.w3c.dom.Node;


public class Dependency {

    private String group;
    private String artifact;
    private String version;
    private String scope;
    private String type;

    public Dependency(Node group, Node artifact, 
                      Node version, Node type, Node scope) throws Exception {
        this.group    = group.getTextContent();
        this.artifact = artifact.getTextContent();
        this.version  = version.getTextContent();
        this.scope    = scope == null ? "compile" : scope.getTextContent();
        this.type     = type  == null ? "jar" : type.getTextContent();

        if (this.version.charAt(0) == '[')
            this.version = this.version.substring(1, this.version.length()-1);

        if (this.version.contains("$"))
            throw new Exception("Mvn variables NOT supported!");
    }

    @Override
    public String toString() {
        return group + ":" + artifact + ":" + version;
    }

    public String getType() {
        return type;
    }

    public String getScope() {
        return scope;
    }

    public String getPackage() {
        return group + ":" + artifact;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}