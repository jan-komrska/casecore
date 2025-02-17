package org.minutetask.tstapp;

import org.minutetask.casecore.annotation.ActiveRef;
import org.minutetask.casecore.annotation.IdRef;

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

import org.minutetask.casecore.annotation.KeyRef;
import org.minutetask.casecore.annotation.ServiceRef;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateEncounterCase {
    @IdRef
    private Long id;

    @ActiveRef
    private boolean active;

    private String personId;

    private String encounterId;

    @KeyRef("CreateEncounterCase::tcn")
    private String tcn;

    @ServiceRef(Runnable.class)
    private String action;
}
