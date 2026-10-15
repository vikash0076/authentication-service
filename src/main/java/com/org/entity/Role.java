package com.org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@Audited
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
public class Role extends AuthEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private Boolean active;

}
