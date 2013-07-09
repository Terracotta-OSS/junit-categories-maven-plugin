package categorized.tests;

import categories.FastTests;
import categories.SlowTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author: Anthony Dahanne
 */
public class NotAnnotatedClass {

  @Test
  @Category(SlowTests.class)
  public void testSlow() {
  }

  @Test
  @Category(FastTests.class)
  public void testFast() {
  }



}
