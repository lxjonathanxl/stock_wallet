package com.shares.wallet.services;

import com.shares.wallet.dto.RegistrationRequest;
import com.shares.wallet.exceptions.DatabaseException;
import com.shares.wallet.exceptions.UsernameTakenException;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegistrationService {

    private static final Logger registrationLogger = LoggerFactory.getLogger(RegistrationService.class);
    private final UsersService usersService;

    public RegistrationService(UsersService usersService) {
        this.usersService = usersService;
    }

    public MessageController register(RegistrationRequest request) {


        Users user = new Users(request.getUsername(), request.getPassword());
        MessageController message = new MessageController();

        try {
            usersService.signUpUser(user);
        } catch (UsernameTakenException usernameError) {
            registrationLogger.error("Error while registering user with username: {}," +
                            " cause: username taken, error message: {}",
                    user.getUsername(), usernameError.getMessage(), usernameError);
            message.setMessage("Username taken");
            message.setSucceeded(false);
            return message;
        } catch (DatabaseException databaseError) {
            registrationLogger.error("Error while registering user with username: {}," +
                    " cause: unknown error referent to database, error message: {}",
                    user.getUsername(), databaseError.getMessage(), databaseError);
            message.setMessage("Error registering User, please try again");
            message.setSucceeded(false);
            return message;
        }

        registrationLogger.info("successfully registered user with username: {}",
                user.getUsername());
        message.setMessage("Registration complete");
        message.setSucceeded(true);
        return message;

    }

}
