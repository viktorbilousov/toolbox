/**
 * Mini-plugin to configure Nexus publications for all subprojects, where this is necessary.
 * 
 * !!! Apply this plugin after setting the `version` variable in the build script !!!
 */

plugins {
    `maven-publish`
    `java-library`
}

publishing {

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "SystemaNexus"

            var publishUrl = project.properties["nexus_repository_release_url"] as String?

            if ((version as String).contains("SNAPSHOT")) {
                publishUrl = project.properties["nexus_repository_snapshot_url"] as String?
            }

            url = uri(publishUrl!!)

            credentials {
                username = project.properties["nexus_username"] as String?
                password = project.properties["nexus_password"] as String?
            }
        }
    }

//    tasks.withType<PublishToMavenRepository> {
//        dependsOn(tasks.test)
//    }
}