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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cc_literal", indexes = { //
        @Index(name = "ccux_literal", columnList = "value", unique = true) //
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
public class LiteralEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "cc_literal_id")
    @Column(name = "id", nullable = false)
    @ToString.Include
    @TableGenerator( //
            name = "cc_literal_id", table = "cc_sequence", //
            pkColumnValue = "cc_literal_id", initialValue = 1, allocationSize = 5 //
    )
    private Long id = null;

    @Column(name = "value", nullable = false, length = 500)
    @ToString.Include
    private String value;
}
