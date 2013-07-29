package categorized.tests;

import categories.FastTests;
import categories.SlowTests;
import org.junit.experimental.categories.Category;

/**
 * @author: Anthony Dahanne
 */
@Category(SlowTests.class)
public class OldSchoolTestClass extends AbstractOldSchoolTestClass{

  public void testSlow() {
  }

  @Category(FastTests.class)
  public void testFast() {
  }

  @Category(FastTests.class)
  public void testFast2() {
  }

  @Category(FastTests.class)
  public void fast3__not() {
  }

}
