package org.minutetask.casecore.jpa.repository;

import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UseCaseRepository extends JpaRepository<UseCaseEntity, Long> {
}
