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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
@Table(name = "cc_usecaseaction")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
public class UseCaseActionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "cc_usecaseaction_id")
    @Column(name = "id", nullable = false)
    @ToString.Include
    @TableGenerator( //
            name = "cc_usecaseaction_id", table = "cc_sequence", //
            pkColumnValue = "cc_usecaseaction_id", initialValue = 0, allocationSize = 50 //
    )
    private Long id = null;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usecase_id", nullable = false)
    private UseCaseEntity useCase = null;

    @Column(name = "method_id", nullable = false)
    @ToString.Include
    private Long methodId;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Lob
    @Column(name = "data", nullable = true, length = 100000)
    private String dataAsJson = null;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = null;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate = null;

    //

    @Setter
    private static ObjectMapper objectMapper = null;

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }

    private static final TypeReference<Map<String, Object>> DATA_REFERENCE = //
            new TypeReference<Map<String, Object>>() {
            };

    private static final String PARAMETERS_ATTRIBUTE = "parameters";

    //

    @Getter(AccessLevel.NONE)
    @Transient
    private List<Object> parameters = new ArrayList<Object>();

    //

    public List<Object> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<Object>();
        }
        return parameters;
    }
}
