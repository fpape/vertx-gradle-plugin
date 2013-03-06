import org.gradle.api.*
import org.gradle.testfixtures.*;

import org.junit.*
import static org.junit.Assert.*

import org.vertx.gradle.*

class VertxPluginTest {
  def builder

  def root, runnable, nonrunnable

  @Before
  public void before(){
    this.builder = ProjectBuilder.builder()

    File projectDir = new File('src/test/resources/rootproject')
    root = builder.withProjectDir(projectDir).withName('root').build()

    projectDir = new File('src/test/resources/rootproject/runnable')
    runnable = builder.withProjectDir(projectDir).withParent(root).withName('runnable').build()

    projectDir = new File('src/test/resources/rootproject/nonrunnable')
    nonrunnable = builder.withProjectDir(projectDir).withParent(root).withName('nonrunnable').build()

    loadProperties(root)
    assertNotNull('Gradle Properties not loaded', root.vertxVersion)
    assertNotNull('Gradle Properties not loaded', runnable.vertxVersion)
    assertNotNull('Gradle Properties not loaded', nonrunnable.vertxVersion)

    root.delete 'mods'

    root.apply plugin: VertxPlugin
  }

  @Test
  public void testVertxPluginApplied() {
    assertTrue('VertxPlugin not applied', root.vertx)
  }

  @Test
  public void testModulePluginRunnable() {
    assertTrue('VertxModulePlugin not applied', runnable.vertx)

    assertEquals('VertxModulePlugin did not set props main properly', runnable.config.main, 'app.js')
    assertTrue('Module should be runnable', runnable.runnable)

    assertNotNull('Run Task was not created', runnable.tasks.getByPath('run-module1'))
  }

  @Test
  public void testModulePluginNonRunnable() {
    assertTrue('VertxModulePlugin not applied', nonrunnable.vertx)

    assertNull('VertxModulePlugin did not set props main properly', nonrunnable.config.main)
    assertFalse('Module should not be runnable', nonrunnable.runnable)

    try {
      assertNull(nonrunnable.tasks.getByPath('run-module2'))
      fail('Run Task was created!')
    } catch(UnknownTaskException e) {
    }
  }


  @Test
  public void testModuleCopy() {
    runnable.tasks.copyMod.execute()

    assertTrue('mods directory not created', root.file('mods').isDirectory())
    assertTrue('module directory not copied into mods directory', root.file("mods/${runnable.moduleName}").isDirectory())
  }

  @Test
  public void testBuildGradleApplied() {
    assertTrue('module.gradle was not applied to runnable task', runnable.applied)
    assertTrue('module.gradle was not applied to nonrunnable task', nonrunnable.applied)
  }

  def loadProperties(Project project){
    project.file('gradle.properties').withReader { def reader ->
      def props = new Properties()
      props.load(reader)

      props.each { k,v ->
        project.ext[k] = v
      }
    }
  }
}