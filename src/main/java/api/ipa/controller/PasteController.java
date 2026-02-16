package api.ipa.controller;

import api.ipa.dto.PasteRequest;
import api.ipa.dto.PasteResponse;
import api.ipa.facade.PasteFacade;
import api.ipa.service.PasteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/paste")
public class PasteController {

    private final PasteFacade pasteFacade;

    @RequestMapping("/new")
    public ResponseEntity<?> createPaste(@RequestBody PasteRequest request){
        String createdPaste = pasteFacade.createPaste(request);
        return new ResponseEntity<>(createdPaste, HttpStatus.OK);
    }

    @RequestMapping("/{url}")
    public ResponseEntity<?> getPaste(@PathVariable String url){
        PasteResponse response = pasteFacade.getPaste(url);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
