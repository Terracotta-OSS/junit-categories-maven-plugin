package categorized.tests;

import categories.FastTests;
import categories.SlowTests;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.List;

/**
 * @author: Anthony Dahanne
 */
@Category(SlowTests.class)
public class AnnotatedClass {

  @Test
  public void testSlow() {
  }

  @Test
  @Category(FastTests.class)
  public void testFast() {
  }

  @Test
  @Category(FastTests.class)
  public void testFast2() {
  }


}
