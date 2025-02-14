package org.minutetask.casecore.jpa.entity;

import java.time.LocalDateTime;

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
import jakarta.persistence.TableGenerator;
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
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "cc_usecase_id")
    @Column(name = "id", nullable = false)
    @ToString.Include
    @TableGenerator( //
            name = "cc_usecase_id", table = "cc_sequence", //
            pkColumnValue = "cc_usecase_id", initialValue = 1, allocationSize = 50 //
    )
    private Long id = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Lob
    @Column(name = "data", nullable = true, length = 100000)
    private String dataAsJson = null;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = null;

    @Column(name = "updated_date", nullable = true)
    private LocalDateTime updatedDate = null;

    @Column(name = "finished_date", nullable = true)
    private LocalDateTime finishedDate = null;

    @OneToMany(mappedBy = "useCase", fetch = FetchType.LAZY)
    private List<UseCaseKeyEntity> useCaseKeys = null;

    //

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final TypeReference<Map<String, Object>> DATA_REFERENCE = //
            new TypeReference<Map<String, Object>>() {
            };

    private static final String KEYS_ATTRIBUTE = "keys";
    private static final String PARAMETERS_ATTRIBUTE = "parameters";
    private static final String SERVICES_ATTRIBUTE = "services";

    //

    @Transient
    private Map<String, String> keys = new HashMap<String, String>();

    @Transient
    private Map<String, Object> parameters = new HashMap<String, Object>();

    @Transient
    private Map<Class<?>, String> services = new HashMap<Class<?>, String>();

    @PostLoad
    public void postLoad() {
        try {
            keys = new HashMap<String, String>();
            parameters = new HashMap<String, Object>();
            services = new HashMap<Class<?>, String>();
            //
            if (StringUtils.isNotEmpty(dataAsJson)) {
                Map<String, Object> dataAsMap = objectMapper.readValue(dataAsJson, DATA_REFERENCE);
                //
                @SuppressWarnings("unchecked")
                Map<String, String> tmpKeys = (Map<String, String>) dataAsMap.get(KEYS_ATTRIBUTE);
                if (MapUtils.isNotEmpty(tmpKeys)) {
                    keys.putAll(tmpKeys);
                }
                //
                @SuppressWarnings("unchecked")
                Map<String, Object> tmpParameters = (Map<String, Object>) dataAsMap.get(PARAMETERS_ATTRIBUTE);
                if (MapUtils.isNotEmpty(tmpParameters)) {
                    parameters.putAll(tmpParameters);
                }
                //
                @SuppressWarnings("unchecked")
                Map<String, String> tmpServices = (Map<String, String>) dataAsMap.get(SERVICES_ATTRIBUTE);
                if (MapUtils.isNotEmpty(tmpServices)) {
                    for (Map.Entry<String, String> entry : tmpServices.entrySet()) {
                        services.put(Class.forName(entry.getKey()), entry.getValue());
                    }
                }
            }
        } catch (JsonProcessingException | ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void applyChanges() {
        try {
            Map<String, Object> dataAsMap = new HashMap<String, Object>();
            //
            if (MapUtils.isNotEmpty(keys)) {
                dataAsMap.put(KEYS_ATTRIBUTE, new HashMap<String, String>(keys));
            }
            if (MapUtils.isNotEmpty(parameters)) {
                dataAsMap.put(PARAMETERS_ATTRIBUTE, new HashMap<String, Object>(parameters));
            }
            if (MapUtils.isNotEmpty(services)) {
                Map<String, String> tmpServices = new HashMap<String, String>();
                for (Map.Entry<Class<?>, String> entry : services.entrySet()) {
                    tmpServices.put(entry.getKey().getName(), entry.getValue());
                }
                //
                dataAsMap.put(SERVICES_ATTRIBUTE, tmpServices);
            }
            //
            if (MapUtils.isNotEmpty(dataAsMap)) {
                dataAsJson = objectMapper.writeValueAsString(dataAsMap);
            } else {
                dataAsJson = null;
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
