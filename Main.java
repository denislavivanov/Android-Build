
public class Main {
    public static void main(String[] args) throws Exception {
        PomParser parser = PomParser.getInstance();
        parser.getDependencies("com.android.support.constraint:constraint-layout:2.0.4");
    }
}