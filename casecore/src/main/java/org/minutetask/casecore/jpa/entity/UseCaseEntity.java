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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.CascadeType;
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
            pkColumnValue = "cc_usecase_id", initialValue = 0, allocationSize = 50 //
    )
    private Long id = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Lob
    @Column(name = "data", nullable = true, length = 100000)
    private String dataAsJson = null;

    @Column(name = "closed", nullable = false)
    private boolean closed = false;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = null;

    @Column(name = "updated_date", nullable = true)
    private LocalDateTime updatedDate = null;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "useCase", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<UseCaseKeyEntity> useCaseKeys = null;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "useCase", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<UseCaseActionEntity> useCaseActions = null;

    //

    @Setter
    private static ObjectMapper objectMapper = null;

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }

    //

    @Getter(AccessLevel.NONE)
    @Transient
    private UseCaseData useCaseData = new UseCaseData();

    @Transient
    private Object source = null;

    @PostLoad
    public void postLoad() {
        try {
            if (StringUtils.isNotEmpty(dataAsJson)) {
                useCaseData = getObjectMapper().readValue(dataAsJson, UseCaseData.class);
            } else {
                useCaseData = new UseCaseData();
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void applyChanges() {
        try {
            if ((useCaseData != null) && useCaseData.isNotEmpty()) {
                dataAsJson = getObjectMapper().writeValueAsString(useCaseData);
            } else {
                dataAsJson = null;
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    //

    public List<UseCaseKeyEntity> getUseCaseKeys() {
        if (useCaseKeys == null) {
            useCaseKeys = new ArrayList<UseCaseKeyEntity>();
        }
        return useCaseKeys;
    }

    public List<UseCaseActionEntity> getUseCaseActions() {
        if (useCaseActions == null) {
            useCaseActions = new ArrayList<UseCaseActionEntity>();
        }
        return useCaseActions;
    }

    //

    public UseCaseData getUseCaseData() {
        if (useCaseData == null) {
            useCaseData = new UseCaseData();
        }
        return useCaseData;
    }
}
