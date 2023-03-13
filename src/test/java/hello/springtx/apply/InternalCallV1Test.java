package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void internalCallTest() {
        callService.internal();
    }

    /**
     * 1. 클라이언트인 테스트 코드는 callService.external() 을 호출한다. 여기서 callService 는 트랜잭션 프록시이다.
     * 2. callService 의 트랜잭션 프록시가 호출된다.
     * 3. external() 메서드에는 @Transactional 이 없다. 따라서 트랜잭션 프록시는 트랜잭션을 적용하지 않는다.
     * 4. 트랜잭션 적용하지 않고, 실제 callService 객체 인스턴스의 external() 을 호출한다.
     * 5. external() 은 내부에서 internal() 메서드를 호출한다. 그런데 여기서 문제가 발생한다.
     *
     * 문제 원인
     *      자바 언어에서 메서드 앞에 별도의 참조가 없으면 this 라는 뜻으로 자기 자신의 인스턴스를 가리킨다.
     *      결과적으로 자기 자신의 내부 메서드를 호출하는 this.internal() 이 되는데,
     *          여기서 this 는 자기 자신을 가리키므로,
     *          실제 대상 객체( target )의 인스턴스를 뜻한다.
     *      결과적으로 이러한 내부 호출은 프록시를 거치지 않는다.
     *      따라서 트랜잭션을 적용할 수 없다. 결과적으로 target 에 있는 internal() 을 직접 호출하게 된 것이다.
     *
     * 프록시 방식의 AOP 한계
     *      @Transactional 를 사용하는 트랜잭션 AOP는 프록시를 사용한다. 프록시를 사용하면 메서드 내부 호출에 프록시를 적용할 수 없다.
     *      그렇다면 이 문제를 어떻게 해결할 수 있을까?
     *      가장 단순한 방법은 내부 호출을 피하기 위해 internal() 메서드를 별도의 클래스로 분리하는 것이다.
     */
    @Test
    void externalCallTest() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {
        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    @Slf4j
    static class CallService {
        public void external() {
            log.info("call external");
            printTxInfo();
            internal();//this.internal();
        }

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
    }
}
