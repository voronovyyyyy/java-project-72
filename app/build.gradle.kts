plugins {
    application
    jacoco
    id("com.diffplug.spotless") version "8.10.4"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

application {
    mainClass.set("hexlet.code.App")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

dependencies {
    implementation(platform("io.javalin:javalin-bom:7.2.3"))
    implementation("io.javalin:javalin")
    implementation("io.javalin:javalin-rendering-jte")
    implementation("gg.jte:jte:3.2.4")
    implementation("org.slf4j:slf4j-simple:2.0.20")
    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("com.h2database:h2:2.5.252")
    implementation("org.postgresql:postgresql:42.7.14")
    implementation("com.konghq:unirest-java-core:4.10.2")
    implementation("org.jsoup:jsoup:1.23.2")

    testImplementation("io.javalin:javalin-testtools")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.test {
    useJUnitPlatform()
    environment("APP_ENV", "test")
    environment("JDBC_DATABASE_URL", "jdbc:h2:mem:project_test;DB_CLOSE_DELAY=-1")
    systemProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn")
    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

jacoco {
    toolVersion = "0.8.15"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.shadowJar {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    filesMatching(listOf("META-INF/services/**", "META-INF/*.kotlin_module")) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    mergeServiceFiles()
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat().aosp()
        formatAnnotations()
        endWithNewline()
    }
}
