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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UseCaseData {
    @Getter(AccessLevel.NONE)
    private Map<String, Object> parameters = null;

    @Getter(AccessLevel.NONE)
    private Map<Long, Object> keys = null;

    @Getter(AccessLevel.NONE)
    private Map<Long, Long> services = null;

    //

    public Map<String, Object> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        return parameters;
    }

    public Map<Long, Object> getKeys() {
        if (keys == null) {
            keys = new HashMap<Long, Object>();
        }
        return keys;
    }

    public Map<Long, Long> getServices() {
        if (services == null) {
            services = new HashMap<Long, Long>();
        }
        return services;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return getParameters().isEmpty() && getKeys().isEmpty() && getServices().isEmpty();
    }
}
