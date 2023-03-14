package hello.springtx.order;

import hello.springtx.order.constants.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
public class OrderServiceTest {
    @Autowired
    OrderService service;

    @Autowired
    OrderRepository repository;

    /**
     * 사용자 이름을 "정상" 으로 설정했다. 모든 프로세스가 정상 수행된다.
     * 다음을 통해서 데이터가 완료 상태로 저장 되었는지 검증한다.
     * assertThat(findOrder.getPayStatus()).isEqualTo("완료");
     */
    @Test
    @DisplayName("주문 정상")
    void successTest() throws NotEnoughMoneyException {
        //given
        String username = OrderStatus.ORDER_SUCCESS;
        String payStatus = OrderStatus.STATUS_COMPLETE;
        Order order = new Order(username);
        //when
        service.order(order);
        Order findOrder = repository.findById(order.getId()).orElseThrow();
        //then
        assertThat(findOrder.getPayStatus()).isEqualTo(payStatus);
    }

    /**
     * 사용자 이름을 "예외" 로 설정했다.
     * RuntimeException("시스템 예외") 이 발생한다.
     * 런타임 예외로 롤백이 수행되었기 때문에 Order 데이터가 비어 있는 것을 확인할 수 있다.
     */
    @Test
    @DisplayName("주문 예외")
    void runtimeExceptionTest() {
        //given
        String username = OrderStatus.ORDER_EXCEPTION;
        Order order = new Order(username);
        //when
        assertThatThrownBy(() -> service.order(order))
                .isInstanceOf(RuntimeException.class);
        Optional<Order> optionalOrder = repository.findById(order.getId());
        //then
        assertThat(optionalOrder).isEmpty();
    }

    /**
     * 사용자 이름을 "잔고부족" 으로 설정했다.
     * NotEnoughMoneyException("잔고가 부족합니다") 이 발생한다.
     * 체크 예외로 커밋이 수행되었기 때문에 Order 데이터가 저장된다.
     * 다음을 통해서 데이터가 대기 상태로 잘 저장 되었는지 검증한다.
     * assertThat(findOrder.getPayStatus()).isEqualTo("대기");
     */
    @Test
    @DisplayName("잔고 부족 비즈니스 예외")
    void bizExceptionTest() {
        //given
        String username = OrderStatus.ORDER_NOT_ENOUGH_MONEY;
        String payStatus = OrderStatus.STATUS_WAIT;
        Order order = new Order(username);
        //when
        try {
            service.order(order);
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내");
        }
        //then
        Order findOrder = repository.findById(order.getId()).orElseThrow();
        assertThat(findOrder.getUsername()).isEqualTo(username);
        assertThat(findOrder.getPayStatus()).isEqualTo(payStatus);
    }
}
