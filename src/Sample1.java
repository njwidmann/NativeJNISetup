import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;

/**
 * Description
 *
 * @author Nick Widmann
 * @version 6/28/2017
 */
public class Sample1 {
    public native int intMethod(int n);

    public native boolean booleanMethod(boolean bool);

    public native String stringMethod(String text);

    public native int intArrayMethod(int[] intArray);

    public static void main(String[] args) {

        String cwd = System.getProperty("user.dir") + "/src";

        //System.setProperty("java.library.path", System.getProperty("java.library.path") + ";" + cwd);
        //Properties test = System.getProperties();
        try {
            addLibraryPath(cwd);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        System.loadLibrary("Sample1");

        Sample1 sample = new Sample1();
        int square = sample.intMethod(5);
        boolean bool = sample.booleanMethod(true);
        String text = sample.stringMethod("JAVA");
        int sum = sample.intArrayMethod(
                new int[]{1, 1, 2, 3, 5, 8, 13});

        System.out.println("intMethod: " + square);
        System.out.println("booleanMethod: " + bool);
        System.out.println("stringMethod: " + text);
        System.out.println("intArrayMethod: " + sum);
    }


    /**
     * Adds the specified path to the java library path
     *
     * @param pathToAdd the path to add
     * @throws Exception
     */
    public static void addLibraryPath(String pathToAdd) throws Exception {
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        //get array of paths
        final String[] paths = (String[]) usrPathsField.get(null);

        //check if the path to add is already present
        for (String path : paths) {
            if (path.equals(pathToAdd)) {
                return;
            }
        }

        //add the new path
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }
}
