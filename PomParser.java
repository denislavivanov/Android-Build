import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.util.HashMap;
import java.net.URL;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class PomParser {

    private final String mvn = "https://maven.google.com/";
    private static PomParser instance;
    private DocumentBuilder builder;
    private HashMap<String, Dependency> dependencies;

    private PomParser() {
        try {
            dependencies = new HashMap<>();
            builder = DocumentBuilderFactory.newInstance()
                                            .newDocumentBuilder();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static PomParser getInstance() {
        if (instance == null)
            instance = new PomParser();

        return instance;
    }

    private String getURL(String dependency, String ext) {
        String[] urlParts = dependency.split(":");
        String url = mvn;

        for (int i = 0; i < urlParts.length; ++i) {
            if (i < urlParts.length - 1) {
                String[] dotted = urlParts[i].split("\\.");

                if (dotted.length != 0) {
                    for (int j = 0; j < dotted.length; ++j) {
                        url += dotted[j] + "/";
                    }
                }
                else {
                    url += urlParts[i] + "/";
                }
            }
            else {
                url += urlParts[i] + "/";
            }
        }

        return String.format("%s%s-%s.%s", url, urlParts[urlParts.length-2], 
                            urlParts[urlParts.length-1], ext);
    }

    public void getDependencies(String name) throws Exception {
        NodeList  root;
        NodeList  tags;
        Document   dom;
        InputStream in;

        in   = new URL(getURL(name, "pom")).openStream();
        dom  = builder.parse(in);
        root = dom.getElementsByTagName("dependencies");

        if (root.getLength() == 0)
            return;

        tags = root.item(root.getLength() - 1).getChildNodes();

        for (int i = 1; i < tags.getLength(); i += 2) {
            Node          tag = tags.item(i);
            NodeList children = tag.getChildNodes();

            Dependency d = new Dependency(
                children.item(1).getTextContent(),
                children.item(3).getTextContent(),
                children.item(5).getTextContent(),
                children.item(9) == null
            );
    
            if (!d.toString().contains("android"))
                continue;
            
            Dependency match = dependencies.get(d.getPackage());
            
            if (match == null) {
                dependencies.put(d.getPackage(), d);
            }
            else {
                if (match.getVersion() < d.getVersion()) {
                    match.setVersion(d.getVersionStr());
                }

                continue;
            }
            
            System.out.println(d.toString());
            getDependencies(d.toString());
        }
    }
}