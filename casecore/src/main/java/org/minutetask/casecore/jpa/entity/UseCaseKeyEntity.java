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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cc_usecasekey", indexes = { //
        @Index(name = "ccux_usecasekey", columnList = "type_id, value", unique = true) //
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
public class UseCaseKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "cc_usecasekey_id")
    @Column(name = "id", nullable = false)
    @ToString.Include
    @TableGenerator( //
            name = "cc_usecasekey_id", table = "cc_sequence", //
            pkColumnValue = "cc_usecasekey_id", initialValue = 1, allocationSize = 50 //
    )
    private Long id = null;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usecase_id", nullable = false)
    private UseCaseEntity useCase = null;

    @Column(name = "type_id", nullable = false)
    @ToString.Include
    private Long typeId;

    @Column(name = "value", nullable = false, length = 50)
    @ToString.Include
    private String value;
}
