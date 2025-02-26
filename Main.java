
public class Main {
    public static void main(String[] args) throws Exception {
        DependencyResolver resolver = DependencyResolver.getInstance();
        resolver.getDependencies("com.android.support.constraint:constraint-layout:2.0.4");
        resolver.downloadDependencies();
    }
}