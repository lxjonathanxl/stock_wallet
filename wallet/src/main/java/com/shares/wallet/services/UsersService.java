package com.shares.wallet.services;

import com.shares.wallet.exceptions.AlterUserStockException;
import com.shares.wallet.exceptions.DatabaseException;
import com.shares.wallet.exceptions.ServerErrorException;
import com.shares.wallet.exceptions.UsernameTakenException;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.model.Users;
import com.shares.wallet.repo.UsersRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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

    private final static Logger userServiceLogger = LoggerFactory.getLogger(UsersService.class);

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
            userServiceLogger
                    .error("Error while calling usersRepo.save() method with user with username {}",
                            user.getUsername(), e);
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
            userServiceLogger.error("while trying to change user username," +
                    " with original username: {} user was not found on database", username);
            message.setMessage("Server error: unable to find user");
            message.setSucceeded(false);
            return message;
        }

        if (!confirmPassword(username, password))
        {
            userServiceLogger.error("while trying to change user username," +
                    " with original username: {} received password does not match with user password saved on database ",
                    username);
            message.setMessage("wrong password!!");
            message.setSucceeded(false);
            return message;
        }

        boolean newUsernameIsTaken = usersRepo.findByUsername(newUsername)
                .isPresent();

        if (newUsernameIsTaken) {
            userServiceLogger.error("while trying to change user username," +
                            " with original username: {} new username was found on database, making it unavailable",
                    username);
            message.setMessage("username unavailable");
            message.setSucceeded(false);
            return message;
        }

        Long userId = usersRepo.findUserId(username);

        try {
            usersRepo.changeUsername(newUsername, userId);
        } catch (RuntimeException dataError) {
            userServiceLogger
                    .error("Error while calling usersRepo.changeUsername(newUsername, userId) method " +
                                    "with new username: {} and userId: {}",
                            newUsername, userId, dataError);
            message.setMessage("server error: unable to change username");
            message.setSucceeded(false);
            return message;
        }

        userServiceLogger.info("user with id: {} username changed from: {} to: {}",
                userId ,username, newUsername);
        message.setMessage("Username changed");
        message.setSucceeded(true);
        return message;
    }

    public MessageController changePassword(String username, String oldPassword, String newPassword) {

        MessageController message = new MessageController();

        boolean userExists = usersRepo.findByUsername(username)
                .isPresent();

        if (!userExists) {
            userServiceLogger.error("while trying to change user password," +
                    " with username: {} user was not found on database", username);
            message.setMessage("Server error: unable to find user");
            message.setSucceeded(false);
            return message;
        }

        if (!confirmPassword(username, oldPassword))
        {
            userServiceLogger.error("while trying to change user password," +
                            " with username: {} original password received does not match with user password saved on database ",
                    username);
            message.setMessage("wrong password!!");
            message.setSucceeded(false);
            return message;
        }

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

        if (!newPassword.matches(regex))
        {
            userServiceLogger.error("while trying to change user password," +
                            " with username: {} new password received does not match with regex criteria",
                    username);
            message.setMessage("new password is weak");
            message.setSucceeded(false);
            return message;
        }

        Long userId = usersRepo.findUserId(username);
        String encodedPassword = bCryptPasswordEncoder.encode(newPassword);

        try {
            usersRepo.changePassword(encodedPassword, userId);
        } catch (RuntimeException dataError) {
            userServiceLogger
                    .error("Error while calling usersRepo.changePassword(encodedPassword, userId) method " +
                                    "with userId: {}",
                            userId, dataError);
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
            userServiceLogger.error("while trying to look into user cash," +
                    " with username: {} user was not found on database", username);
            throw new ServerErrorException("Server Error: unable to found user by username retrieve with principal");
        }

        return usersRepo.findUserCash(username);
    }

    public int updateCash(String username, BigDecimal cash) {

        boolean userExists = usersRepo.findByUsername(username)
                .isPresent();

        if (!userExists) {
            userServiceLogger.error("while trying to update user cash," +
                    " with username: {} user was not found on database", username);
            throw new ServerErrorException("Server Error: unable to found user by username retrieve with principal");
        }

        return usersRepo.changeCash(cash, username);
    }

    public String changeCashUserProfile(
            String username, String password, BigDecimal cashToAdd) {

        String message;

        if (!confirmPassword(username, password)) {
            userServiceLogger.error("while trying to add cash to users profile," +
                    " with username: {} received password doesn't match with password saved on database",
                    username);
            return message ="wrong password";
        }

        try {
            BigDecimal userWallet = lookIntoCash(username);
            userWallet = userWallet.add(cashToAdd);
            updateCash(username, userWallet);
        } catch (DataAccessException dataError) {
            userServiceLogger.error("while trying to add cash to users profile," +
                            " error referent to database either lookIntoCash() method or updateCash()", dataError);
            return message = "server error: handling users wallet";
        } catch (ServerErrorException serverError) {
            userServiceLogger.error("while trying to add cash to users profile," +
                    " error referent to database, user probably wasn't found on database", serverError);
            return message = "server error: handling user in database";
        }

        userServiceLogger.info("{} cash was added to user wallet with username: {} ", cashToAdd, username);
        return message = "cash added to wallet";
    }


}
