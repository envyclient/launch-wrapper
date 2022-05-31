package com.envyclient.launch;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class LaunchWrapper {

    private static final LaunchClassLoader LAUNCH_CLASS_LOADER = new LaunchClassLoader();

    private static final File WORKING_DIRECTORY = new File("minecraft");
    private static final File ASSETS_DIRECTORY = new File(WORKING_DIRECTORY, "assets");

    public void launch(String[] launchArgs, String[] args, File libraryFile) throws NoSuchMethodException, ClassNotFoundException {
        // add the library to the class loader
        LAUNCH_CLASS_LOADER.addToClassPath(libraryFile);

        // transform the classes
        LAUNCH_CLASS_LOADER.transform();

        // update the context class loader
        Thread.currentThread().setContextClassLoader(LAUNCH_CLASS_LOADER);

        // get the main class of the application
        Class<?> mainClass = Class.forName("net.minecraft.client.main.Main", false, LAUNCH_CLASS_LOADER);

        // get the main method
        Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);

        // invoke the main method
        try {
            mainMethod.invoke(mainClass, new Object[]{launchArgs});
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException {
        // define a new option parser
        OptionParser optionParser = new OptionParser();

        // setup the options
        OptionSpec<String> assetIndexOption = optionParser.accepts("assetIndex").withRequiredArg();
        OptionSpec<File> libraryOption = optionParser.accepts("library").withOptionalArg().ofType(File.class);

        // parse the options
        OptionSet optionSet = optionParser.parse(args);

        // get the library file
        File libraryFile = libraryOption.value(optionSet);
        if (!libraryFile.exists()) {
            throw new RuntimeException("Library file was not provided");
        }

        // launch the client
        new LaunchWrapper().launch(new String[]{
                "--username", "Player",
                "--version", "SDK",
                "--accessToken", "0",
                "--userProperties", "{}",
                "--assetIndex", assetIndexOption.value(optionSet),
                "--assetsDir", ASSETS_DIRECTORY.getAbsolutePath(),
                "--gameDir", WORKING_DIRECTORY.getAbsolutePath()
        }, new String[0], libraryFile);
    }

    /**
     * Combines two arrays into a single array
     *
     * @param first  first array that you want to combine
     * @param second second array that you want to combine
     * @return combination of the two combined arrays
     */

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
