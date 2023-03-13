package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
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
public class InternalCallV2Test {

    @Autowired
    InternalService internalService;
    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void internalCallTest() {
        internalService.internal();
    }

    /**
     * InternalService 클래스를 만들고 internal() 메서드를 여기로 옮겼다.
     * 이렇게 메서드 내부 호출을 외부 호출로 변경했다.
     * CallService 에는 트랜잭션 관련 코드가 전혀 없으므로 트랜잭션 프록시가 적용되지 않는다.
     * InternalService 에는 트랜잭션 관련 코드가 있으므로 트랜잭션 프록시가 적용된다.
     *
     * 1. 클라이언트인 테스트 코드는 callService.external() 을 호출한다.
     * 2. callService 는 실제 callService 객체 인스턴스이다.
     * 3. callService 는 주입 받은 internalService.internal() 을 호출한다.
     * 4. internalService 는 트랜잭션 프록시이다. internal() 메서드에 @Transactional 이 붙어 있으므로 트랜잭션 프록시는 트랜잭션을 적용한다.
     * 5. 트랜잭션 적용 후 실제 internalService 객체 인스턴스의 internal() 을 호출한다.
     */
    @Test
    void externalCallTest() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {
        @Bean
        CallService callService() {
            return new CallService(internalService());
        }
        @Bean
        InternalService internalService() {
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;
        public void external() {
            log.info("call external");
            printTxInfo();
            internalService.internal();//this.internal();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
    }

    @Slf4j
    static class InternalService {
        /**
         * public 메서드만 트랜잭션 적용
         * 스프링의 트랜잭션 AOP 기능은 public 메서드에만 트랜잭션을 적용하도록 기본 설정이 되어있다.
         * 그래서 protected , private , package-visible 에는 트랜잭션이 적용되지 않는다.
         * 생각해보면 protected , package-visible 도 외부에서 호출이 가능하다. 따라서 이 부분은 앞서 설명한 프록시의 내부 호출과는 무관하고, 스프링이 막아둔 것이다.
         *
         * 스프링 부트 3.0 부터는 protected , package-visible (default 접근제한자)에도 트랜잭션이 적용된다.
         */
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
