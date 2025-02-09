package org.minutetask.casecore.jpa.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    @Lob
    @Column(name = "param_map", length = 100000)
    private String paramMapAsJson = null;

    @Lob
    @Column(name = "service_map", length = 100000)
    private String serviceMapAsJson = null;

    @OneToMany(mappedBy = "useCase", fetch = FetchType.LAZY)
    private List<UseCaseKeyEntity> useCaseKeys = null;
}
