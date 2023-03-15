package hello.springtx.propagation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

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
        memberService.joinV1_OffOnOn(username);
        Optional<Member> actualMember = memberRepository.find(username);
        Optional<Log> actualLog = logRepository.find(username);

        //then
        assertThat(actualMember).isPresent();
        assertThat(actualLog).isPresent();
    }

    /**
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON Exception
     */
    @Test
    void outerTxOFF_fail() {
        //given
        String username = "outerTxOFF_fail" + LogRepository.LOG_EXCEPTION_MESSAGE;

        //when
        assertThatThrownBy(() -> memberService.joinV1_OffOnOn(username))
                .isInstanceOf(RuntimeException.class);

        Optional<Member> actualMember = memberRepository.find(username);
        Optional<Log> actualLog = logRepository.find(username);

        //then
        assertThat(actualMember).isPresent();
        assertThat(actualLog).isNotPresent();
    }

    /**
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:OFF
     * LogRepository    @Transactional:OFF
     */
    @Test
    void singleTx() {
        //given
        String username = "singleTx";

        //when
        memberService.joinV1_OnOffOff(username);

        Optional<Member> actualMember = memberRepository.find(username);
        Optional<Log> actualLog = logRepository.find(username);

        //then
        assertThat(actualMember).isPresent();
        assertThat(actualLog).isPresent();
    }

    /**
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON
     */
    @Test
    void outerTxOn_Success() {
        //given
        String username = "outerTxOn_Success";

        //when
        memberService.joinV1_OnOnOn(username);

        Optional<Member> actualMember = memberRepository.find(username);
        Optional<Log> actualLog = logRepository.find(username);

        //then
        assertThat(actualMember).isPresent();
        assertThat(actualLog).isPresent();
    }

    /**
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON Exception
     */
    @Test
    void outerTxOn_fail() {
        //given
        String username = "outerTxOn_fail" + LogRepository.LOG_EXCEPTION_MESSAGE;

        //when
        assertThatThrownBy(() -> memberService.joinV1_OnOnOn(username))
                .isInstanceOf(RuntimeException.class);

        Optional<Member> actualMember = memberRepository.find(username);
        Optional<Log> actualLog = logRepository.find(username);

        //then
        assertThat(actualMember).isNotPresent();
        assertThat(actualLog).isNotPresent();
    }

    /**
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON Exception
     */
    @Test
    void recoverException_fail() {
        //given
        String username = "recoverException_fail" + LogRepository.LOG_EXCEPTION_MESSAGE;

        //when
        assertThatThrownBy(() -> memberService.joinV2_OnOnOn(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        Optional<Member> actualMember = memberRepository.find(username);
        Optional<Log> actualLog = logRepository.find(username);

        //then
        /* 자주 하는 실수
            assertThat(actualMember).isPresent(); -> X
            내부 LogRepository RuntimeException 을
            외부 MemberService 에서 catch 해서 recover 했으므로,
            member(commit), log(rollback) 을 기대했으나,
            JDBC transaction marked for rollback-only (exception provided for stack trace) 처리가 되면서 아래의 과정을 거쳐 전체 롤백이 진행된다.
            : 내부 LogRepository @Transactional(내부 논리 트랜잭션) 에서
              외부 MemberService @Transactional(외부 논리 트랜잭션; 물리 트랜잭션) 에 rollback-only 를 mark 했기 때문에
              외부 MemberService @Transactional(외부 논리 트랜잭션; 물리 트랜잭션) 의 전체 rollback 이 이루어진다.
        */
        assertThat(actualMember).isNotPresent();
        assertThat(actualLog).isNotPresent();
    }
}
