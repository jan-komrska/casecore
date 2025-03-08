package org.minutetask.casecore.jpa.entity;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UseCaseActionData {
    private Long serviceClassId;

    private Long methodClassId;

    private String methodName;

    @Getter(AccessLevel.NONE)
    private List<Long> parameterClassIds;

    @Getter(AccessLevel.NONE)
    private List<Object> parameters;

    private Long lastExceptionClassId;

    private String lastExceptionMessage;

    //

    public List<Long> getParameterClassIds() {
        if (parameterClassIds == null) {
            parameterClassIds = new ArrayList<Long>();
        }
        return parameterClassIds;
    }

    public List<Object> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<Object>();
        }
        return parameters;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return (serviceClassId == null) //
                && (methodClassId == null) && StringUtils.isEmpty(methodName) //
                && getParameterClassIds().isEmpty() && getParameters().isEmpty();
    }
}
