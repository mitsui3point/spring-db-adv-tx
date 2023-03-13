package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * basicService.tx() 호출
 *      클라이언트가 basicService.tx() 를 호출하면, 프록시의 tx() 가 호출된다. 여기서 프록시는 tx() 메서드가 트랜잭션을 사용할 수 있는지 확인해본다. tx() 메서드에는 @Transactional 이 붙어있으므로 트랜잭션 적용 대상이다.
 *      따라서 트랜잭션을 시작한 다음에 실제 basicService.tx() 를 호출한다.
 *      그리고 실제 basicService.tx() 의 호출이 끝나서 프록시로 제어가(리턴) 돌아오면 프록시는 트랜잭션 로직을 커밋하거나 롤백해서 트랜잭션을 종료한다.
 *
 * basicService.nonTx() 호출
 *      클라이언트가 basicService.nonTx() 를 호출하면, 트랜잭션 프록시의 nonTx() 가 호출된다. 여기서 nonTx() 메서드가 트랜잭션을 사용할 수 있는지 확인해본다. nonTx() 에는 @Transactional 이 없으므로 적용 대상이 아니다.
 *      따라서 트랜잭션을 시작하지 않고, basicService.nonTx() 를 호출하고 종료한다.
 */
@Slf4j
@SpringBootTest
public class TxBasicTest {

    @Autowired
    BasicService basicService;

    @Test
    void proxyCheck() {
        log.info("aop class={}", basicService.getClass());
        assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }

    @Test
    void txTest() {
        basicService.tx();
        basicService.nonTx();
    }

    @TestConfiguration
    static class TxApplyBasicConfig {
        @Bean
        BasicService basicService() {
            return new BasicService();
        }
    }

    @Slf4j
    static class BasicService {
        @Transactional
        public void tx() {
            log.info("call tx");
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();//현재 쓰레드에 트랜잭션이 적용되어있는지 확인할수 있는 기능이다.
            log.info("txActive={}", txActive);
        }

        public void nonTx() {
            log.info("call nonTx");
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive={}", txActive);
        }
    }
}
