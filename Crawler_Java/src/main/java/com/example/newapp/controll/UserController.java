package com.example.newapp.controll;

import com.example.newapp.model.User;
import com.example.newapp.repo.UserRepository;
import com.example.newapp.response.ResponseError;
import com.example.newapp.service.MailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class UserController {

    @Autowired
    UserRepository repo;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    MailServiceImpl service;

    @GetMapping("/get-all-users")
    public ResponseEntity<?> getAllUsers(){
        List<User> listUsers = repo.findAll();
        log.info("get all user successfully");
        return new ResponseEntity<>(listUsers,HttpStatus.OK);
    }

    @GetMapping("/get-user-info/{id}")
    public ResponseEntity<?> getUserInfo(@PathVariable("id") Long id){
        Optional<User> otp = repo.findById(id);
        if(otp.isPresent()){
            User user = otp.get();
            log.info("get user information with user email: " + user.getEmail());
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        ResponseError error = new ResponseError("Id not found");
        log.error("get user information fail with id: " + id);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id){
        Optional<User> otp = repo.findById(id);
        if(otp.isPresent()){
            User user = otp.get();
            repo.delete(user);
            log.info("delete user information with user email: " + user.getEmail());
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        ResponseError error = new ResponseError("Id not found");
        log.error("delete user information fail with id: " + id);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
