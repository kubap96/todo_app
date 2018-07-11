package com.impaqgroup.training.user.domain;

import com.impaqgroup.training.todo.domain.Todo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of="login")
@ToString
@Builder
@Entity
@Table(name = "USERS")
public class User implements Serializable{

    private static final long serialVersionUID = 6760276400659841526L;

    public enum Role {
        USER, ADMIN
    }

    @Id
    @NotNull
    private String login;

    @NotNull
    private String passwordHash;

    @NotNull
    private Role role;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "login")
    private List<Todo> todos;

    public void encodePassword(PasswordEncoder passwordEncoder) {
        passwordHash = passwordEncoder.encode(passwordHash);
    }
}
