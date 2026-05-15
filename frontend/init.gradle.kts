// SSL bypass for Gradle
// Apply with: ./gradlew <task> --init-script init.gradle.kts

allprojects {
    repositories {
        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
            isAllowInsecureProtocol = true
        }
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
            isAllowInsecureProtocol = true
        }
        google()
    }
}
