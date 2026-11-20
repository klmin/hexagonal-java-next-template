package com.example.template.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

// 기능(feature) 패키지마다 domain <- application <- adapter 방향만 허용
@AnalyzeClasses(packages = "com.example.template", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    private static final String ROOT = "com.example.template";
    private static final Pattern FEATURE = Pattern.compile(Pattern.quote(ROOT) + "\\.([^.]+)\\..*");

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
            .should().beInterfaces();

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
