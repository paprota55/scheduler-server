package pl.rafalpaprota.schedulerserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.rafalpaprota.schedulerserver.dto.EditAccountDTO;
import pl.rafalpaprota.schedulerserver.dto.LogRegUserDTO;
import pl.rafalpaprota.schedulerserver.model.User;
import pl.rafalpaprota.schedulerserver.services.UserService;

@RestController
@CrossOrigin
@RequestMapping
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/myAccount")
    public ResponseEntity<?> getMyAccount() {
        User user = this.userService.getUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        LogRegUserDTO userDTO = new LogRegUserDTO();
        userDTO.setLogin(user.getLogin());
        userDTO.setEmail(user.getEmail());
        return ResponseEntity.ok(userDTO);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/api/modify/password")
    public ResponseEntity<?> updatePassword(@RequestBody final EditAccountDTO editAccountDTO) {
        final User user = this.userService.getCurrentUser();
        if (this.userService.checkPassword(editAccountDTO.getPassword(), user.getPassword())) {
            if (this.userService.changePassword(user, editAccountDTO.getNewPassword())) {
                return ResponseEntity.status(HttpStatus.OK).body("Hasło zostało zmienione.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Hasło nie zostało zmienione.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Podane hasło jest nieprawidłowe.");
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/api/modify/email")
    ResponseEntity<?> updateEmail(@RequestBody final EditAccountDTO editAccountDTO) {
        final User user = this.userService.getCurrentUser();
        if (this.userService.checkPassword(editAccountDTO.getPassword(), user.getPassword())) {
            if (this.userService.checkEmail(editAccountDTO.getNewEmail())) {
                this.userService.changeEmail(user, editAccountDTO.getNewEmail());
                return ResponseEntity.status(HttpStatus.OK).body("Email został zmieniony.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Wystąpił problem podczas zmiany adresu email.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Podane hasło jest nieprawidłowe.");
        }
    }
}
    
