package org.minutetask.casecore.jpa.entity;

/*-
 * ========================LICENSE_START=================================
 * org.minutetask.casecore:casecore
 * %%
 * Copyright (C) 2024 - 2025 Jan Komrska
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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cc_usecase")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
public class UseCaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ToString.Include
    private Long id = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Lob
    @Column(name = "parameters", length = 100000)
    private String parametersAsJson = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Lob
    @Column(name = "services", length = 100000)
    private String servicesAsJson = null;

    @OneToMany(mappedBy = "useCase", fetch = FetchType.LAZY)
    private List<UseCaseKeyEntity> useCaseKeys = null;

    //

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final TypeReference<Map<String, Object>> PARAMETERS_TYPE_REFERENCE = //
            new TypeReference<Map<String, Object>>() {
            };

    private static final TypeReference<Map<String, String>> SERVICES_TYPE_REFERENCE = //
            new TypeReference<Map<String, String>>() {
            };

    //

    @Transient
    private Map<String, Object> parameters = new HashMap<String, Object>();

    @Transient
    private Map<Class<?>, String> services = new HashMap<Class<?>, String>();

    @PostLoad
    public void postLoad() {
        try {
            parameters = new HashMap<String, Object>();
            if (StringUtils.isNotEmpty(parametersAsJson)) {
                parameters.putAll(objectMapper.readValue(parametersAsJson, PARAMETERS_TYPE_REFERENCE));
            }
            //
            services = new HashMap<Class<?>, String>();
            if (StringUtils.isNotEmpty(servicesAsJson)) {
                Map<String, String> tmpServices = objectMapper.readValue(parametersAsJson, SERVICES_TYPE_REFERENCE);
                for (Map.Entry<String, String> entry : tmpServices.entrySet()) {
                    services.put(Class.forName(entry.getKey()), entry.getValue());
                }
            }
        } catch (JsonProcessingException | ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void applyChanges() {
        try {
            parametersAsJson = null;
            if (MapUtils.isNotEmpty(parameters)) {
                parametersAsJson = objectMapper.writeValueAsString(parameters);
            }
            //
            servicesAsJson = null;
            if (MapUtils.isNotEmpty(services)) {
                Map<String, String> tmpServices = new HashMap<String, String>();
                for (Map.Entry<Class<?>, String> entry : services.entrySet()) {
                    tmpServices.put(entry.getKey().getName(), entry.getValue());
                }
                //
                servicesAsJson = objectMapper.writeValueAsString(tmpServices);
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
