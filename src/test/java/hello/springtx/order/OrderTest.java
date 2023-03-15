package hello.springtx.order;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class OrderTest {
    @Autowired
    EntityManager em;
    @Test
    void orderTest() {
        //given
        String username = "정상";
        //when
        Order order = new Order(username);
        em.persist(order);
        em.flush();
        //then
        assertThat(order.getUsername()).isEqualTo(username);
    }
}
