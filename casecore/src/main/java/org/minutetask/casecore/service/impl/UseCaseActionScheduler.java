package org.minutetask.casecore.service.impl;

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
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseActionScheduler {
    @Autowired
    private UseCaseActionService useCaseActionService;

    @Scheduled(fixedRateString = "${case-core.use-case-action.check-interval:5000}")
    @Transactional
    public void checkScheduledActions() {
        List<UseCaseActionEntity> scheduledActions = useCaseActionService.findScheduledActions(LocalDateTime.now());
        for (UseCaseActionEntity scheduledAction : scheduledActions) {
            scheduledAction.setScheduledDate(null);
        }
    }
}
