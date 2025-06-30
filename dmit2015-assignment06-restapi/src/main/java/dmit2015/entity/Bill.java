package dmit2015.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.random.RandomGenerator;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @NotBlank(message = "Please enter a payee name")
    private String payeeName;

    @NotNull(message = "Please enter/select the due date")
    @FutureOrPresent(message = "Payment due date must be in the future or present day")
    private LocalDate dueDate = LocalDate.now().plusWeeks(2);

    @NotNull(message = "Please enter the amount that is due.")
    private BigDecimal paymentDue;

    private boolean paid = false;

    @Version
    private Integer version;

    @Column(nullable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    private void beforePersist() {
        createTime = LocalDateTime.now();
    }

    @PreUpdate
    private void beforeUpdate() {
        updateTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object obj) {
        return ( (obj instanceof Bill other) && Objects.equals(id, other.id) );
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static Bill of(Faker faker) {
        Bill currentBill = new Bill();
        currentBill.setPayeeName(faker.company().name());
        currentBill.setDueDate(LocalDate.now().plusWeeks(2));
        currentBill.setPaymentDue(BigDecimal.valueOf(RandomGenerator.getDefault().nextDouble(2, 100)));
        return currentBill;
    }
}
