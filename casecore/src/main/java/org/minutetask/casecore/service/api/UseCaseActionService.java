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

import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;

public interface UseCaseActionService {
    public UseCaseActionEntity newAction(UseCaseEntity useCase, Method method, Object[] args);

    public UseCaseActionEntity getAction(Long id);

    public void saveAction(UseCaseActionEntity action);

    public void deleteAction(Long id);

    //

    public Method getActionMethod(UseCaseActionEntity action);

    public Object[] getActionParameters(UseCaseActionEntity action);
}
