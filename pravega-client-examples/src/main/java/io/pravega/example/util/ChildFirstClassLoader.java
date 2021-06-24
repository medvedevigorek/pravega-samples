package io.pravega.example.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class ChildFirstClassLoader extends URLClassLoader {

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("io.pravega.example.util.ChildFirstClassLoader.debug", "false"));

    public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            try {
                if(DEBUG) {
                    System.out.println("Class [" + name + "] is not found in loaded classes. Attempt to load it from the given urls");
                }
                loadedClass = findClass(name);
            } catch (ClassNotFoundException e) {
                if(DEBUG) {
                    System.out.println("Class [" + name + "] does not exist in the given urls. Loading from the parent class loader");
                }
                loadedClass = super.loadClass(name, resolve);
            }
        } else {
            if(DEBUG) {
                System.out.println("Class [" + name + "] is found in loaded classes. Loaded by " + loadedClass.getClassLoader());
            }
        }

        if (resolve) {
            resolveClass(loadedClass);
        }

        if(DEBUG) {
            System.out.println("Class [" + name + "] is loaded by " + loadedClass.getClassLoader());
        }
        return loadedClass;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> allRes = new LinkedList<>();

        // load resource from this classloader
        Enumeration<URL> thisRes = findResources(name);
        if (thisRes != null) {
            while (thisRes.hasMoreElements()) {
                final URL resource = thisRes.nextElement();
                allRes.add(resource);
            }
        }

        // then try finding resources from parent classloaders
        Enumeration<URL> parentRes = super.findResources(name);
        if (parentRes != null) {
            while (parentRes.hasMoreElements()) {
                allRes.add(parentRes.nextElement());
            }
        }

        return new Enumeration<URL>() {
            Iterator<URL> it = allRes.iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public URL nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public URL getResource(String name) {
        URL res = findResource(name);
        if (res == null) {
            res = super.getResource(name);
        }
        return res;
    }

    @Override
    protected void finalize() throws Throwable {
        if(DEBUG) {
            System.out.println("Instance " + this + " is garbage collected");
        }
    }
}