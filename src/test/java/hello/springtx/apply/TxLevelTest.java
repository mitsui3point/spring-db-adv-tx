package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 스프링에서 우선순위는 항상 더 구체적이고 자세한 것이 높은 우선순위를 가진다. 이것만 기억하면 스프링에서 발생하는 대부분의 우선순위를 쉽게 기억할 수 있다.
 * 그리고 더 구체적인 것이 더 높은 우선순위를 가지는 것은 상식적으로 자연스럽다.
 *
 * 우선순위
 *      트랜잭션을 사용할 때는 다양한 옵션을 사용할 수 있다.
 *      그런데 어떤 경우에는 옵션을 주고, 어떤 경우에는 옵션을 주지 않으면 어떤 것이 선택될까?
 *      예를 들어서 읽기 전용 트랜잭션 옵션을 사용하는 경우와 아닌 경우를 비교해보자. (읽기 전용 옵션에 대한 자세한 내용은 뒤에서 다룬다. 여기서는 적용 순서에 집중하자.)
 *      LevelService 의 타입에 @Transactional(readOnly = true) 이 붙어있다.
 *      write() : 해당 메서드에 @Transactional(readOnly = false) 이 붙어있다.
 *          이렇게 되면 타입에 있는 @Transactional(readOnly = true) 와 해당 메서드에 있는 @Transactional(readOnly = false) 둘 중 하나를 적용해야 한다.
 *          클래스 보다는 메서드가 더 구체적이므로 메서드에 있는 @Transactional(readOnly = false) 옵션을 사용한 트랜잭션이 적용된다.
 *
 * 클래스에 적용하면 메서드는 자동 적용
 *      read() : 해당 메서드에 @Transactional 이 없다. 이 경우 더 상위인 클래스를 확인한다.
 *          클래스에 @Transactional(readOnly = true) 이 적용되어 있다. 따라서 트랜잭션이 적용되고 readOnly = true 옵션을 사용하게 된다.
 *      참고로 readOnly=false 는 기본 옵션이기 때문에 보통 생략한다. 여기서는 이해를 돕기 위해 기본 옵션을 적어주었다.
 *      @Transactional == @Transactional(readOnly=false) 와 같다.
 *
 * 인터페이스에 @Transactional 적용
 *      인터페이스에도 @Transactional 을 적용할 수 있다. 이 경우 다음 순서로 적용된다. 구체적인 것이 더 높은 우선순위를 가진다고 생각하면 바로 이해가 될 것이다.
 *      1. 클래스의 메서드 (우선순위가 가장 높다.)
 *      2. 클래스의 타입
 *      3. 인터페이스의 메서드
 *      4. 인터페이스의 타입 (우선순위가 가장 낮다.)
 *
 * 클래스의 메서드를 찾고, 만약 없으면 클래스의 타입을 찾고 만약 없으면 인터페이스의 메서드를 찾고 그래도 없으면 인터페이스의 타입을 찾는다.
 *
 * 그런데 인터페이스에 @Transactional 사용하는 것은 스프링 공식 메뉴얼에서 권장하지 않는 방법이다.
 * AOP를 적용하는 방식에 따라서 인터페이스에 애노테이션을 두면 AOP가 적용이 되지 않는 경우도 있기 때문이다
 * 가급적 구체 클래스에 @Transactional 을 사용하자.
 */
@Slf4j
@SpringBootTest
public class TxLevelTest {

    @Autowired
    LevelService service;

    @Test
    void orderTest() {
        service.read();
        service.write();
    }

    @TestConfiguration
    static class TxLevelTestConfig {
        @Bean
        LevelService levelService() {
            return new LevelService();
        }
    }

    @Slf4j
    @Transactional(readOnly = true)
    static class LevelService {

        @Transactional(readOnly = false)
        public void write() {
            log.info("call write");
            printTxInfo();
        }

        public void read() {
            log.info("call read");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
            boolean txReadonly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();//현재 트랜잭션에 적용된 readOnly 옵션 값을 반환한다.
            log.info("tx readOnly={}", txReadonly);
        }
    }
}
