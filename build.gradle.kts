import java.util.Properties

plugins {
    kotlin("jvm") version "1.9.21"
    `java-library`
    jacoco
    `maven-publish`
}

group = "com.smelman"
version = "1.0.0"

repositories {
    mavenCentral()
}

val objectMapperVersion = "2.17.2"
val junitJupiterVersion = "5.9.3"
val slf4jVersion = "2.0.16"
val logbackVersion = "1.5.7"


dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:$objectMapperVersion")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion") // SLF4J binding for Logback
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks.withType<Jar> {
    archiveBaseName.set("rapyd-api-headers") // Change to your library name
    archiveVersion.set("1.0.0") // Change to your version
    from(sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Avoids issues with duplicate files
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport") // Generate Jacoco report after tests
}

// Configure the Jacoco report task if it exists
val jacocoTestReportTask = tasks.findByName("jacocoTestReport")
if (jacocoTestReportTask != null) {
    (jacocoTestReportTask as JacocoReport).apply {
        dependsOn(tasks.test) // Ensure tests are run before generating the report

        reports {
            xml.required.set(true) // Generate XML report
            html.required.set(true) // Generate HTML report
        }

        sourceDirectories.setFrom(files("src/main/kotlin"))
        classDirectories.setFrom(files("build/classes/kotlin/main"))
        executionData.setFrom(files("build/jacoco/test.exec"))
    }
}

kotlin {
    jvmToolchain(17)
}

// JVM Arguments (Optional)
val gradlePropertiesFile = file("gradle.properties")
if (gradlePropertiesFile.exists()) {
    val gradleProperties = Properties().apply {
        load(gradlePropertiesFile.inputStream())
    }
    gradleProperties["org.gradle.jvmargs"] = "-Xmx4g -Xms1g -Xss2m"
    gradleProperties.store(gradlePropertiesFile.outputStream(), null)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            // Define the artifact coordinates
            groupId = "com.smelman"
            artifactId = "rapyd-api-headers"
            version = "1.0.0"

            // POM metadata
            pom {
                name.set("Rapyd API Headers")
                description.set("A library for generating headers for Rapyd API requests")
                url.set("https://github.com/semyonmelman/rapyd-api-headers") // Adjust to your GitHub repository URL

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("@semyonmelman")
                        name.set("Melman Semyon")
                        email.set("shkolar92@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/semyonmelman/rapyd-api-headers.git")
                    developerConnection.set("scm:git:ssh://github.com/semyonmelman/rapyd-api-headers.git")
                    url.set("https://github.com/semyonmelman/rapyd-api-headers")
                }
            }
        }
    }
}

