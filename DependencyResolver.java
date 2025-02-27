import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
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
    public HashMap<String, Dependency> dependencies;

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

    public void getDependencies(Dependency dependency) throws Exception {
        Node        type;
        Element      tag;
        Document     dom;
        NodeList    tags;
        Dependency  curr;
        InputStream in = null;

        if (dependencies.containsKey(dependency.getName()))
            return;

        /* Loop through all repositories to find the package */
        for (int i = 0; i < repos.length; ++i) {
            try {
                in = new URL(buildURL(dependency.toString(), repos[i], 
                    "pom")).openStream();
                dependency.repoIndex = i;
                break;
            }
            catch (FileNotFoundException e) {
            }
        }

        if (in == null)
            return;

        dom  = builder.parse(in);
        tags = dom.getElementsByTagName("dependency");
        type = dom.getElementsByTagName("packaging").item(0);

        dependencies.put(dependency.getName(), dependency);
        dependency.type = type == null ? "jar" : type.getTextContent();

        for (int i = 0; i < tags.getLength(); ++i) {
            tag  = (Element)tags.item(i);
            curr = new Dependency(
                tag.getElementsByTagName("groupId").item(0),
                tag.getElementsByTagName("artifactId").item(0),
                tag.getElementsByTagName("version").item(0),
                tag.getElementsByTagName("scope").item(0)
            );

            if (curr.getScope().equals("test"))
                continue;

            getDependencies(curr);
        }
    }

    public void downloadDependencies() throws Exception {
        InputStream   in;
        StringBuilder sb;
        Path dependencyPath;

        for (Dependency dependency : dependencies.values()) {
            in = new URL(buildURL(dependency.toString(), repos[dependency.repoIndex], 
                        dependency.type)).openStream();
            sb = new StringBuilder(downloadDir);
            sb.append(dependency.toString());
            sb.append(".");
            sb.append(dependency.type);

            dependencyPath = Paths.get(sb.toString());

            Files.copy(in, dependencyPath, 
                StandardCopyOption.REPLACE_EXISTING);

            if (dependency.type.equals("aar")) {
                unzipFile(dependencyPath.toFile(), downloadDir + dependency.toString() + "/");
                Files.delete(dependencyPath);
            }
        }
    }

    public void unzipFile(File path, String dir) throws Exception {
        ZipInputStream in = new ZipInputStream(new FileInputStream(path));
        ZipEntry entry;
        File  destFile;

        while ((entry = in.getNextEntry()) != null) {
            destFile = new File(dir, entry.getName());
            destFile.getParentFile().mkdirs();

            if (!entry.isDirectory()) {
                Files.copy(in, destFile.toPath());
            }
        }
    }
}