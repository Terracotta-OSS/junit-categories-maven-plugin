package org.terracotta.maven.plugins.list.junit.categories;

import junit.framework.TestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author Anthony Dahanne
 */
@Mojo(name = "list", requiresDependencyResolution = ResolutionScope.TEST)
public class ListJunitCategoriesMojo extends AbstractMojo {

  /**
   * The directory containing generated test classes of the project being tested. This will be included at the
   * beginning of the test classpath. *
   */
  @Parameter( defaultValue = "${project.build.testOutputDirectory}" )
  protected File testClassesDirectory;



  @Component
  private MavenProject rootProject;

  private ClassLoader classLoaderWithCompiledClasspath;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      if(rootProject.getModel().getPackaging().equals("pom") || !testClassesDirectory.exists()) {
        getLog().warn("Project does not have test classes, if it is supposed to, please run mvn test-compile first.");
      } else {
        Thread.currentThread().setContextClassLoader(getClassLoaderWithCompiledClasspath());
        formatAndPrint(listCategories(testClassesDirectory));
      }
    } catch (Exception e) {
      throw new MojoFailureException("Impossible to list the junit categories",e);
    }
  }

  private ClassLoader getClassLoaderWithCompiledClasspath() throws DependencyResolutionRequiredException, MalformedURLException {
    if (classLoaderWithCompiledClasspath != null) {
      return classLoaderWithCompiledClasspath;
    }
    if (rootProject != null) {
      //requiresDependencyResolution = ResolutionScope.TEST is making this next call return every dep.
      List<String> compileClasspathElements = rootProject.getTestClasspathElements();
      List<URL> urls = new ArrayList<URL>();
      for (String path : compileClasspathElements) {
        urls.add(new File(path).toURI().toURL());
      }
      classLoaderWithCompiledClasspath = new URLClassLoader(urls.toArray(new URL[]{}), getClass().getClassLoader());
    } else {
      // no maven project ? we rely on the default class loader
      classLoaderWithCompiledClasspath = getClass().getClassLoader();
    }
    return classLoaderWithCompiledClasspath;
  }

  private void formatAndPrint(Map<Class,Map<Class,List<Method>>> categoriesMap) {
    
    getLog().info("******************************************");
    getLog().info("* JUnit Categories found in test classes *");
    getLog().info("******************************************");

    if(categoriesMap.isEmpty()) {
      getLog().info("NO CATEGORIZED TESTS COULD BE FOUND !");
      getLog().info("Make sure your tests are compiled (present in target/), in doubt run mvn test-compile !");
      getLog().info("Make sure you use the Junit annotation @org.junit.experimental.categories.Category in your tests !");
    } else {
      for (Map.Entry<Class, Map<Class, List<Method>>> categoriesMapEntry : categoriesMap.entrySet()) {
        getLog().info("------------------------------------------");
        getLog().info("| Category : " + categoriesMapEntry.getKey().getCanonicalName() + " |");
        getLog().info("------------------------------------------");

        for (Map.Entry<Class,  List<Method>> classesMapEntry: categoriesMapEntry.getValue().entrySet()) {
          for (Method method : classesMapEntry.getValue()) {
            StringBuilder sb = new StringBuilder();
            sb.append(classesMapEntry.getKey().getCanonicalName())
              .append(".")
              .append(method.getName());
            getLog().info(sb.toString());
          }
        }
      }
    }
  }

  /**
   *
   * Given a directory, returns the map indexed by all found categories containing classes for each category
   * For each class, a list of methods falling under the category is also returned
   *
   * @param directory
   * @return map indexed with category classes, and for each category the list of elements (classes,
   * @throws ClassNotFoundException
   */
  public Map<Class,Map<Class,List<Method>>> listCategories(File directory) throws ClassNotFoundException, MalformedURLException, DependencyResolutionRequiredException {
    Map<Class,Map<Class,List<Method>>> categoriesMap = new TreeMap<Class, Map<Class, List<Method>>>(getComparator());
    List<Class> testClasses = findClasses(directory, "");
    for (Class testClass : testClasses) {
      Category categoryAnnotationOnClass = (Category) testClass.getAnnotation(Category.class);
      List<Class> categoriesOnClass =  new ArrayList<Class>();
      if(categoryAnnotationOnClass != null) {
        for (Class value : categoryAnnotationOnClass.value()) {
          if(!categoriesMap.containsKey(value)) {
            Map<Class, List<Method>> categorizedClassesMap =  new TreeMap<Class, List<Method>>(getComparator());
            categoriesMap.put(value,categorizedClassesMap);
          }
          categoriesOnClass.add(value);
        }
      }
      categoriesOnClass.addAll(findSuperclassCategories(testClass));
      Method[] methods = testClass.getDeclaredMethods();
      boolean classDoesExtendTestCase = doesExtendTestCase(testClass);
      for (Method method : methods) {
        // only test methods are considered !
        Test testAnnotationOnMethod = method.getAnnotation(Test.class);
        if (testAnnotationOnMethod != null || (classDoesExtendTestCase &&  method.getName().startsWith("test"))) {
          Category categoryAnnotationOnMethod = method.getAnnotation(Category.class);
          if (categoryAnnotationOnMethod !=null) {
            List<Class> categoriesOnMethod =  new ArrayList<Class>();
            for (Class value : categoryAnnotationOnMethod.value()) {
              if(!categoriesMap.containsKey(value)) {
                Map<Class, List<Method>> categorizedClassesMap =  new TreeMap<Class, List<Method>>(getComparator());;
                categoriesMap.put(value,categorizedClassesMap);
              }
              categoriesOnMethod.add(value);
            }
            if(!categoriesOnMethod.isEmpty()) {
              for (Class aClass : categoriesOnMethod) {
                createListOfMethodsIfNotExisting(categoriesMap, testClass, aClass);
                categoriesMap.get(aClass).get(testClass).add(method);
              }
            }
          } else if(!categoriesOnClass.isEmpty()) {
            for (Class aClass : categoriesOnClass) {
              createListOfMethodsIfNotExisting(categoriesMap, testClass, aClass);
              categoriesMap.get(aClass).get(testClass).add(method);
            }
          }
        }
      }
    }
    return categoriesMap;
  }

  boolean doesExtendTestCase(Class testClass) {
    Class superclass = testClass.getSuperclass();
    if(superclass == null || superclass.equals(Object.class)) {
      return false;
    } else if(superclass.equals(TestCase.class)) {
      return true;
    }
    else {
      return doesExtendTestCase(superclass);
    }
  }

  private void createListOfMethodsIfNotExisting(Map<Class, Map<Class, List<Method>>> categoriesMap, Class testClass, Class aClass) {
    if(categoriesMap.get(aClass).get(testClass) == null) {
      List<Method> listOfMethods = new ArrayList<Method>();
      categoriesMap.get(aClass).put(testClass,listOfMethods);
    }
  }

  private Comparator<Class> getComparator() {
    return new Comparator<Class>() {
      @Override
      public int compare(Class o1, Class o2) {
        return o1.getCanonicalName().compareTo(o2.getCanonicalName());
      }
    };
  }


  /**
   * copied from org.apache.maven.surefire.common.junit48.FilterFactory
   * @param clazz
   * @return
   */
  private Collection<Class<?>> findSuperclassCategories( Class<?> clazz )
  {
    if ( clazz != null && clazz.getSuperclass() != null )
    {
      Category cat = clazz.getSuperclass().getAnnotation( Category.class );
      if ( cat != null )
      {
        return new HashSet<Class<?>>( Arrays.asList( cat.value() ) );
      }
      else
      {
        return findSuperclassCategories( clazz.getSuperclass() );
      }
    }

    return Collections.emptySet();
  }

  /**
   * Recursive method used to find all classes in a given directory and subdirs.
   *
   * @param directory   The base directory
   * @param packageName The current packageName considered in the search, use "" to scan all packages
   * @return The classes
   * @throws ClassNotFoundException
   */
  public List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException, MalformedURLException, DependencyResolutionRequiredException {
    List<Class> classes = new ArrayList<Class>();
    if (!directory.exists()) {
      return classes;
    }
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        classes.addAll(findClasses(file, ("".equals(packageName) ? packageName : packageName + ".") + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        ClassLoader classLoader = getClassLoaderWithCompiledClasspath();
        String className = ("".equals(packageName) ? packageName : packageName + ".") + file.getName().substring(0, file.getName().length() - 6);
        classes.add(Class.forName(className, false, classLoader));
      }
    }
    return classes;
  }


}
