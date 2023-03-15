package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final LogRepository logRepository;

    public void joinV1_OffOnOn(String username) {
        join(username,
                member -> memberRepository.saveTx(member),
                logInfo -> logRepository.saveTx(logInfo)
        );
    }

    @Transactional
    public void joinV1_OnOffOff(String username) {
        join(username,
                member -> memberRepository.saveNoTx(member),
                logInfo -> logRepository.saveNoTx(logInfo)
        );
    }

    @Transactional
    public void joinV1_OnOnOn(String username) {
        join(username,
                member -> memberRepository.saveTx(member),
                logInfo -> logRepository.saveTx(logInfo));
    }

    @Transactional
    public void joinV2_OnOnOn(String username) {
        join(username,
                member -> memberRepository.saveTx(member),
                logInfo -> {
                    try {
                        logRepository.saveTx(logInfo);
                    } catch (RuntimeException e) {
                        log.info("log 저장에 실패했습니다. logMessage={}", logInfo.getMessage());
                        log.info("정상 흐름 변환");
                    }
                });
    }

    @Transactional
    public void joinV2_OnOnOn_requiresNew(String username) {
        join(username,
                member -> memberRepository.saveTx(member),
                logInfo -> {
                    try {
                        logRepository.saveTxRequiresNew(logInfo);
                    } catch (RuntimeException e) {
                        log.info("log 저장에 실패했습니다. logMessage={}", logInfo.getMessage());
                        log.info("정상 흐름 변환");
                    }
                });
    }

    private void join(String username,
                      Callback<Member> memberRepositoryCallback,
                      Callback<Log> logRepositoryCallback) {
        Member member = new Member(username);
        Log logInfo = new Log(username);

        log.info("== memberRepository 시작 ==");
        memberRepositoryCallback.callback(member);
        log.info("== memberRepository 종료 ==");

        log.info("== logRepository 시작 ==");
        logRepositoryCallback.callback(logInfo);
        log.info("== logRepository 종료 ==");
    }

    private static interface Callback<T> {
        void callback(T t);
    }
}
