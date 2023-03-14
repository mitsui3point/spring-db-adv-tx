package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static hello.springtx.order.constants.OrderStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        /*
        private String username;    //정상, 예외, 잔고부족
        private String payStatus;   //대기, 완료
        */
        log.info("order 호출");
        repository.save(order);
        log.info("결제 프로세스 진입");
        if (ORDER_SUCCESS.equals(order.getUsername())) {
            order.setPayStatus(STATUS_COMPLETE);
        }
        if (ORDER_EXCEPTION.equals(order.getUsername())) {
            log.info("시스템 예외 발생");
            throw new RuntimeException("시스템 예외");
        }
        if (ORDER_NOT_ENOUGH_MONEY.equals(order.getUsername())) {
            log.info("잔고 부족 비즈니스 예외 발생");
            order.setPayStatus(STATUS_WAIT);
            throw new NotEnoughMoneyException("잔고 부족 비즈니스 예외");
        }
        log.info("결제 프로세스 완료");
    }
}
