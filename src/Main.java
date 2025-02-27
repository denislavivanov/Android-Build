
public class Main {
    public static void main(String[] args) throws Exception {
        DependencyResolver resolver = DependencyResolver.getInstance();
        // resolver.getDependencies(new Dependency("com.android.support.constraint:constraint-layout:1.1.3"));
        // resolver.downloadDependencies();
        
        ApkBuilder builder = new ApkBuilder(29, 23, resolver.dependencies);
        builder.compileSource();
        builder.mergeDexFiles();
    }
}