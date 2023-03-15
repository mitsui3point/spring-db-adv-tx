package hello.springtx.propagation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    @Transactional//Propagation propagation() default Propagation.REQUIRED;
    public void saveTx(Member member) {
        save(member);
    }

    public void saveNoTx(Member member) {
        save(member);
    }

    public Optional<Member> find(String username) {
        return em.createQuery("select m from Member m where m.username = :username")
                .setParameter("username", username)
                .getResultList()
                .stream()
                .findAny();
    }

    private void save(Member member) {
        log.info("member 저장");
        em.persist(member);
    }
}
