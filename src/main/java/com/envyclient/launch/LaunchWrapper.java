package com.envyclient.launch;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LaunchWrapper {

    private static final LaunchClassLoader LAUNCH_CLASS_LOADER = new LaunchClassLoader();

    private static final File WORKING_DIRECTORY = new File("minecraft");
    private static final File ASSETS_DIRECTORY = new File(WORKING_DIRECTORY, "assets");

    public void launch(String[] launchArgs, boolean development) throws NoSuchMethodException, ClassNotFoundException {
        // load the main class
        Class<?> mainClass = loadMain(development);

        // get the main method
        Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);

        // invoke the main method
        try {
            mainMethod.invoke(mainClass, new Object[]{launchArgs});
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the main class
     *
     * @param development flag containing if the client has launched in the development mode
     * @return {@link Class}
     */

    private Class<?> loadMain(boolean development) throws ClassNotFoundException {

        // if the development mode is enabled
        if (!development) {

            // transform the classes
            LAUNCH_CLASS_LOADER.transform();

            // update the context class loader
            Thread.currentThread().setContextClassLoader(LAUNCH_CLASS_LOADER);

            // get the main class of the application
            return Class.forName("net.minecraft.client.main.Main", false, LAUNCH_CLASS_LOADER);
        }

        // else just search for the main class
        return Class.forName("net.minecraft.client.main.Main");
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException {
        // define a new option parser
        OptionParser optionParser = new OptionParser();

        // setup the options
        OptionSpec<String> assetIndexOption = optionParser.accepts("assetIndex").withRequiredArg();
        OptionSpec<Boolean> developmentOption = optionParser.accepts("dev").withOptionalArg().ofType(Boolean.class);

        // parse the options
        OptionSet optionSet = optionParser.parse(args);

        // flag containing if client was launched in development mode
        boolean development = developmentOption.value(optionSet);

        // launch the client
        new LaunchWrapper().launch(development ? new String[]{
                "--username", "Player",
                "--version", "SDK",
                "--accessToken", "0",
                "--userProperties", "{}",
                "--assetIndex", assetIndexOption.value(optionSet),
                "--assetsDir", ASSETS_DIRECTORY.getAbsolutePath(),
                "--gameDir", WORKING_DIRECTORY.getAbsolutePath()
        } : args, development);
    }

}
