package org.minutetask.casecore.jpa.repository;

/*-
 * ========================LICENSE_START=================================
 * org.minutetask.casecore:casecore
 * %%
 * Copyright (C) 2025 Jan Komrska
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import java.time.LocalDateTime;
import java.util.List;

import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface UseCaseActionRepository extends JpaRepository<UseCaseActionEntity, Long> {
    @Query("SELECT action FROM UseCaseActionEntity action WHERE (action.scheduledDate<=?1)")
    public List<UseCaseActionEntity> findScheduledActions(LocalDateTime targetDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public UseCaseActionEntity getLockedEntityById(Long id);
}
