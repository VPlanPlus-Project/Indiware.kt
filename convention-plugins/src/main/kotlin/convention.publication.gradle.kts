import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import java.util.*
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

plugins {
    `maven-publish`
}
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun RepositoryHandler.projectRepository() {
    maven {
        name = "releases"
        url = uri("https://gitlab.jvbabi.es/api/v4/projects/14/packages/maven")
        credentials {
            username = localProperties.getProperty("PROJECT_MAVEN_USERNAME") ?: System.getenv("PROJECT_MAVEN_USERNAME")
            password = localProperties.getProperty("PROJECT_MAVEN_PASSWORD") ?: System.getenv("PROJECT_MAVEN_PASSWORD")
        }
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
}

publishing {
    repositories {
        projectRepository()
    }

    publications.withType<MavenPublication> {

        artifact(javadocJar.get())

        pom {
            name.set("Indiware.kt")
            description.set("A small KMP-compatible library to fetch and transform data from stundenplan24.de to a more friendly format. ")
            url.set("https://github.com/VPlanPlus-Project/Indiware.kt")

            developers {
                developer {
                    id.set("Julius-Babies")
                    name.set("Julius Babies")
                    email.set("julius@familie-babies.de")
                }
            }
            scm {
                url.set("https://github.com/VPlanPlus-Project/Indiware.kt")
            }
        }
    }
}