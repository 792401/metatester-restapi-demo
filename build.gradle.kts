plugins {
    id("java")
}

group = "io.demo.restapi.metatester"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/792401/metatester")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME");
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN");

        }
        mavenCentral()
//    mavenLocal()
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("io.rest-assured:rest-assured:5.3.0")
    implementation("io.rest-assured:json-path:5.3.0")
    implementation("io.metatester:metatester:1.0.0-dev-3cf0526")
}

tasks.test {
    useJUnitPlatform()
    val aspectjAgent = configurations.runtimeClasspath.get().find { it.name.contains("aspectjweaver") }?.absolutePath
    val runWithMetatester = System.getProperty("runWithMetatester") == "true"

    val jvmArguments = mutableListOf(
        "-Xmx2g",
        "-Xms512m"
    )

    if (runWithMetatester && aspectjAgent != null) {
        jvmArguments.add("-javaagent:${aspectjAgent}")
        // jvmArguments.addAll(listOf(
        //     "-Daj.weaving.verbose=true",
        //     "-Dorg.aspectj.weaver.showWeaveInfo=true",
        //     "-Dorg.aspectj.matcher.verbosity=5"
        // ))
    }
    jvmArguments.add("-DrunWithMetatester=${System.getProperty("runWithMetatester")}")

    jvmArgs = jvmArguments
}