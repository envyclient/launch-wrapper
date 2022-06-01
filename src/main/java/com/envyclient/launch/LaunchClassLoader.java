package com.envyclient.launch;

import com.envyclient.fusion.Fusion;
import com.envyclient.launch.util.ClassPath;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.nio.file.Paths;

public class LaunchClassLoader extends URLClassLoader {

    private static final ClassPath CLASS_PATH = new ClassPath();

    public LaunchClassLoader() {
        super(CLASS_PATH.getUrls());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (CLASS_PATH.hasClass(name)) {
            byte[] data = CLASS_PATH.getClassData(name);
            return defineClass(name, data, 0, data.length);
        }
        return super.findClass(name);
    }

    public void transform() {
        CLASS_PATH.map(new Fusion().export());
    }

    /**
     * Adds a file to the class path
     *
     * @param file file that you want to add to the class path
     */

    public void addToClassPath(File file) {
        CLASS_PATH.loadJar(file, true);
        try {
            addURL(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     *  Required for Java Agents when this classloader is used as the system classloader
     */
    @SuppressWarnings("unused")
    private void appendToClassPathForInstrumentation(String jarfile) throws IOException {
        addURL(Paths.get(jarfile).toRealPath().toUri().toURL());
    }

}
