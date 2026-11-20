plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// jOOQ 코드 생성 전용 소스셋 (src/jooqCodegen). 앱에는 포함되지 않는다.
val jooqCodegen: SourceSet by sourceSets.creating

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jooq-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    "jooqCodegenImplementation"("org.jooq:jooq-codegen")
    "jooqCodegenImplementation"("org.flywaydb:flyway-core")
    "jooqCodegenImplementation"("org.flywaydb:flyway-database-postgresql")
    "jooqCodegenImplementation"("org.testcontainers:testcontainers-postgresql")
    "jooqCodegenRuntimeOnly"("org.postgresql:postgresql")
    "jooqCodegenRuntimeOnly"("org.slf4j:slf4j-simple")
}

// Flyway 마이그레이션 -> 임시 PostgreSQL(Testcontainers) -> jOOQ 코드 생성
// 마이그레이션 파일이 바뀔 때만 다시 실행된다.
val migrationDir = layout.projectDirectory.dir("src/main/resources/db/migration")
val jooqOutputDir = layout.buildDirectory.dir("generated-sources/jooq")

val generateJooq by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Flyway 마이그레이션으로 jOOQ 코드를 생성한다 (Docker 필요)"
    classpath = jooqCodegen.runtimeClasspath
    mainClass = "codegen.JooqCodegen"
    inputs.dir(migrationDir).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.files(jooqCodegen.runtimeClasspath).withNormalizer(ClasspathNormalizer::class)
    outputs.dir(jooqOutputDir)
    outputs.cacheIf { true }
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            migrationDir.asFile.absolutePath,
            jooqOutputDir.get().asFile.absolutePath,
            "com.example.template.common.jooq",
        )
    })
}

sourceSets.main {
    java.srcDir(generateJooq.map { jooqOutputDir })
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        // Docker가 없어 skip된 테스트를 CI 로그에서 바로 보이게
        events("skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) {
                println(
                    "Tests: ${result.testCount}, passed: ${result.successfulTestCount}, " +
                        "failed: ${result.failedTestCount}, skipped: ${result.skippedTestCount}"
                )
            }
        }
    })
}

// openapi/openapi.json(API 계약)을 현재 코드 기준으로 다시 쓴다 (Docker 필요)
val updateOpenApi by tasks.registering(Test::class) {
    group = "documentation"
    description = "openapi/openapi.json 을 현재 API로 갱신한다"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    filter.includeTestsMatching("*OpenApiSpecTests")
    systemProperty("openapi.update", "true")
    outputs.upToDateWhen { false }
}

// Docker 이미지에는 실행 jar(bootJar) 하나만 쓴다
tasks.jar {
    enabled = false
}
