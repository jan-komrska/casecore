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

import org.minutetask.casecore.jpa.entity.UseCaseEntity;

public interface UseCaseService {
    public UseCaseEntity newUseCase();

    public UseCaseEntity getUseCase(Long id);

    public UseCaseEntity getUseCase(String keyType, String keyValue);

    public UseCaseEntity lockUseCase(UseCaseEntity useCase);

    public UseCaseEntity saveUseCase(UseCaseEntity useCase);

    public void deleteUseCaseById(Long id);

    //

    public <Data> Data getUseCaseData(UseCaseEntity useCase, Class<Data> dataClass);

    public void updateUseCaseData(UseCaseEntity useCase, Object data);
}
