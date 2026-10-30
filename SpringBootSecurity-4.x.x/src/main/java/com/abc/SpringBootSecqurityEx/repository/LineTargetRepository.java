package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.LineTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface LineTargetRepository extends JpaRepository<LineTarget, Long> {
    List<LineTarget> findByTargetDateBetween(LocalDate from, LocalDate to);
    List<LineTarget> findByLineCodeAndTargetDateBetween(String lineCode, LocalDate from, LocalDate to);
}