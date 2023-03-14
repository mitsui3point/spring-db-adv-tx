package hello.springtx.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NotEnoughMoneyExceptionTest {
    @Test
    @DisplayName("NotEnoughMoneyException 체크 예외인지 확인")
    void isCheckedException() {
        assertThatThrownBy(() -> throwException())
                .isInstanceOf(Exception.class)
                .isNotInstanceOf(RuntimeException.class);
    }

    void throwException() throws NotEnoughMoneyException {
        throw new NotEnoughMoneyException("예외 발생");
    }
}
