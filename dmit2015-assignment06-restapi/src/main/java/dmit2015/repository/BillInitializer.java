package dmit2015.repository;

import dmit2015.entity.Bill;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.datafaker.Faker;

import java.util.logging.Logger;

@ApplicationScoped
public class BillInitializer {
    private final Logger _logger = Logger.getLogger(BillInitializer.class.getName());

    @Inject
    private BillRepository _billRepository;


    /**
     * Using the combination of `@Observes` and `@Initialized` annotations, you can
     * intercept and perform additional processing during the phase of beans or events
     * in a CDI container.
     * <p>
     * The @Observers is used to specify this method is in observer for an event
     * The @Initialized is used to specify the method should be invoked when a bean type of `ApplicationScoped` is being
     * initialized
     * <p>
     * Execute code to create the test data for the entity.
     * This is an alternative to using a @WebListener that implements a ServletContext listener.
     * <p>
     * ]    * @param event
     */
    public void initialize(@Observes @Initialized(ApplicationScoped.class) Object event) {
        _logger.info("Initializing bills");

        if (_billRepository.count() == 0) {

            try {
                // Generate 10 fake Bills
                var faker = new Faker();
                for (int count = 1; count <= 10; count++) {
                    var currentBill = Bill.of(faker);
                    _billRepository.add(currentBill);
                }

            } catch (Exception ex) {
                _logger.fine(ex.getMessage());
            }

            _logger.info("Created " + _billRepository.count() + " records.");
        }
    }
}