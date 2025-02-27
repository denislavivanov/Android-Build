import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    private DocumentBuilder builder;
    private Path downloadDir;
    public HashMap<String, Dependency> dependencies;

    private DependencyResolver() {
        try {
            dependencies = new HashMap<>();
            builder      = DocumentBuilderFactory.newInstance()
                                                 .newDocumentBuilder();
            
            downloadDir  = Paths.get("libs");
            Files.createDirectories(downloadDir);
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
                in = dependency.getPom(repos[i]).openStream();
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
        
        in.close();

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
        InputStream in;
        Path dependencyPath;

        for (Dependency dependency : dependencies.values()) {
            in = dependency.getFile(repos[dependency.repoIndex]).openStream();
            dependencyPath = downloadDir.resolve(dependency.getFileName());

            Files.copy(in, dependencyPath,
                StandardCopyOption.REPLACE_EXISTING);

            if (dependency.type.equals("aar")) {
                unzipFile(dependencyPath.toFile(), downloadDir.resolve(dependency.toString()));
                Files.delete(dependencyPath);
            }
        }
    }

    public void unzipFile(File path, Path dir) throws Exception {
        ZipInputStream in = new ZipInputStream(new FileInputStream(path));
        ZipEntry entry;
        Path file;

        while ((entry = in.getNextEntry()) != null) {
            file = dir.resolve(entry.getName());
            Files.createDirectories(file.getParent());

            if (!entry.isDirectory()) {
                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        
        in.close();
    }
}