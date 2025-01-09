package com.WinkProject.auth.schema;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auth {
    @Id
    @NotNull
    private Long socialId ;

    @Column
    private String profileUrl;

    @OneToMany(mappedBy = "auth", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Member> members = new ArrayList<>();
}
