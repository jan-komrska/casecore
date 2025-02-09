package org.minutetask.casecore.jpa.repository;

import org.minutetask.casecore.jpa.entity.UseCaseKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UseCaseKeyRepository extends JpaRepository<UseCaseKeyEntity, Long> {
    public UseCaseKeyEntity findByTypeAndValue(String type, String value);
}
