import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.io.InputStream;
import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class DependencyResolver {

    private final static String[] repos = {
        "https://maven.google.com/",
        "https://repo1.maven.org/maven2/"
    };

    private static DependencyResolver instance;
    private String downloadDir;
    private DocumentBuilder  builder;
    private HashMap<String, Dependency> dependencies;

    private DependencyResolver() {
        Path downloadPath;

        try {
            downloadDir  = "libs/";
            dependencies = new HashMap<>();
            builder = DocumentBuilderFactory.newInstance()
                                            .newDocumentBuilder();

            downloadPath = Paths.get(downloadDir);

            if (Files.notExists(downloadPath)) {
                Files.createDirectories(downloadPath);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static DependencyResolver getInstance() {
        if (instance == null)
            instance = new DependencyResolver();

        return instance;
    }

    private String buildURL(String dependency, String repo, String fileType) {
        String[] urlParts = dependency.split(":");
        StringBuilder sb  = new StringBuilder(repo);

        for (int i = 0; i < urlParts.length-1; ++i) {
            String[] dotted = urlParts[i].split("\\.");

            if (dotted.length != 0) {
                for (int j = 0; j < dotted.length; ++j) {
                    sb.append(dotted[j]);
                    sb.append("/");
                }
            }
            else {
                sb.append(urlParts[i]);
                sb.append("/");
            }
        }

        sb.append(urlParts[urlParts.length-1]);
        sb.append("/");

        return String.format("%s%s-%s.%s", sb, urlParts[urlParts.length-2],
                            urlParts[urlParts.length-1], fileType);
    }

    public void getDependencies(String name) throws Exception {
        Element      tag;
        Document     dom;
        NodeList    tags;
        Dependency  curr;
        Dependency match;
        InputStream in = null;

        /* Loop through all repositories to find the package */
        for (String repo : repos) {
            try {
                in = new URL(buildURL(name, repo, "pom")).openStream();
                break;
            }
            catch (FileNotFoundException e) {
            }
        }

        if (in == null)
            return;

        dom  = builder.parse(in);
        tags = dom.getElementsByTagName("dependency");

        for (int i = 0; i < tags.getLength(); ++i) {
            tag  = (Element)tags.item(i);
            curr = new Dependency(
                tag.getElementsByTagName("groupId").item(0),
                tag.getElementsByTagName("artifactId").item(0),
                tag.getElementsByTagName("version").item(0),
                tag.getElementsByTagName("type").item(0),
                tag.getElementsByTagName("scope").item(0)
            );

            if (curr.getScope().equals("test"))
                continue;

            match = dependencies.get(curr.getPackage());
            
            if (match == null) {
                dependencies.put(curr.getPackage(), curr);
                getDependencies(curr.toString());
            }
        }
    }

    public void downloadDependencies() throws Exception {
        InputStream   in = null;
        StringBuilder sb;

        for (Dependency dependency : dependencies.values()) {
            for (String repo : repos) {
                try {
                    in = new URL(buildURL(dependency.toString(), repo, 
                                dependency.getType())).openStream();

                    break;
                }
                catch (FileNotFoundException e) {
                }
            }

            sb = new StringBuilder(downloadDir);
            sb.append(dependency.toString());
            sb.append(".");
            sb.append(dependency.getType());

            Files.copy(in, Paths.get(sb.toString()), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}