import java.util.HashMap;

public class ApkBuilder {
    HashMap<String, Dependency> dependencies;
    String compilePath;

    public ApkBuilder(HashMap<String, Dependency> dependencies) {
        this.dependencies = dependencies;
        this.compilePath  = ""; //TODO: Change to StringBuilder
        
        compilePath = "d8 --lib $ANDROID_SDK_ROOT/platforms/android-23/android.jar ";
    
        for (Dependency d : dependencies.values()) {
            if (d.type.equals("aar")) {
                compilePath += "libs/" + d.toString() + "/classes.jar ";
            }
            else {
                compilePath += "libs/" + d.toString() + ".jar ";
            }
        }

        System.out.println(compilePath);
    }
}
