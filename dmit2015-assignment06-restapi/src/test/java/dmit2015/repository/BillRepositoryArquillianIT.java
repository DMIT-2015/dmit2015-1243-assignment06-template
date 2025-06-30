package dmit2015.repository;

import dmit2015.config.ApplicationConfig;
import dmit2015.entity.Bill;
import dmit2015.entity.NewBillChecks;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import net.datafaker.Faker;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(ArquillianExtension.class)
public class BillRepositoryArquillianIT { // The class must be declared as public

    static Faker faker = new Faker();

    static String mavenArtifactIdId;

    @Deployment
    public static WebArchive createDeployment() throws IOException, XmlPullParserException {
        PomEquippedResolveStage pomFile = Maven.resolver().loadPomFromFile("pom.xml");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("pom.xml"));
        mavenArtifactIdId = model.getArtifactId();
        final String archiveName = model.getArtifactId() + ".war";
        return ShrinkWrap.create(WebArchive.class, archiveName)
                .addAsLibraries(pomFile.resolve("org.codehaus.plexus:plexus-utils:3.4.2").withTransitivity().asFile())
                .addAsLibraries(pomFile.resolve("org.hamcrest:hamcrest:3.0").withTransitivity().asFile())
                .addAsLibraries(pomFile.resolve("org.assertj:assertj-core:3.27.3").withTransitivity().asFile())
                .addAsLibraries(pomFile.resolve("net.datafaker:datafaker:2.4.2").withTransitivity().asFile())
                .addAsLibraries(pomFile.resolve("com.h2database:h2:2.3.232").withTransitivity().asFile())
                .addAsLibraries(pomFile.resolve("com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11").withTransitivity().asFile())
                .addAsLibraries(pomFile.resolve("com.oracle.database.jdbc:ojdbc11:23.6.0.24.10").withTransitivity().asFile())
                .addAsLibraries(pomFile.resolve("org.postgresql:postgresql:42.7.5").withTransitivity().asFile())
//                .addAsLibraries(pomFile.resolve("com.mysql:mysql-connector-j:9.0.0").withTransitivity().asFile())
//                .addAsLibraries(pomFile.resolve("org.mariadb.jdbc:mariadb-java-client:3.4.1").withTransitivity().asFile())
                .addAsLibraries(pomFile.resolve("org.hibernate.orm:hibernate-spatial:6.6.9.Final").withTransitivity().asFile())
                // .addAsLibraries(pomFile.resolve("org.eclipse:yasson:3.0.4").withTransitivity().asFile())
                .addClass(ApplicationConfig.class)
                .addClasses(Bill.class, NewBillChecks.class, BillRepository.class, BillInitializer.class)
                .addAsResource("META-INF/persistence.xml")
                .addAsResource("META-INF/beans.xml");
    }

    @Inject
    private BillRepository _billRepository;

    @Resource
    private UserTransaction _beanManagedTransaction;

    @BeforeAll
    static void beforeAllTests() {
        // code to execute before all tests in the current test class
    }

    @AfterAll
    static void afterAllTests() {
        // code to execute after all tests in the current test class
    }

    @BeforeEach
    void beforeEachTestMethod() throws SystemException, NotSupportedException {
        // Start a new transaction
        _beanManagedTransaction.begin();
    }

    @AfterEach
    void afterEachTestMethod() throws SystemException {
        // Rollback the transaction
        _beanManagedTransaction.rollback();
    }

    @Order(1)
    @Test
    void givenNewEntity_whenAddEntity_thenEntityIsAdded() throws SystemException, NotSupportedException {
        // Arrange
        Bill newBill = Bill.of(faker);

        // Act
        _billRepository.add(newBill);

        // Assert
        assertThat(newBill.getId())
                .isNotNull();

    }

    @Order(2)
    @Test
    void givenExistingId_whenFindById_thenReturnEntity() {
        // Arrange
        Bill newBill = Bill.of(faker);

        // Act
        _billRepository.add(newBill);

        // Assert
        Optional<Bill> optionalBill = _billRepository.findById(newBill.getId());
        assertThat(optionalBill.isPresent())
                .isTrue();
        // Assert
        var existingBill = optionalBill.orElseThrow();
        assertThat(existingBill)
                .usingRecursiveComparison()
                // .ignoringFields("field1", "field2")
                .isEqualTo(newBill);

    }

    @Order(3)
    @Test
    void givenExistingEntity_whenUpdatedEntity_thenEntityIsUpdated() {
        // Arrange
        Bill newBill = Bill.of(faker);

        _billRepository.add(newBill);

        newBill.setPayeeName(faker.company().name());
        newBill.setDueDate(LocalDate.now().plusWeeks(2));
        newBill.setPaymentDue(BigDecimal.valueOf(RandomGenerator.getDefault().nextDouble(2, 100)));

        // Act
        Bill updatedBill = _billRepository.update(newBill);

        // Assert
        Optional<Bill> optionalBill = _billRepository.findById(updatedBill.getId());
        assertThat(optionalBill.isPresent())
                .isTrue();
        var existingBill = optionalBill.orElseThrow();
        assertThat(existingBill)
                .usingRecursiveComparison()
                // .ignoringFields("field1", "field2")
                .isEqualTo(newBill);

    }

    @Order(4)
    @Test
    void givenExistingId_whenDeleteEntity_thenEntityIsDeleted() {
        // Arrange
        Bill newBill = Bill.of(faker);
        _billRepository.add(newBill);
        // Act
        _billRepository.deleteById(newBill.getId());
        // Assert
        Optional<Bill> optionalBill = _billRepository.findById(newBill.getId());
        assertThat(optionalBill.isPresent())
                .isFalse();

    }

    @Order(5)
    @ParameterizedTest
    @CsvSource({"10"})
    void givenMultipleEntity_whenFindAll_thenReturnEntityList(int expectedRecordCount) {
        // Arrange: Set up the initial state

        // Delete all existing data
        assertThat(_billRepository).isNotNull();
        _billRepository.deleteAll();
        // Generate expectedRecordCount number of fake data
        Bill firstExpectedBill = null;
        Bill lastExpectedBill = null;
        for (int counter = 1; counter <= expectedRecordCount; counter++) {
            Bill currentBill = Bill.of(faker);
            if (counter == 1) {
                firstExpectedBill = currentBill;
            } else if (counter == expectedRecordCount) {
                lastExpectedBill = currentBill;
            }

            _billRepository.add(currentBill);
        }

        // Act: Perform the action to be tested
        List<Bill> billList = _billRepository.findAll();

        // Assert: Verify the expected outcome
        assertThat(billList.size())
                .isEqualTo(expectedRecordCount);

        // Get the first entity and compare with expected results
        var firstActualBill = billList.getFirst();
        assertThat(firstActualBill)
                .usingRecursiveComparison()
                // .ignoringFields("field1", "field2")
                .isEqualTo(firstExpectedBill);
        // Get the last entity and compare with expected results
        var lastActualBill = billList.getLast();
        assertThat(lastActualBill)
                .usingRecursiveComparison()
                // .ignoringFields("field1", "field2")
                .isEqualTo(lastExpectedBill);

    }

    @Order(6)
    @ParameterizedTest
    @CsvSource(value = {
            ", 2025-04-01, 99.88, Please enter a payee name",
            "A & W, , 18.99, Please enter/select the due date",
            "A & W, 2000-04-01, 18.99, Payment due date must be in the future or present day",
            "A & W, 2025-04-01, , Please enter the amount that is due",
    }, nullValues = {"null"})
    void givenEntityWithValidationErrors_whenAddEntity_thenThrowException(
            String payeeName,
            LocalDate dueDate,
            BigDecimal paymentDue,
            String expectedExceptionMessage
    ) {
        // Arrange
        Bill newBill = new Bill();
        newBill.setPayeeName(payeeName);
        newBill.setDueDate(dueDate);
        newBill.setPaymentDue(paymentDue);

        try {
            // Act
            _billRepository.add(newBill);
            fail("An bean validation constraint should have been thrown");
        } catch (Exception ex) {
            // Assert
            assertThat(ex)
                    .hasMessageContaining(expectedExceptionMessage);
        }

    }

}