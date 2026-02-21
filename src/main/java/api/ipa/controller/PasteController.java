package api.ipa.controller;

import api.ipa.dto.PasteRequest;
import api.ipa.dto.PasteResponse;
import api.ipa.entity.User;
import api.ipa.facade.PasteFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/paste")
public class PasteController {

    private final PasteFacade pasteFacade;

    @PostMapping("/new")
    public ResponseEntity<?> createPaste(@RequestBody PasteRequest request,
                                         @AuthenticationPrincipal User user){
        String createdPaste = pasteFacade.createPaste(request, user);
        return new ResponseEntity<>(createdPaste, HttpStatus.OK);
    }

    @GetMapping("/{url}")
    public ResponseEntity<?> getPaste(@PathVariable String url,
                                      @AuthenticationPrincipal User user){
        PasteResponse response = pasteFacade.getPaste(url, user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{url}")
    public ResponseEntity<?> deletePaste(@PathVariable String url,
                                         @AuthenticationPrincipal User user){
        String answer;
        if(pasteFacade.deletePaste(url, user)){
            answer = "Paste with url: " + url + " was successfully deleted";
        }else{
            answer = "Paste with url: " + url + " was not deleted";
        }
        return new ResponseEntity<>(answer, HttpStatus.OK);
    }

}
