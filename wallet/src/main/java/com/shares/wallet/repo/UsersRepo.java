package com.shares.wallet.repo;

import com.shares.wallet.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UsersRepo extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);

    @Query("SELECT u.cash FROM Users u WHERE u.username = :username")
    BigDecimal findUserCash(@Param("username") String username);

    @Query("SELECT u.id FROM Users u WHERE u.username = :username")
    Long findUserId(@Param("username") String username);

    @Query("SELECT u.password FROM Users u WHERE u.username = :username")
    String findUserPassword(@Param("username") String username);

    @Query("SELECT u.username FROM Users u WHERE u.id = :id")
    String findUserUsername(@Param("id") Long id);

    @Query("SELECT u.email FROM Users u WHERE u.username = :username")
    String findUserEmail(@Param("username") String username);


    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.cash = :cash WHERE u.username = :username")
    int changeCash(@Param("cash") BigDecimal cash, @Param("username") String username);

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.username = :username WHERE u.id = :id")
    int changeUsername(@Param("username") String username, @Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.password = :password WHERE u.id = :id")
    int changePassword(@Param("password") String password, @Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.email = :email WHERE u.username = :username")
    int changeEmail(@Param("email") String email, @Param("username") String username);

}
