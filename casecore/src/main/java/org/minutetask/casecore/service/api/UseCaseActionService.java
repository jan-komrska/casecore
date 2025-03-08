package org.minutetask.casecore.service.api;

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

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;

public interface UseCaseActionService {
    public UseCaseActionEntity newAction(UseCaseEntity useCase, boolean active);

    public UseCaseActionEntity getAction(Long id);

    public UseCaseActionEntity persistAction(UseCaseActionEntity action);

    public UseCaseActionEntity saveAction(UseCaseActionEntity action);

    public UseCaseActionEntity deleteAction(UseCaseActionEntity action);

    public List<UseCaseActionEntity> findScheduledActions(LocalDateTime targetDate);

    //

    public Class<?> getServiceClass(UseCaseActionEntity action);

    public void setServiceClass(UseCaseActionEntity action, Class<?> serviceClass);

    public Method getMethod(UseCaseActionEntity action);

    public void setMethod(UseCaseActionEntity action, Method method);

    public Object[] getArgs(UseCaseActionEntity action);

    public void setArgs(UseCaseActionEntity action, Object[] args);

    public boolean isPersistent(UseCaseActionEntity action);

    public boolean isAsync(UseCaseActionEntity action);

    public String getTaskExecutor(UseCaseActionEntity action);

    public Class<?> getLastExceptionClass(UseCaseActionEntity action);

    public String getLastExceptionMessage(UseCaseActionEntity action);

    public void setLastException(UseCaseActionEntity action, Throwable throwable);

    public int getRetryCount(UseCaseActionEntity action);

    public void incRetryCount(UseCaseActionEntity action);
}
