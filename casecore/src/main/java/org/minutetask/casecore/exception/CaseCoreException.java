package org.minutetask.casecore.exception;

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

public abstract class CaseCoreException extends RuntimeException {
    private static final long serialVersionUID = -2209566400024180110L;

    public CaseCoreException() {
        super();
    }

    public CaseCoreException(String message) {
        super(message);
    }

    public CaseCoreException(Throwable cause) {
        super(cause);
    }

    public CaseCoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
