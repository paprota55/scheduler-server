package pl.rafalpaprota.schedulerserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.rafalpaprota.schedulerserver.dto.AuthenticationResponse;
import pl.rafalpaprota.schedulerserver.dto.LogRegUserDTO;
import pl.rafalpaprota.schedulerserver.model.Settings;
import pl.rafalpaprota.schedulerserver.model.User;
import pl.rafalpaprota.schedulerserver.services.EventService;
import pl.rafalpaprota.schedulerserver.services.SettingsService;
import pl.rafalpaprota.schedulerserver.services.UserService;
import pl.rafalpaprota.schedulerserver.util.JwtUtil;

@RestController
@CrossOrigin
@RequestMapping
public class AccountController {

    private final UserService userService;

    private final SettingsService settingsService;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    private final EventService eventService;

    @Autowired
    public AccountController(UserService userService, SettingsService settingsService, JwtUtil jwtUtil, AuthenticationManager authenticationManager, EventService eventService) {
        this.userService = userService;
        this.settingsService = settingsService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.eventService = eventService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "login")
    public ResponseEntity<?> login(@RequestBody final LogRegUserDTO userDTO) throws Exception {

        System.out.println(userDTO.getLogin() + " " + userDTO.getPassword());
        final User user = this.userService.getUserByLogin(userDTO.getLogin());

        final Authentication authentication;

        try {
            authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDTO.getLogin(), userDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (final BadCredentialsException e) {
            throw new Exception("Bad login creds", e);
        }

        final String token = this.jwtUtil.generateToken(authentication);

        final String role;

        this.eventService.moveEventsToExpiredEventsWhenReachArchiveTime();
        role = "";

        return ResponseEntity.ok(new AuthenticationResponse(token, role));
    }

    @RequestMapping(method = RequestMethod.POST, value = "register")
    public ResponseEntity<?> register(@RequestBody final LogRegUserDTO userDTO) {
        User user = this.userService.getUserByLogin(userDTO.getLogin());

        if (user != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Użytkownik o podanym loginie już istnieje");
        } else {
            User newUser = this.userService.addUserByUserDTO(userDTO);
            Settings settings = this.settingsService.createNewSettings(newUser);
            return ResponseEntity.ok("Konto zostało utworzone");
        }
    }
}
