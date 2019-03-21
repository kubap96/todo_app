package com.training.user.persistance;

import com.training.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Transactional
    @Modifying
    @Query("Update User u SET u.passwordHash=:passwordHash WHERE u.login=:login")
    void updatePassword(@Param("login") String login,
                        @Param("passwordHash") String passwordHash);

    @Transactional
    @Modifying
    @Query("Update User u SET u.role=:role WHERE u.login=:login")
    void updateRole(@Param("login") String login,
                    @Param("role") User.Role role);

}
