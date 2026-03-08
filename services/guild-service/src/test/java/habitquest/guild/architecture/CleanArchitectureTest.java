package habitquest.guild.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("PMD")
@AnalyzeClasses(packages = "habitquest.guild")
class CleanArchitectureTest {
  private static final String DOMAIN_PACKAGE = "..domain..";

  @ArchTest
  private static ArchRule domainShouldNotDependOnOtherLayers =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN_PACKAGE)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..application..", "..adapter..", "..infrastructure..")
          .because("Domain layer must not depend on outer layers");

  //  @ArchTest
  //  private static ArchRule domainShouldNotDependOnSpring =
  //      noClasses()
  //          .that()
  //          .resideInAPackage(DOMAIN_PACKAGE)
  //          .should()
  //          .dependOnClassesThat()
  //          .resideInAnyPackage("org.springframework..")
  //          .because("Domain layer must be framework-agnostic");

  @ArchTest
  private static ArchRule applicationShouldNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..adapter..", "..infrastructure..")
          .allowEmptyShould(true)
          .because("Application layer must not depend on adapters or infrastructure");

  @ArchTest
  private static ArchRule servicesShouldNotBeInDomain =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN_PACKAGE)
          .should()
          .beAnnotatedWith(Service.class)
          .because("Spring @Service annotation should not be used in the domain layer");

  @ArchTest
  private static ArchRule controllersShouldNotBeInDomain =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN_PACKAGE)
          .should()
          .beAnnotatedWith(RestController.class)
          .orShould()
          .beAnnotatedWith(Controller.class)
          .because("Controllers should not be in the domain layer");

  @ArchTest
  private static ArchRule repositoriesShouldNotBeInDomain =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN_PACKAGE)
          .should()
          .beAnnotatedWith(Repository.class)
          .because("Spring @Repository annotation should not be used in the domain layer");
}
