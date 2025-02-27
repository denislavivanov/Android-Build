
public class Main {
    public static void main(String[] args) throws Exception {
        DependencyResolver resolver = DependencyResolver.getInstance();
        resolver.getDependencies(new Dependency("com.android.support.constraint:constraint-layout:2.0.4"));
        // resolver.downloadDependencies();

        ApkBuilder builder = new ApkBuilder(resolver.dependencies);

    }

    dx --dex libs/com.android.support:appcompat-v7:28.0.0/classes.jar
    d8 libs/com.android.support:appcompat-v7:28.0.0/classes.jar --lib $ANDROID_SDK_ROOT/platforms/android-23/android.jar
}   java -jar desugar.jar --input=libs/com.android.support:appcompat-v7:28.0.0/classes.jar --output=converted.jar
