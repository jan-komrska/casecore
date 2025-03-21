package org.minutetask.casecore;

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

public interface UseCaseManager {
    public <UseCase> UseCase getUseCase(Object id, Class<UseCase> useCaseClass);

    public <UseCase> UseCase getUseCase(String keyType, String keyValue, Class<UseCase> useCaseClass);

    public <UseCase> UseCase lockUseCase(UseCase useCase);

    public <UseCase> UseCase refreshUseCase(UseCase useCase);

    public <UseCase> UseCase saveUseCase(UseCase useCase);

    public <UseCase> UseCase deleteUseCase(UseCase useCase);
}
