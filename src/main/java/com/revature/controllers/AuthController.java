package com.revature.controllers;

import com.revature.dtos.LoginRequest;
import com.revature.dtos.RegisterRequest;
import com.revature.dtos.ResetRequest;
import com.revature.models.User;
import com.revature.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "https://production.dcs2jw2334qwo.amplifyapp.com/", allowCredentials = "true")
public class AuthController{

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    /**
     * A route to request a password reset email be sent
     *
     * @param requestDTO A DTO ResetRequest that contains the email of the account to password reset
     * @return true - if the password reset request was sent. Do not use in the front end (for testing)
     */
    @PostMapping("/reset")
    public void passwordResetRequest(@RequestBody ResetRequest requestDTO){
        authService.forgotPassword(requestDTO.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest loginRequest, HttpSession session){
        Optional<User> optional = authService.findByCredentials(loginRequest.getEmail(), loginRequest.getPassword());

        if (!optional.isPresent()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        session.setAttribute("user", optional.get());

        return ResponseEntity.status(HttpStatus.OK).body(optional.get());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session){
        session.removeAttribute("user");

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest registerRequest){
        User created = new User(0,
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                ""
        );

        if(authService.findByEmail(created.getEmail()).isPresent()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(created);

        created = authService.register(created);
        if (created.getId() > 0) return ResponseEntity.status(HttpStatus.CREATED).body(created);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(created);
    }

    @PatchMapping("/users/{userId}")
    public Object responseEntity(@RequestBody String password, @PathVariable("userId") int id){
        Optional<User> possibleUser = authService.findByUserId(id);
        if (!possibleUser.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        User user = possibleUser.get();

        user.setPassword(password);
        user.encryptAndSetPassword();
        return authService.register(user);
    }
}
