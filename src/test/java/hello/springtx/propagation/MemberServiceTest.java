package hello.springtx.propagation;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * MemberService    @Transactional:OFF
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON
     */
    @Test
    void outerTxOFF_success() {
        //given
        String username = "outerTxOFF_success";

        //when
        memberService.joinNoTxV1(username);
        Optional<Member> actualMember = memberRepository.find(username);
        Optional<Log> actualLog = logRepository.find(username);

        //then
        assertThat(actualMember).isPresent();
        assertThat(actualLog).isPresent();
    }

    /**
     * MemberService    @Transactional:OFF
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON Exception
     */
    @Test
    void outerTxOFF_fail() {
        //given
        String username = "outerTxOFF_fail" + LogRepository.LOG_EXCEPTION_MESSAGE;

        //when
        assertThatThrownBy(() -> memberService.joinNoTxV1(username))
                .isInstanceOf(RuntimeException.class);

        Optional<Member> actualMember = memberRepository.find(username);
        Optional<Log> actualLog = logRepository.find(username);

        //then
        assertThat(actualMember).isPresent();
        assertThat(actualLog).isNotPresent();
    }
}
