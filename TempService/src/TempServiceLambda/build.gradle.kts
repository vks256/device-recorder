import org.gradle.api.tasks.testing.logging.TestLogEvent

// Applies BrazilGradle plugin then uses it to setup the build script classpath.
buildscript {
    apply(plugin = "brazil-gradle")
    val brazilGradle = the<com.amazon.brazil.gradle.BrazilPluginExtension>()
    dependencies {
        brazilGradle.tool("BrazilGradleQualityDefaults").forEach { classpath(it) }
    }
}

/*
 Applies core Gradle plugins, which are ones built into Gradle itself.
*/
plugins {
    // Java for compile and unit test of Java source files. Read more at:
    // https://docs.gradle.org/current/userguide/java_plugin.html
    java

    // JaCoCo for coverage metrics and reports of Java source files. Read more at:
    // https://docs.gradle.org/current/userguide/jacoco_plugin.html
    jacoco

    // Checkstyle for style checks and reports on Java source files. Read more at:
    // https://docs.gradle.org/current/userguide/checkstyle_plugin.html
    checkstyle
}

/*
 Applies community Gradle plugins, usually added as build-tools in Config.
*/

// BrazilGradleQualityDefaults for enabling brazil specific quality rules and
// reports for other quality plugins. Read more at:
// https://w.amazon.com/bin/view/BrazilGradle/QualityDefaults
apply(plugin = "brazil-quality-defaults")

// SpotBugs for quality checks and reports of Java source files. Read more at:
// https://spotbugs.readthedocs.io/en/stable/gradle.html
apply(plugin = "com.github.spotbugs")

/*
 Configures the Checkstyle "checkstyle" plugin. Remove this and the plugin if
 you want to skip these checks and report generation.
*/
checkstyle {
    sourceSets = listOf(the<SourceSetContainer>()["main"])
    setIgnoreFailures(false)
}

/*
 Configures the SpotBugs "com.github.spotbugs" plugin. Remove this and the
 plugin to skip these checks and report generation.
*/
configure<com.github.spotbugs.snom.SpotBugsExtension> {
    ignoreFailures.set(false)
}

/*
 Resolve build, test, tool, and runtime dependencies using BrazilGradle.
*/
dependencies {
    brazilGradle.run().forEach { runtimeOnly(it) }
    brazilGradle.build().forEach { implementation(it) }
    brazilGradle.testbuild().forEach { testImplementation(it) }
}

tasks {
    /*
     Configures the JaCoCo "jacoco" plugin. Remove this if you want to skip
     these checks and report generation.

     Set minimum code coverage to fail build, where 0.01 = 1%.
    */
    jacocoTestCoverageVerification {
        violationRules {
            rule { limit { minimum = BigDecimal.valueOf(0.75) } }
        }
    }

    /*
      The SpotBugs Gradle Plugin generates a task for each sourceSet generated by
      Gradle Java Plugin. For instance, if you have two sourceSets main and test,
      this plugin will generates two tasks: spotbugsMain and spotbugsTest.
      Uncomment below if you want to skip checks for test code.

    named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsTest") {
        setIgnoreFailures(true)
    }
    */

    check {
        dependsOn(jacocoTestCoverageVerification)
    }

    task<Copy>("copyConfiguration") {
        from("${project.projectDir}/configuration")
        into(brazilGradle.buildDir)
    }

    build {
        dependsOn("copyConfiguration")
    }

    withType<Test> {
        /*
         Specifies that JUnit Platform (a.k.a. JUnit 5) should be used to execute tests.

         For mixed JUnit 4 and 5 tests, add 'JUnit-4-12-migration = 5.x;' to
         test-dependencies in Config.
        */
        useJUnitPlatform()

        /*
         Print the tests to STDOUT to verify that they actually run
        */
        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        }
    }
}