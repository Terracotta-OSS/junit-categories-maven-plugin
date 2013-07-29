package org.terracotta.maven.plugins.list.junit.categories;

import categorized.tests.OldSchoolTestClass;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author: Anthony Dahanne
 */
public class ListJunitCategoriesMojoTest {

  private File resourcesDirectory;

  @Before
  public void setUp() throws Exception {
    resourcesDirectory = new File(ListJunitCategoriesMojoTest.class.getClassLoader().getResource("./").getFile());
  }

  @Test
  public void scanTest() throws ClassNotFoundException, MalformedURLException, DependencyResolutionRequiredException {
    ListJunitCategoriesMojo listJunitCategoriesMojo = new ListJunitCategoriesMojo();
    List<Class> classes = listJunitCategoriesMojo.findClasses(resourcesDirectory, "");
    assertEquals(9,classes.size());

    assertTrue(classes.contains(Class.forName("categories.FastTests")));
    assertTrue(classes.contains(Class.forName("categories.SlowTests")));
    assertTrue(classes.contains(Class.forName("categorized.tests.AnnotatedClass")));
    assertTrue(classes.contains(Class.forName("categorized.tests.InheritedAnnotatedClass")));
    assertTrue(classes.contains(Class.forName("categorized.tests.NotAnnotatedClass")));
    assertTrue(classes.contains(Class.forName("categorized.tests.OldSchoolTestClass")));
    assertTrue(classes.contains(Class.forName("categorized.tests.AbstractOldSchoolTestClass")));
    assertTrue(classes.contains(Class.forName("categorized.tests.NotATestClass")));
    assertTrue(classes.contains(Class.forName("org.terracotta.maven.plugins.list.junit.categories.ListJunitCategoriesMojoTest")));

  }

  @Test
  public void listCategoriesTest() throws ClassNotFoundException, MalformedURLException, DependencyResolutionRequiredException {
    ListJunitCategoriesMojo listJunitCategoriesMojo = new ListJunitCategoriesMojo();
    Map<Class,Map<Class,List<Method>>> categoriesMap = listJunitCategoriesMojo.listCategories(resourcesDirectory);
    //2 categories : SlowTests and FastTests
    assertEquals(2,categoriesMap.keySet().size());
    assertFalse(categoriesMap.get(Class.forName("categories.FastTests")).isEmpty());
    assertFalse(categoriesMap.get(Class.forName("categories.SlowTests")).isEmpty());

    //Fast tests
    assertEquals(2,categoriesMap.get(Class.forName("categories.FastTests")).get(Class.forName("categorized.tests.AnnotatedClass")).size());
    assertEquals(2,categoriesMap.get(Class.forName("categories.FastTests")).get(Class.forName("categorized.tests.OldSchoolTestClass")).size());
    assertEquals(1,categoriesMap.get(Class.forName("categories.FastTests")).get(Class.forName("categorized.tests.NotAnnotatedClass")).size());


    //Slow tests
    assertEquals(1,categoriesMap.get(Class.forName("categories.SlowTests")).get(Class.forName("categorized.tests.AnnotatedClass")).size());
    assertEquals(1,categoriesMap.get(Class.forName("categories.SlowTests")).get(Class.forName("categorized.tests.OldSchoolTestClass")).size());
    assertEquals(1,categoriesMap.get(Class.forName("categories.SlowTests")).get(Class.forName("categorized.tests.InheritedAnnotatedClass")).size());
    assertEquals(1,categoriesMap.get(Class.forName("categories.SlowTests")).get(Class.forName("categorized.tests.NotAnnotatedClass")).size());

    //this is not a test class :
    assertNull(categoriesMap.get(Class.forName("categories.FastTests")).get(Class.forName("categorized.tests.NotATestClass")));
    assertNull(categoriesMap.get(Class.forName("categories.SlowTests")).get(Class.forName("categorized.tests.NotATestClass")));
  }

  @Test
  public void doesExtendTestCaseTest() {
    ListJunitCategoriesMojo listJunitCategoriesMojo =  new ListJunitCategoriesMojo();
    assertTrue(listJunitCategoriesMojo.doesExtendTestCase(OldSchoolTestClass.class));
    assertFalse(listJunitCategoriesMojo.doesExtendTestCase(ListJunitCategoriesMojo.class));
  }

}
