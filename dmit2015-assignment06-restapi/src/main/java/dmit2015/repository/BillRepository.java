package dmit2015.repository;

import dmit2015.entity.Bill;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * This Jakarta Persistence class contains methods for performing CRUD operations on a
 * Jakarta Persistence managed entity.
 */
@ApplicationScoped
public class BillRepository {

    // Assign a unitName if there are more than one persistence unit defined in persistence.xml
    @PersistenceContext //(unitName="pu-name-in-persistence.xml")   
    private EntityManager _entityManager;

    @Transactional
    public void add(@Valid Bill newBill) {
        // If the primary key is not an identity column then write code below here to
        // 1) Generate a new primary key value
        // 2) Set the primary key value for the new entity

        _entityManager.persist(newBill);
    }

    public Optional<Bill> findById(Long billId) {
        try {
            Bill querySingleResult = _entityManager.find(Bill.class, billId);
            if (querySingleResult != null) {
                return Optional.of(querySingleResult);
            }
        } catch (Exception ex) {
            // billId value not found
            throw new RuntimeException(ex);
        }
        return Optional.empty();
    }

    public List<Bill> findAll() {
        return _entityManager.createQuery("SELECT o FROM Bill o ", Bill.class)
                .getResultList();
    }

    @Transactional
    public Bill update(@Valid Bill updatedBill) {
        Optional<Bill> optionalBill = findById(updatedBill.getId());
        if (optionalBill.isEmpty()) {
            String errorMessage = String.format("The id %s does not exists in the system.", updatedBill.getId());
            throw new RuntimeException(errorMessage);
        } else {
            var existingBill = optionalBill.orElseThrow();
            // Update only properties that is editable by the end user
            existingBill.setPayeeName(updatedBill.getPayeeName());
            existingBill.setDueDate(updatedBill.getDueDate());
            existingBill.setPaymentDue(updatedBill.getPaymentDue());
            existingBill.setPaid(updatedBill.isPaid());

            updatedBill = _entityManager.merge(existingBill);
        }
        return updatedBill;
    }

    @Transactional
    public void delete(Bill existingBill) {
        // Write code to throw a RuntimeException if this entity contains child records

        if (_entityManager.contains(existingBill)) {
            _entityManager.remove(existingBill);
        } else {
            _entityManager.remove(_entityManager.merge(existingBill));
        }
    }

    @Transactional
    public void deleteById(Long billId) {
        Optional<Bill> optionalBill = findById(billId);
        if (optionalBill.isPresent()) {
            Bill existingBill = optionalBill.orElseThrow();
            // Write code to throw a RuntimeException if this entity contains child records

            _entityManager.remove(existingBill);
        }
    }

    public long count() {
        return _entityManager.createQuery("SELECT COUNT(o) FROM Bill o", Long.class).getSingleResult();
    }

    @Transactional
    public void deleteAll() {
        _entityManager.flush();
        _entityManager.clear();
        _entityManager.createQuery("DELETE FROM Bill").executeUpdate();
    }

}