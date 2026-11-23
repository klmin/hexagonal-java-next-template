package com.example.template.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

// 기능(feature) 패키지마다 domain <- application <- adapter 방향만 허용
@AnalyzeClasses(packages = "com.example.template", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    private static final String ROOT = "com.example.template";
    private static final Pattern FEATURE = Pattern.compile(Pattern.quote(ROOT) + "\\.([^.]+)\\..*");
    private static final String SPRING_TRANSACTIONAL = "org.springframework.transaction.annotation.Transactional";
    private static final String JAKARTA_TRANSACTIONAL = "jakarta.transaction.Transactional";
    private static final Set<Class<?>> TIME_TYPES = Set.of(
            Instant.class, LocalDate.class, LocalDateTime.class, LocalTime.class,
            OffsetDateTime.class, ZonedDateTime.class);

    @ArchTest
    static final ArchRule domainDependsOnNothingOutside = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..adapter..");

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "jakarta.validation..", "org.jooq..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnAdapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..");

    @ArchTest
    static final ArchRule applicationDoesNotUseWebOrPersistence = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.web..", "org.springframework.data..", "jakarta.persistence..", "org.jooq..");

    // 인증 정보(SecurityContextHolder, Authentication, Jwt 등)는 adapter.in 에서 꺼내 Command 에 사용자 id 로 넘긴다.
    // 서비스와 도메인은 Spring Security 를 모른다 (docs/authentication.md). domain 은 위 domainIsFrameworkFree 가 막는다
    @ArchTest
    static final ArchRule applicationDoesNotUseSecurity = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.security..")
            .because("인증 정보는 adapter.in 에서 꺼내 Command 로 넘긴다 (docs/authentication.md)");

    @ArchTest
    static final ArchRule inboundAdaptersDoNotUseOutbound = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out..");

    @ArchTest
    static final ArchRule outboundAdaptersDoNotUseInbound = noClasses()
            .that().resideInAPackage("..adapter.out..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.in..");

    @ArchTest
    static final ArchRule inboundAdaptersUseOnlyInputPorts = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAnyPackage("..application.service..", "..application.port.out..");

    @ArchTest
    static final ArchRule portsAreInterfaces = classes()
            .that().resideInAnyPackage("..application.port.in..", "..application.port.out..")
            .and().haveSimpleNameNotEndingWith("Command")
            .and().haveSimpleNameNotEndingWith("Query")
            .and().haveSimpleNameNotEndingWith("Result")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule onlyOutboundAdaptersUseJooq = noClasses()
            .that().resideOutsideOfPackages("..adapter.out..", ROOT + ".common.jooq..")
            .should().dependOnClassesThat().resideInAnyPackage("org.jooq..", ROOT + ".common.jooq..");

    @ArchTest
    static final ArchRule commonDoesNotDependOnFeatures = classes()
            .that().resideInAPackage(ROOT + ".common..")
            .should(notDependOnAnyFeature());

    @ArchTest
    static final ArchRule featuresAreFreeOfCycles = slices()
            .matching(ROOT + ".(*)..")
            .should().beFreeOfCycles();

    // 다른 기능은 application.port.in(유스케이스)과 domain만 사용할 수 있다
    @ArchTest
    static final ArchRule featuresTalkThroughInputPorts = classes()
            .that().resideInAPackage(ROOT + "..")
            .should(onlyUseOtherFeaturesThroughInputPorts());

    // JPA 엔티티, 리포지토리, 어댑터는 package-private. 엔티티는 어댑터 밖으로 나가지 않고 도메인 모델로 바뀐다
    @ArchTest
    static final ArchRule outboundAdaptersArePackagePrivate = classes()
            .that().resideInAPackage("..adapter.out..")
            .should().notBePublic()
            .because("엔티티, 리포지토리, 어댑터는 포트 인터페이스로만 노출한다");

    // 트랜잭션 경계는 유스케이스 구현(application.service)에만 둔다
    @ArchTest
    static final ArchRule transactionalClassesOnlyInServices = noClasses()
            .that().resideOutsideOfPackage("..application.service..")
            .should().beAnnotatedWith(SPRING_TRANSACTIONAL)
            .orShould().beAnnotatedWith(JAKARTA_TRANSACTIONAL)
            .because("트랜잭션 경계는 application.service 에 둔다");

    @ArchTest
    static final ArchRule transactionalMethodsOnlyInServices = noMethods()
            .that().areDeclaredInClassesThat().resideOutsideOfPackage("..application.service..")
            .should().beAnnotatedWith(SPRING_TRANSACTIONAL)
            .orShould().beAnnotatedWith(JAKARTA_TRANSACTIONAL)
            .because("트랜잭션 경계는 application.service 에 둔다");

    // 현재 시각은 Clock Bean 으로 읽는다. Instant.now(clock) 처럼 Clock 을 넘기는 호출은 허용
    @ArchTest
    static final ArchRule noSystemTimeNow = noClasses()
            .should().callMethodWhere(systemTimeNow())
            .because("현재 시각은 주입받은 Clock 으로 읽어야 테스트에서 고정할 수 있다");

    // 시스템 Clock 은 common.config 의 Clock Bean 에서만 만든다
    @ArchTest
    static final ArchRule systemClockOnlyInConfig = noClasses()
            .that().resideOutsideOfPackage(ROOT + ".common.config..")
            .should().callMethodWhere(systemClockFactory())
            .because("Clock 은 Bean 으로 주입받는다");

    private static DescribedPredicate<JavaMethodCall> systemTimeNow() {
        return DescribedPredicate.describe("now() without Clock on java.time types", call ->
                call.getName().equals("now")
                        && TIME_TYPES.stream().anyMatch(t -> call.getTargetOwner().isEquivalentTo(t))
                        && !call.getTarget().getRawParameterTypes().stream()
                                .map(JavaClass::getName).toList()
                                .equals(List.of(Clock.class.getName())));
    }

    private static DescribedPredicate<JavaMethodCall> systemClockFactory() {
        return DescribedPredicate.describe("Clock.system*()", call ->
                call.getTargetOwner().isEquivalentTo(Clock.class) && call.getName().startsWith("system"));
    }

    private static Optional<String> featureOf(JavaClass javaClass) {
        Matcher m = FEATURE.matcher(javaClass.getName());
        if (!m.matches() || m.group(1).equals("common")) {
            return Optional.empty();
        }
        return Optional.of(m.group(1));
    }

    private static ArchCondition<JavaClass> notDependOnAnyFeature() {
        return new ArchCondition<>("not depend on any feature package") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                for (Dependency d : item.getDirectDependenciesFromSelf()) {
                    if (featureOf(d.getTargetClass()).isPresent()) {
                        events.add(SimpleConditionEvent.violated(d, d.getDescription()));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> onlyUseOtherFeaturesThroughInputPorts() {
        return new ArchCondition<>("only use other features through application.port.in or domain") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                Optional<String> from = featureOf(item);
                if (from.isEmpty()) {
                    return;
                }
                for (Dependency d : item.getDirectDependenciesFromSelf()) {
                    Optional<String> to = featureOf(d.getTargetClass());
                    if (to.isEmpty() || to.equals(from)) {
                        continue;
                    }
                    String pkg = d.getTargetClass().getPackageName();
                    boolean allowed = pkg.contains(".application.port.in") || pkg.contains(".domain");
                    if (!allowed) {
                        events.add(SimpleConditionEvent.violated(d, d.getDescription()));
                    }
                }
            }
        };
    }
}
