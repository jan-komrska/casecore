package org.minutetask.tstapp.simple;

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

import org.minutetask.casecore.annotation.ContractRef;
import org.minutetask.casecore.annotation.IdRef;
import org.minutetask.casecore.annotation.KeyRef;

@ContractRef
public interface DocumentFlow {
    public void run(@IdRef Long id);

    public void pageUploaded(@KeyRef("DocumentFlow::pageUrl") String pageUrl, int pageState, String message);

    public void reviewFinished(@KeyRef("DocumentFlow::documentId") String documentId, int score, String message);
}
