package com.isuwang.dapeng.bootstrap;

import com.isuwang.dapeng.bootstrap.classloader.AppClassLoader;
import com.isuwang.dapeng.bootstrap.classloader.ClassLoaderManager;
import com.isuwang.dapeng.bootstrap.classloader.PlatformClassLoader;
import com.isuwang.dapeng.bootstrap.classloader.ShareClassLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap
 *
 * @author craneding
 * @date 16/1/28
 */
public class Bootstrap {

    private static final List<URL> shareURLs = new ArrayList<>();
    private static final List<URL> platformURLs = new ArrayList<>();
    public static final List<List<URL>> appURLs = new ArrayList<>();
    public static final String enginePath = System.getProperty("soa.base", new File(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent());
    private static final String soaRunMode = System.getProperty("soa.run.mode", "maven");

    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.setProperty("soa.run.mode", soaRunMode);
        System.setProperty("soa.base", enginePath);

        System.out.println("soa.base:" + enginePath);
        System.out.println("soa.run.mode:" + soaRunMode);

        final boolean isRunInMaven = soaRunMode.equals("maven");

        if (!isRunInMaven) {
            loadAllUrls();

            loadAllClassLoader();
        }

        setAppClassLoader();

//        if (!isRunInMaven) {

        new Thread(() -> {
            try {
                System.out.println("开启ServerSocket");
                final ServerSocket server = new ServerSocket(9091);
                do {
                    final Socket socket = server.accept();
                    socket.setKeepAlive(true);
                    Thread thread = new Thread(new DynamicThreadHandler(socket));
                    thread.start();
                } while (true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
//        }

        startup();
    }

    private static void setAppClassLoader() {
        try {
            Class<?> springContainerClass = ClassLoaderManager.platformClassLoader.loadClass("com.isuwang.dapeng.container.spring.SpringContainer");
            Field appClassLoaderField = springContainerClass.getField("appClassLoaders");
            appClassLoaderField.set(springContainerClass, ClassLoaderManager.appClassLoaders);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void startup() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Thread.currentThread().setContextClassLoader(ClassLoaderManager.platformClassLoader);
        Class<?> mainClass = ClassLoaderManager.platformClassLoader.loadClass("com.isuwang.dapeng.container.ContainerStartup");
        Method mainMethod = mainClass.getMethod("startup");
        mainMethod.invoke(mainClass);
    }

    private static void loadAllClassLoader() {
        ClassLoaderManager.shareClassLoader = new ShareClassLoader(shareURLs.toArray(new URL[shareURLs.size()]));

        ClassLoaderManager.platformClassLoader = new PlatformClassLoader(platformURLs.toArray(new URL[platformURLs.size()]));

        for (List<URL> appURL : appURLs) {
            AppClassLoader appClassLoader = new AppClassLoader(appURL.toArray(new URL[appURL.size()]));

            ClassLoaderManager.appClassLoaders.add(appClassLoader);
        }
    }

    private static void loadAllUrls() throws MalformedURLException {
        shareURLs.addAll(findJarURLs(new File(enginePath, "lib")));

        platformURLs.addAll(findJarURLs(new File(enginePath, "bin/lib")));

        final File appsPath = new File(enginePath, "apps");
        loadAppsUrls(appsPath);
    }

    /**
     * 给定apps目录，加载目录下所有URL
     *
     * @param appsPath
     * @throws MalformedURLException
     */
    public static void loadAppsUrls(File appsPath) throws MalformedURLException {

        if (appsPath.exists() && appsPath.isDirectory()) {
            final File[] files = appsPath.listFiles();

            for (File file : files) {
                final List<URL> urlList = new ArrayList<>();

                if (file.isDirectory()) {
                    urlList.addAll(findJarURLs(file));
                } else if (file.isFile() && file.getName().endsWith(".jar")) {
                    urlList.add(file.toURI().toURL());
                }
                if (!urlList.isEmpty())
                    appURLs.add(urlList);
            }
        }


    }

    private static List<URL> findJarURLs(File file) throws MalformedURLException {
        final List<URL> urlList = new ArrayList<>();

        if (file != null && file.exists()) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                urlList.add(file.toURI().toURL());
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        urlList.addAll(findJarURLs(files[i]));
                    }
                }
            }
        }

        return urlList;
    }
}
