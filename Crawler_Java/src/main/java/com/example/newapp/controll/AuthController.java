package com.example.newapp.controll;

import com.example.newapp.exception.TokenRefreshException;
import com.example.newapp.jwt.JwtUtils;
import com.example.newapp.model.RefreshToken;
import com.example.newapp.model.User;
import com.example.newapp.repo.UserRepository;
import com.example.newapp.request.ChangePassword;
import com.example.newapp.request.LoginRequest;
import com.example.newapp.request.RefreshTokenRequest;
import com.example.newapp.request.RegisterRequest;
import com.example.newapp.response.GetResponse;
import com.example.newapp.response.LoginResponse;
import com.example.newapp.response.RefreshTokenResponse;
import com.example.newapp.response.ResponseError;
import com.example.newapp.service.RefreshTokenServiceImpl;
import com.example.newapp.service.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    @Autowired
    AuthenticationManager manager;

    @Autowired
    JwtUtils untils;

    @Autowired
    UserRepository repo;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private RefreshTokenServiceImpl refreshService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticationUserUsingEmail(@RequestBody LoginRequest request){
        if(repo.existsByEmail(request.getEmail())){
            Authentication authentication = manager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl accDetails = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = untils.generateJwtTokenForLogin(accDetails);
            RefreshToken refreshToken = refreshService.createRefreshToken(accDetails.getId());
            LoginResponse res = new LoginResponse();
            res.setUser_role(accDetails.getUser_role());
            res.setEmail(accDetails.getEmail());
            res.setUsername(accDetails.getUsername());
            res.setToken(jwt);
            res.setRefreshToken(refreshToken.getToken());
            log.info("Login Success : " + accDetails.getEmail());
            return new ResponseEntity<>(res,HttpStatus.OK);

        }
        log.error("Account has been locked : " + request.getEmail());
        return new ResponseEntity<>(new ResponseError("801"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request){
        if(repo.existsByEmail(request.getEmail())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            User user = new User();
            user.setUser_role(request.getUser_role());
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(encoder.encode(request.getPassword()));
            User saved_user = repo.save(user);
            return new ResponseEntity<>(saved_user, HttpStatus.CREATED);
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken( @RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        return refreshService.findByToken(requestRefreshToken)
                .map(refreshService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(acc -> {
                    String token = untils.generateTokenFromEmail(acc.getEmail());
                    log.info("refresh token Success : " + token);
                    return ResponseEntity.ok(new RefreshTokenResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "810"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(userDetails);
        Long userId = userDetails.getId();
        refreshService.deleteByUserId(userId);
        log.info("Logout user Success : " + userDetails.getUsername());
        return ResponseEntity.ok("Logout Success");
    }

    @PutMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePassword request){
        Optional<User> otp = repo.findByEmail(request.getEmail());
        if(otp.isPresent()){
            User user = otp.get();
            user.setPassword(encoder.encode(request.getNew_pass()));
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }



}
