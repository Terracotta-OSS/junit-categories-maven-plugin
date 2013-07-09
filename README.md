# Junit Categories Maven Plugin
This maven plugin allows the user to list the different [JUnit categories](https://github.com/junit-team/junit/wiki/Categories) used in the tests classes and methods

## Example :

Go to your maven project, and launch this command :

    mvn clean test-compile org.terracotta.maven.plugins:junit-categories-maven-plugin:list

The output (on the console) should, if you have tests using [JUnit categories](https://github.com/junit-team/junit/wiki/Categories), look like :

```bash
[INFO] --- junit-categories-maven-plugin:1.0.0-SNAPSHOT:list (default-cli) @ my-sample-project ---
[INFO] ******************************************
[INFO] * JUnit Categories found in test classes *
[INFO] ******************************************
[INFO] ------------------------------------------
[INFO] | Category : org.terracotta.test.categories.FastTests |
[INFO] ------------------------------------------
[INFO] org.terracotta.my.sample.project.OperationsTest.multiplyTest__fast
[INFO] ------------------------------------------
[INFO] | Category : org.terracotta.test.categories.SlowTests |
[INFO] ------------------------------------------
[INFO] org.terracotta.my.sample.project.OperationsTest.multiplyTest__slow
```

In this case, the plugin detected that OperationsTest.multiplyTest_fast is annotated with @Category(FastTests.class) and that OperationsTest.multiplyTest_slow is annotated with @Category(SlowTests.class)

```java
  @Test
  @Category(SlowTests.class)
  public void multiplyTest__slow () {
  }
  @Test
  @Category(FastTests.class)
  public void multiplyTest__fast () {
  }
```


## How to build :
Simply clone this repo and run mvn clean install

## Is it in Maven central ? :
No, you will have to add Terracotta maven repositories to your pom.xml :

```xml
  <pluginRepositories>
    <pluginRepository>
      <id>terracotta-snapshots</id>
      <url>http://www.terracotta.org/download/reflector/snapshots</url>
    </pluginRepository>
    <pluginRepository>
      <id>terracotta-releases</id>
      <url>http://www.terracotta.org/download/reflector/releases</url>
    </pluginRepository>
  </pluginRepositories>
```


## How to use :
The plugin is org.terracotta.maven.plugins:junit-categories-maven-plugin and it has one goals: list.
See the previous example.

A great companion to this tool is Maven surefire plugin, in particular its [JUnit Categories support](http://maven.apache.org/surefire/maven-surefire-plugin/examples/junit.html), <groups> and <excludedGroups>[see the options for more details](http://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html)


## How it works :

The plugin's list mojo will
* add the test classes and their dependencies to the plugin classpath (this is why you need to compile - not run- your tests first)
* scan each test class found in ${project.build.testOutputDirectory} and look for @Test and @Category annotations on classes and test methods.
* all the info is stored in a map, that will be displayed on the console (info level)


## Authors :
This plugin was developed at [Terracotta](http://www.terracotta.org), by

- [Anthony Dahanne](https://github.com/anthonydahanne/)