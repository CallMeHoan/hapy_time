package com.happy_time.happy_time.service;

import com.happy_time.happy_time.ddd.auth.application.AuthApplication;
import com.happy_time.happy_time.ddd.auth.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private AuthApplication authApplication;

    @Override
    public UserDetails loadUserByUsername(String phone_number) throws UsernameNotFoundException {

        //find by phone number
        Account account = authApplication.findByPhoneNumber(phone_number);
        if (account == null) {
            throw new UsernameNotFoundException("not found");
        }
        return new User(account.getPhone_number(), account.getPassword(), new ArrayList<>());
    }
}