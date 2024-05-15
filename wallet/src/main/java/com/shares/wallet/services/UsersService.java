package com.shares.wallet.services;

import com.shares.wallet.exceptions.AlterUserStockException;
import com.shares.wallet.exceptions.DatabaseException;
import com.shares.wallet.exceptions.ServerErrorException;
import com.shares.wallet.exceptions.UsernameTakenException;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.model.Users;
import com.shares.wallet.repo.UsersRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class UsersService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG =
            "user with username %s not found";

    private final UsersRepo usersRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UsersService(UsersRepo usersRepo, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.usersRepo = usersRepo;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usersRepo.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND_MSG, username)));
    }

    public boolean signUpUser(Users user) {

        boolean userExists = usersRepo.findByUsername(user.getUsername())
                .isPresent();

        if (userExists) {
            throw new UsernameTakenException("username already taken");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);

        try {
            usersRepo.save(user);
        } catch (RuntimeException e) {
            //TODO logging
            throw new DatabaseException("Server Error: saving user");
        }

        return true;
    }

    public boolean confirmPassword(String username, String password) {

        String userPassword = usersRepo.findUserPassword(username);
        return bCryptPasswordEncoder.matches(password, userPassword);

    }

    public MessageController changeUsername(String username, String password, String newUsername) {

        MessageController message = new MessageController();

        boolean userExists = usersRepo.findByUsername(username)
                .isPresent();

        if (!userExists) {
            message.setMessage("Server error: unable to find user");
            message.setSucceeded(false);
            return message;
        }

        if (!confirmPassword(username, password))
        {
            message.setMessage("wrong password!!");
            message.setSucceeded(false);
            return message;
        }

        boolean newUsernameIsTaken = usersRepo.findByUsername(newUsername)
                .isPresent();

        if (newUsernameIsTaken) {
            message.setMessage("username unavailable");
            message.setSucceeded(false);
            return message;
        }

        Long userId = usersRepo.findUserId(username);

        try {
            usersRepo.changeUsername(newUsername, userId);
        } catch (RuntimeException dataError) {
            //TODO logging
            message.setMessage("server error: unable to change username");
            message.setSucceeded(false);
            return message;
        }

        message.setMessage("Username changed");
        message.setSucceeded(true);
        return message;
    }

    public MessageController changePassword(String username, String oldPassword, String newPassword) {

        MessageController message = new MessageController();

        boolean userExists = usersRepo.findByUsername(username)
                .isPresent();

        if (!userExists) {
            message.setMessage("Server error: unable to find user");
            message.setSucceeded(false);
            return message;
        }

        if (!confirmPassword(username, oldPassword))
        {
            message.setMessage("wrong password!!");
            message.setSucceeded(false);
            return message;
        }

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

        if (!newPassword.matches(regex))
        {
            message.setMessage("new password is weak");
            message.setSucceeded(false);
            return message;
        }

        Long userId = usersRepo.findUserId(username);
        String encodedPassword = bCryptPasswordEncoder.encode(newPassword);

        try {
            usersRepo.changePassword(encodedPassword, userId);
        } catch (RuntimeException dataError) {
            //TODO logging
            message.setMessage("server error: unable to change password");
            message.setSucceeded(false);
            return message;
        }

        message.setMessage("Password changed");
        message.setSucceeded(true);
        return message;
    }

    public Users findUser(String username) {
        return usersRepo.findByUsername(username).orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND_MSG, username)));
    }


    public BigDecimal lookIntoCash(String username) {

        boolean userExists = usersRepo.findByUsername(username)
                .isPresent();

        if (!userExists) {
            throw new ServerErrorException("Server Error: unable to found user by username retrieve with principal");
        }

        return usersRepo.findUserCash(username);
    }

    public int updateCash(String username, BigDecimal cash) {

        boolean userExists = usersRepo.findByUsername(username)
                .isPresent();

        if (!userExists) {
            throw new ServerErrorException("Server Error: unable to found user by username retrieve with principal");
        }

        return usersRepo.changeCash(cash, username);
    }


}
