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

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
@Table(name = "cc_usecaseaction", indexes = { //
        @Index(name = "ccix_usecaseaction_scheduler", columnList = "closed, scheduled_date", unique = false) //
})
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

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Lob
    @Column(name = "data", nullable = true, length = 100000)
    private String dataAsJson = null;

    @Column(name = "closed", nullable = false)
    private boolean closed = false;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = null;

    @Column(name = "scheduled_date", nullable = true)
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

    //

    @Getter(AccessLevel.NONE)
    @Transient
    private UseCaseActionData useCaseActionData = null;

    @PostLoad
    public void postLoad() {
        try {
            if (StringUtils.isNotEmpty(dataAsJson)) {
                setUseCaseActionData(getObjectMapper().readValue(dataAsJson, UseCaseActionData.class));
            } else {
                setUseCaseActionData(null);
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void applyChanges() {
        try {
            if (!getUseCaseActionData().isEmpty()) {
                dataAsJson = getObjectMapper().writeValueAsString(getUseCaseActionData());
            } else {
                dataAsJson = null;
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    //

    public UseCaseActionData getUseCaseActionData() {
        if (useCaseActionData == null) {
            useCaseActionData = new UseCaseActionData();
        }
        return useCaseActionData;
    }
}
