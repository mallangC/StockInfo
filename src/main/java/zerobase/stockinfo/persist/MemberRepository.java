package zerobase.stockinfo.persist;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.stockinfo.model.MemberEntity;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

  Optional<MemberEntity> findByUsername(String username);

  boolean existsByUsername(String username);

}
