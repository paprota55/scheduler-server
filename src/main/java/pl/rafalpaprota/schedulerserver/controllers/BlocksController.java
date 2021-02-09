package pl.rafalpaprota.schedulerserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.rafalpaprota.schedulerserver.dto.BlockDTO;
import pl.rafalpaprota.schedulerserver.model.User;
import pl.rafalpaprota.schedulerserver.services.BlockService;
import pl.rafalpaprota.schedulerserver.services.UserService;

@RestController
public class BlocksController {

    private final BlockService blockService;
    private final UserService userService;

    @Autowired
    public BlocksController(BlockService blockService, UserService userService) {
        this.blockService = blockService;
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "api/blocks/addBlock")
    public ResponseEntity<?> addBlock(@RequestBody BlockDTO blockDTO) {
        User user = this.userService.getCurrentUser();
        this.blockService.setStartDayInBlockDTO(blockDTO);
        if (this.blockService.checkDatesCorrectness(blockDTO.getDateFrom(), blockDTO.getDateTo())) {
            if (!this.blockService.checkBlockExist(blockDTO.getBlockName(), user)) {
                if (!this.blockService.checkIfBlockBetweenDatesExist(blockDTO, user)) {
                    this.blockService.addBlockToDB(blockDTO, user);
                    return ResponseEntity.ok("Blok został dodany.");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Posiadasz już blok w tych datach");
                }
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Blok o podanej nazwie już istnieje.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Daty są niepoprawne.");
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "api/blocks/deleteBlock/{blockName}")
    public ResponseEntity<?> deleteBlock(@PathVariable final String blockName) {
        User user = this.userService.getCurrentUser();
        if (this.blockService.checkBlockExist(blockName, user)) {
            this.blockService.deleteBlockFromDB(blockName, user);
            return ResponseEntity.ok("Blok został usunięty.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie posiadasz bloku o takiej nazwie.");
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "api/blocks/modifyBlock")
    public ResponseEntity<?> updateBlock(@RequestBody BlockDTO blockDTO) {
        User user = this.userService.getCurrentUser();
        if (this.blockService.checkBlockExist(blockDTO.getBlockName(), user)) {
            if (blockDTO.getDateFrom() != null && blockDTO.getDateTo() != null) {
                this.blockService.setStartDayInBlockDTO(blockDTO);
                if (!this.blockService.checkIfBlockBetweenDatesExist(blockDTO, user)) {
                    if (this.blockService.checkDatesCorrectness(blockDTO.getDateFrom(), blockDTO.getDateTo())) {
                        this.blockService.modifyBlockInDB(blockDTO, user);
                        return ResponseEntity.ok("Blok został zmieniony.");
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Zła kolejność dat.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Posiadasz już blok w tych datach");
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie podałeś którejś z dat.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie posiadasz bloku o takiej nazwie.");
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/blocks/getBlocks")
    public ResponseEntity<?> getMyBlocks() {
        return ResponseEntity.ok(this.blockService.getCurrentUserBlocks());
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/blocks/getBlocksToScheduler")
    public ResponseEntity<?> getMyBlocksToScheduler() {
        return ResponseEntity.ok(this.blockService.getCurrentUserBlocksToScheduler());
    }
}