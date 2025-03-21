package org.minutetask.tstapp.process;

/*-
 * ========================LICENSE_START=================================
 * casecore-test-application
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

import org.minutetask.casecore.ActionContext;
import org.minutetask.casecore.annotation.ContractRef;
import org.minutetask.casecore.annotation.IdRef;
import org.minutetask.casecore.annotation.MethodRef;

@ContractRef(primary = false)
public interface BuildFlow {
    @MethodRef(async = true, persistent = true)
    public void run(@IdRef Long caseId, ActionContext actionContext);

    @MethodRef(async = true, persistent = true)
    public void compileProject(@IdRef Long caseId, ActionContext actionContext);

    @MethodRef(async = true, persistent = true)
    public void packageProject(@IdRef Long caseId, ActionContext actionContext);

    @MethodRef(async = true, persistent = true)
    public void deployProject(@IdRef Long caseId, ActionContext actionContext);
}
