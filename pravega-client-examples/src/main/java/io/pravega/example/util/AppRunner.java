package io.pravega.example.util;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class AppRunner {

    private static final int CUSTOM_CLASSPATH_OPT_INDEX = 0;
    private static final int APPLICATION_OPT_INDEX = 1;
    private static final int LAST_OPT_INDEX = APPLICATION_OPT_INDEX;

    public static void main(String[] args) throws Exception {

        if(args.length < LAST_OPT_INDEX + 1) {
            System.out.println("Usage: AppRunner <custom classpath> <application to run> <application arguments>");
            System.exit(-1);
        }

        // parse custom class path
        final URL[] urls = Arrays.stream(args[CUSTOM_CLASSPATH_OPT_INDEX].split(";")).map(file -> {
            try {
                return new URL("file", "", file);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }).toArray(URL[]::new);

        // parse application to run
        final String appName = args[APPLICATION_OPT_INDEX];

        ClassLoader customClassLoader = new ChildFirstClassLoader(urls, Thread.currentThread().getContextClassLoader());

        Class<?> appClass = customClassLoader.loadClass(appName);

        Class<?>[] argTypes = new Class[] { String[].class };
        Method mainFunc = appClass.getDeclaredMethod("main", argTypes);

        mainFunc.invoke(null, (Object) Arrays.copyOfRange(args, LAST_OPT_INDEX + 1, args.length));

        mainFunc = null;
        appClass = null;
        customClassLoader = null;

        // cleanup heap after the execution
        System.gc();

        System.err.println("Finished app execution, press Enter to exit....");
        try {
            System.in.read();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}