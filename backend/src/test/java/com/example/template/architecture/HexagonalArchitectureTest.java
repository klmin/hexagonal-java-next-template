package com.example.template.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * 헥사고날 아키텍처의 의존 방향을 강제한다.
 * <pre>
 * adapter.in  ─┐
 *              ├─▶ application ─▶ domain
 * adapter.out ─┘
 * </pre>
 */
@AnalyzeClasses(packages = "com.example.template", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    private static final String ROOT = "com.example.template";

    @ArchTest
    static final ArchRule layerDependencies = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy(ROOT + ".domain..")
            .layer("Application").definedBy(ROOT + ".application..")
            .layer("InboundAdapter").definedBy(ROOT + ".adapter.in..")
            .layer("OutboundAdapter").definedBy(ROOT + ".adapter.out..")
            .layer("Config").definedBy(ROOT + ".config..")
            .whereLayer("InboundAdapter").mayNotBeAccessedByAnyLayer()
            .whereLayer("OutboundAdapter").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("InboundAdapter", "OutboundAdapter", "Config")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "InboundAdapter", "OutboundAdapter");

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage(ROOT + ".domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "jakarta.validation..");

    @ArchTest
    static final ArchRule applicationDoesNotUseWebOrPersistence = noClasses()
            .that().resideInAPackage(ROOT + ".application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.web..", "org.springframework.data..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule portsAreInterfaces = classes()
            .that().resideInAnyPackage(ROOT + ".application.port.in..", ROOT + ".application.port.out..")
            .and().haveSimpleNameNotEndingWith("Command")
            .should().beInterfaces();
}
