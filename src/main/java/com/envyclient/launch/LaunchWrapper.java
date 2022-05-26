package com.envyclient.launch;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class LaunchWrapper {

    private static final Class<?> MAIN_CLASS;
    private static final Method MAIN_METHOD;

    private static final File WORKING_DIRECTORY;
    private static final File ASSETS_DIRECTORY;

    private static final String[] LAUNCH_ARGUMENTS;

    public static void main(String[] args) {
        try {
            MAIN_METHOD.invoke(MAIN_CLASS, new Object[]{concat(LAUNCH_ARGUMENTS, args)});
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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

    static {
        try {
            MAIN_CLASS = Class.forName("net.minecraft.client.main.Main");
            MAIN_METHOD = MAIN_CLASS.getDeclaredMethod("main", String[].class);
            WORKING_DIRECTORY = new File("minecraft");
            ASSETS_DIRECTORY = new File(WORKING_DIRECTORY, "assets");

            LAUNCH_ARGUMENTS = new String[]{
                    "--username", "Player",
                    "--version", "SDK",
                    "--accessToken", "0",
                    "--userProperties", "{}",
                    "--assetsDir", ASSETS_DIRECTORY.getAbsolutePath(),
                    "--gameDir", WORKING_DIRECTORY.getAbsolutePath()
            };
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
