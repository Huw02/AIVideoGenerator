package org.example.aivideogenerator.controller;

import org.example.aivideogenerator.DTO.UserDTO;
import org.example.aivideogenerator.model.User;
import org.example.aivideogenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {


    @Autowired
    UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserByUserId(@PathVariable int userId){
        return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
    }


    @PostMapping("/users")
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
