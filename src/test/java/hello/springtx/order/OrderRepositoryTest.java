package hello.springtx.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class OrderRepositoryTest {

    @Autowired
    OrderRepository repository;

    @Test
    void orderSaveTest() {
        //given
        Long id = 1L;
        String username = "정상";
        //when
        Order order = new Order(username);
        repository.save(order);
        //then
        assertThat(order.getId()).isEqualTo(id);
        assertThat(order.getUsername()).isEqualTo(username);
    }
}
