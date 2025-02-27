import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ApkBuilder {
    Map<String, Dependency> dependencies;
    int     sdkVersion;
    char    pathSeparator;
    boolean useD8;
    String  apiPath;
    String  compilerPath;

    public ApkBuilder(int buildToolsVersion, int sdkVersion, Map<String, Dependency> dependencies) {   
        this.apiPath = String.format("%s/platforms/android-%d/android.jar", 
                        System.getenv("ANDROID_SDK_ROOT"), sdkVersion);

        this.pathSeparator = System.getProperty("os.name").contains("Win") ? ';' : ':';
        this.sdkVersion    = sdkVersion;
        this.dependencies  = dependencies;
        
        if (buildToolsVersion > 28) { //d8 >= 28.0.1
            this.useD8        = true;
            this.compilerPath = String.format("%s/build-tools/35.0.1/lib/d8.jar", 
                                         System.getenv("ANDROID_SDK_ROOT"));
        }
        else {
            this.useD8        = false;
            this.compilerPath = String.format("%s/build-tools/35.0.1/lib/dx.jar", 
                                         System.getenv("ANDROID_SDK_ROOT"));
        }
    }
    
    void compileDependencies() throws IOException, InterruptedException {
        List<String> params = new ArrayList<String>();
        params.add("--output");
        params.add("libs/");
        
        for (Dependency dependency : dependencies.values()) {
            String path = "libs/" + dependency;
            
            if (dependency.type.equals("aar")) {
                path += "/classes.jar";                
            }
            else {
                path += ".jar";
            }

            params.add(path);
        }
        
        runDX(params);
    }
    
    void compileSource() throws IOException, InterruptedException {
        List<String> cmdLine;
        File         sourceDir;
        File[]       sources;
        Process      proc;

        sourceDir = new File("test/");
        cmdLine   = new ArrayList<>();
        sources   = sourceDir.listFiles((d, name) -> name.endsWith(".java"));
        
        cmdLine.add("javac");
        cmdLine.add("-encoding");
        cmdLine.add("utf8");
        
        if (sdkVersion < 32) {
            cmdLine.add("--release");
            cmdLine.add("8");
        }

        cmdLine.add("-cp");
        cmdLine.add(apiPath);
        
        for (File source : sources) {
            cmdLine.add(source.getPath());
        }
        
        proc = new ProcessBuilder(cmdLine).inheritIO().start();
        proc.waitFor();
    }
    
    void mergeDexFiles() throws IOException, InterruptedException {
        List<String> params;
        File         classDir;
        File[]       classes;

        params   = new ArrayList<>();
        classDir = new File("test/");
        classes  = classDir.listFiles((d, name) -> name.endsWith(".class"));

        for (File classFile : classes) {
            params.add(classFile.getPath());
        }

        params.add("libs/classes.dex");
        runDX(params);
    }
    
    private void runDX(List<String> params) throws IOException, InterruptedException {
        List<String> cmdLine;
        Process      proc;

        cmdLine = new ArrayList<>();
        cmdLine.add("java");
        cmdLine.add("-Xmx1024M");
        cmdLine.add("-Xss1m");
        
        if (useD8) {
            cmdLine.add("-cp");
            cmdLine.add(compilerPath);
            cmdLine.add("com.android.tools.r8.D8");
            cmdLine.add("--release");
            cmdLine.add("--lib");
            cmdLine.add(apiPath);
        }
        else {
            cmdLine.add("-jar");
            cmdLine.add(compilerPath);
            cmdLine.add("--dex");
        }
        
        cmdLine.addAll(params);

        proc = new ProcessBuilder(cmdLine).inheritIO().start();
        proc.waitFor();
    }
}
