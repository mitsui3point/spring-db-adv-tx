package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final LogRepository logRepository;
    public void joinNoTxV1(String username) {
        Member member = new Member(username);
        Log logInfo = new Log(username);

        log.info("== memberRepository 시작 ==");
        memberRepository.saveTx(member);
        log.info("== memberRepository 종료 ==");

        log.info("== logRepository 시작 ==");
        logRepository.saveTx(logInfo);
        log.info("== logRepository 종료 ==");
    }

}
