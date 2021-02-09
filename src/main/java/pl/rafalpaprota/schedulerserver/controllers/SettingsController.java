package pl.rafalpaprota.schedulerserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.rafalpaprota.schedulerserver.dto.SettingsDTO;
import pl.rafalpaprota.schedulerserver.model.Settings;
import pl.rafalpaprota.schedulerserver.services.SettingsService;
import pl.rafalpaprota.schedulerserver.services.UserService;

@RestController
@CrossOrigin
@RequestMapping
public class SettingsController {

    private final SettingsService settingsService;
    private final UserService userService;

    @Autowired
    public SettingsController(SettingsService settingsService, UserService userService) {
        this.settingsService = settingsService;
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/settings/getSettings")
    public ResponseEntity<?> getMySettings() {
        SettingsDTO settingsDTO = this.settingsService.getSettings();
        if (settingsDTO != null) {
            return ResponseEntity.ok(settingsDTO);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie posiadasz ustawień");
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/settings/getArchiveTime")
    public ResponseEntity<?> getMyArchiveTime() {
        SettingsDTO settingsDTO = this.settingsService.getSettings();
        if (settingsDTO != null) {
            return ResponseEntity.ok(settingsDTO.getNewTime());
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie posiadasz ustawień");
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "api/modify/archiveTime")
    public ResponseEntity<?> changeSettings(@RequestBody SettingsDTO settingsDTO) {
        Settings settings = this.settingsService.getCurrentUserSettings();
        if (settings != null) {
            if (settingsDTO != null) {
                if (this.userService.checkPassword(settingsDTO.getPassword(), this.userService.getCurrentUser().getPassword())) {
                    this.settingsService.changeSettings(settingsDTO, settings);
                    return ResponseEntity.ok("ok");
                } else {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Hasło jest nieprawidłowe.");
                }

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nie podałeś ustawień.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie możemy znaleźć twoich ustawień.");
        }
    }
}
