package com.shares.wallet.services;

import com.shares.wallet.dto.RegistrationRequest;
import com.shares.wallet.exceptions.DatabaseException;
import com.shares.wallet.exceptions.UsernameTakenException;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.model.Users;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegistrationService {

    private final UsersService usersService;

    public RegistrationService(UsersService usersService) {
        this.usersService = usersService;
    }

    public MessageController register(RegistrationRequest request) {


        Users user = new Users(request.getUsername(), request.getPassword());
        MessageController message = new MessageController();

        try {
            usersService.signUpUser(user);
        } catch (UsernameTakenException e) {
            //TODO logging
            message.setMessage("Username taken");
            message.setSucceeded(false);
            return message;
        } catch (DatabaseException error) {
            //TODO logging
            message.setMessage("Error registering User, please try again");
            message.setSucceeded(false);
            return message;
        }

        message.setMessage("Registration complete");
        message.setSucceeded(true);
        return message;

    }

}
