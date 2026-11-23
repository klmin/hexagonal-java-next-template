package codegen;

import java.nio.file.Path;
import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

// ./gradlew generateJooq 에서 실행됨. args: <migration dir> <output dir> <package>
public final class JooqCodegen {

    private static final String POSTGRES_IMAGE = "postgres:18-alpine";

    public static void main(String[] args) throws Exception {
        Path migrations = Path.of(args[0]);
        Path output = Path.of(args[1]);
        String packageName = args[2];

        if (!DockerClientFactory.instance().isDockerAvailable()) {
            System.err.println("""

                    [generateJooq] Docker에 연결할 수 없습니다.
                    jOOQ 코드는 Flyway 마이그레이션을 임시 PostgreSQL 컨테이너에 적용해서 생성합니다.
                    Docker를 실행한 뒤 다시 빌드하세요.
                    """);
            System.exit(1);
        }

        try (PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))) {
            postgres.start();

            Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations("filesystem:" + migrations.toAbsolutePath())
                    .load()
                    .migrate();

            GenerationTool.generate(new Configuration()
                    .withJdbc(new Jdbc()
                            .withDriver("org.postgresql.Driver")
                            .withUrl(postgres.getJdbcUrl())
                            .withUser(postgres.getUsername())
                            .withPassword(postgres.getPassword()))
                    .withGenerator(new Generator()
                            .withDatabase(new Database()
                                    .withName("org.jooq.meta.postgres.PostgresDatabase")
                                    .withInputSchema("public")
                                    .withExcludes("flyway_schema_history"))
                            .withGenerate(new Generate()
                                    .withRecords(true)
                                    .withPojos(false)
                                    .withDaos(false)
                                    .withJavaTimeTypes(true))
                            .withTarget(new Target()
                                    .withPackageName(packageName)
                                    .withDirectory(output.toString())
                                    .withClean(true))));
        }
    }
}
