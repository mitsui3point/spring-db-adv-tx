package hello.springtx.propagation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LogRepository {
    public static final String LOG_EXCEPTION_MESSAGE = "로그예외";
    private final EntityManager em;
    @Transactional//Propagation propagation() default Propagation.REQUIRED;
    public void saveTx(Log logInfo) {
        save(logInfo);
    }

    public void saveNoTx(Log logInfo) {
        save(logInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTxRequiresNew(Log logInfo) {
        save(logInfo);
    }

    private void save(Log logInfo) {
        log.info("log 저장");
        em.persist(logInfo);

        if (logInfo.getMessage().contains(LOG_EXCEPTION_MESSAGE)) {
            log.info("log 저장시 예외 발생");
            throw new RuntimeException("예외 발생");
        }
    }

    public Optional<Log> find(String message) {
        return em.createQuery("select l from Log l where l.message = :message")
                .setParameter("message", message)
                .getResultList()
                .stream()
                .findAny();
    }
}
