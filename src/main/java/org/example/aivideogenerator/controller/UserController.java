package org.example.aivideogenerator.controller;

import org.example.aivideogenerator.DTO.AuthRequest;
import org.example.aivideogenerator.DTO.UserDTO;
import org.example.aivideogenerator.securityconfig.JwtService;
import org.example.aivideogenerator.model.User;
import org.example.aivideogenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {


    @Autowired
    UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;


    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    /*
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserByUserIdOldVersion(@PathVariable int userId){
        return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
    }*/

    //følgende metode gør så man kun kan få ens egen profil
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserByAuthUserId(@PathVariable int userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User loggedInUser = userService.getUserByUsername(username);

        if (loggedInUser.getId() != userId) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        }

        return new ResponseEntity<>(loggedInUser, HttpStatus.OK);
    }


    //følgende metode bruges ikke
    /*
    @PostMapping("/users/login")
    public ResponseEntity<String> doLogin(@RequestBody AuthRequest request) {
        System.out.println("1");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        System.out.println("2");
        if (authentication.isAuthenticated()) {
            System.out.println("3");
            //return JwtResponseDTO.builder() //
            // .accessToken(jwtService.GenerateToken(authRequestDTO.getUsername()).build();
            String token = jwtService.generateToken(authentication.getName());
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + token)
                    .body("Du er logged in");
        }
        else {
            System.out.println("kunne ikke logge ind");
            throw new UsernameNotFoundException("invalid user request..!!");
        }
    } */

    @PostMapping("/users/login")
    public ResponseEntity<String> doLogintest(@RequestBody AuthRequest customer) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(customer.username(), customer.password()));
        if(authentication.isAuthenticated()){
            //return JwtResponseDTO.builder()
            //        .accessToken(jwtService.GenerateToken(authRequestDTO.getUsername()).build();
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Du er logget på");
        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }
    }



    @PostMapping("/users/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO newUser) {
        User isUsernameFree = userService.getUserByUsername(newUser.username()); //checker om username er free
        if(isUsernameFree != null){
            return new ResponseEntity<>(HttpStatus.CONFLICT); //hvis den ikke er fri sender den 409, som bliver fanget i frontend
        }

        User user = new User();
        User savedUser;
        ResponseEntity response = null;
        try {
            String hashPwd = passwordEncoder.encode(newUser.password());
            user.setName(newUser.name());
            user.setUsername(newUser.username());
            user.setPassword(hashPwd);
            user.setRole("ROLE_USER");


            savedUser = userService.addUser(user);
            if (savedUser.getId() > 0) {
                response = ResponseEntity.status(HttpStatus.CREATED)
                        .body("Given user details are successfully registrered");
            }
        } catch (Exception ex) {
            response = ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An exception occured due to" + ex.getMessage());
        }
        return response;
    }


    //bruges ikke
    @PostMapping("/admin/users")
    public ResponseEntity<User>addUser(@RequestBody UserDTO userDTO){
        User user = new User();
        user.setName(userDTO.name());
        user.setUsername(userDTO.username());
        user.setPassword(userDTO.password());
        user.setRole(userDTO.role());

        return new ResponseEntity<>(userService.addUser(user), HttpStatus.CREATED);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String>deleteUser(@PathVariable int userId){
        User user = userService.getUser(userId);
        if(user != null){
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<String>updateUser(@PathVariable int userId, @RequestBody UserDTO userDTO){
        User checkUserId = userService.getUser(userId);
        if(checkUserId != null){
            User user = new User();

            user.setId(userId);
            user.setName(userDTO.name());
            user.setUsername(userDTO.username());
            user.setPassword(userDTO.password());
            user.setRole(userDTO.role());

            userService.updateUser(user);
            return ResponseEntity.ok("user has updated");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
