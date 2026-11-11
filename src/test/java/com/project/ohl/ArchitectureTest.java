package com.project.ohl;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class ArchitectureTest {
    private static final JavaClasses PRODUCTION = new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("com.project.ohl");

    @Test
    void domainIsIndependentOfFrameworksAndOuterLayers() {
        classes().that().resideInAPackage("..domain..")
              .should().onlyDependOnClassesThat()
              .resideInAnyPackage("java..", "com.project.ohl.domain..", "lombok..")
              .check(PRODUCTION);
    }

    @Test
    void applicationUsesPortsAndDomainInsteadOfFrameworksAndAdapters() {
        classes().that().resideInAPackage("..application..")
              .should().onlyDependOnClassesThat()
              .resideInAnyPackage("java..", "com.project.ohl.application..", "com.project.ohl.domain..",
                    "jakarta.validation..", "lombok..")
              .check(PRODUCTION);
    }

    @Test
    void webAndPersistenceAdaptersCannotCallEachOther() {
        noClasses().that().resideInAPackage("..adapter.web..")
              .should().dependOnClassesThat().resideInAPackage("..adapter.persistence..")
              .check(PRODUCTION);
        noClasses().that().resideInAPackage("..adapter.persistence..")
              .should().dependOnClassesThat()
              .resideInAnyPackage("..adapter.web..", "..application.service..")
              .check(PRODUCTION);
    }

    @Test
    void architectureLayersHaveNoDependencyCycles() {
        slices().matching("com.project.ohl.(*)..").should().beFreeOfCycles().check(PRODUCTION);
    }
}
