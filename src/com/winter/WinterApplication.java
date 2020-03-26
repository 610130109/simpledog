package com.winter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.simpledog.boot.SimpleDogApplication;
import com.simpledog.scan.ClassScannerUtils;
import com.winter.annotation.Controller;
import com.winter.annotation.RequestMapping;
import com.winter.cache.SimpleCache;

public class WinterApplication {

	public static void run(Class<?> clazz, String[] args) {

		// load controller
		Set<Class<?>> classSet = ClassScannerUtils.searchClasses(clazz.getPackage().getName());

		Set<Class<?>> subClassSet = loadJarClasses(clazz);

		if (subClassSet != null && !subClassSet.isEmpty()) {
			classSet.addAll(subClassSet);
		}

		Iterator<Class<?>> iterator = classSet.iterator();

		// build path class-method map
		ConcurrentHashMap<String, Method> pathControllerMap = SimpleCache.getInstance().getPathControllerMap();

		System.out.println("path controller map satrt ---------");

		while (iterator.hasNext()) {
			Class<?> clazzItem = iterator.next();
			clazzItem.getInterfaces();
			Controller controller = clazzItem.getAnnotation(Controller.class);
			if (controller == null) {
				continue;
			}
			RequestMapping requestMapping = clazzItem.getAnnotation(RequestMapping.class);
			if (requestMapping == null) {
				continue;
			}
			String classPath = requestMapping.value();
			Method[] methods = clazzItem.getMethods();
			if (methods == null || methods.length <= 0) {
				continue;
			}
			for (Method method : methods) {
				RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
				if (methodRequestMapping == null) {
					continue;
				}
				String methodPath = methodRequestMapping.value();
				String path = classPath + methodPath;
				pathControllerMap.put(path, method);
				System.out.println(path + " " + clazzItem.getName() + "." + method.getName());
			}
		}

		System.out.println("path controller map end ---------");

		// run server
		SimpleDogApplication.run(WinterApplication.class, args);
	}

	protected static Set<Class<?>> loadJarClasses(Class<?> clazz) {
		Set<Class<?>> classSet = new HashSet<>();
		URL rootURL = clazz.getResource("/");
		String rootPath = rootURL.getPath() + "webcodes";
		File file = new File(rootPath);
		if (file.exists()) {
			File[] fs = file.listFiles();
			if (fs != null) {
				for (File f : fs) {
					if (f.isFile() && f.getName().endsWith(".jar")) {
						Set<Class<?>> clsSet = null;
						try {
							clsSet = loadJar(f.getPath());
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (clsSet != null) {
							classSet.addAll(clsSet);
						}
					}
				}
			}
		}
		return classSet;
	}

	protected static Set<Class<?>> loadJar(String jarPath) throws Exception {

		// 反射加载jar文件
		System.out.println(" load jar:" + jarPath);
		File file = new File(jarPath);
		if (file != null) {
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			boolean accessible = method.isAccessible(); // 获取方法的访问权限
			if (accessible == false) {
				method.setAccessible(true);
			}
			URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
			URL url = file.toURI().toURL();
			method.invoke(classLoader, url);
		}

		// 遍历jar文件加载class
		Set<Class<?>> classSet = new HashSet<>();
		@SuppressWarnings("resource")
		JarFile jarFile = new JarFile(file);
		Enumeration<JarEntry> entrys = jarFile.entries();
		while (entrys.hasMoreElements()) {
			JarEntry jarEntry = entrys.nextElement();
			String jarEntryName = jarEntry.getName();
			if (jarEntryName.contains(".class")) {
				String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
				Class<?> cls = Class.forName(className);
				classSet.add(cls);
				System.out.println(" load clss:" + className);
			}
		}
		return classSet;
	}

}
