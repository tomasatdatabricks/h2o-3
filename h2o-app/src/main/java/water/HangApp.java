package water;

import water.util.Log;

/**
 * Expects cluster of size 2
 */
public class HangApp extends H2OStarter {
    public static void main(String[] args) {

        if (H2O.checkUnsupportedJava())
            System.exit(1);

        start(args, System.getProperty("user.dir"));

        System.out.println("Before");
        // Renumber to handle dup names
        if (DKV.get("prostate.hex") == null) {
            Log.info("All ok");
        }else{
            throw new RuntimeException("Should not be in:");
        }
        System.out.println("After");

    }

}
