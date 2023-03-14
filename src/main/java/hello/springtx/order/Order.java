package hello.springtx.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue
    private Long id;

    private String username;    //정상, 예외, 잔고부족
    private String payStatus;   //대기, 완료

    public Order(String username) {
        this.username = username;
    }
}
