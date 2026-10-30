package com.abc.SpringBootSecqurityEx.repository;

import com.abc.SpringBootSecqurityEx.entity.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    boolean existsByCode(String code);
    java.util.Optional<com.abc.SpringBootSecqurityEx.entity.Dealer> findByCode(String code);
}
