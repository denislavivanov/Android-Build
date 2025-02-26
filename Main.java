
public class Main {
    public static void main(String[] args) throws Exception {
        PomParser parser = PomParser.getInstance();
        parser.getDependencies("androidx.constraintlayout:constraintlayout:2.2.0");
    }
}