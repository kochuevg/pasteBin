package api.ipa.controller;

import api.ipa.dto.PastePreview;
import api.ipa.dto.PasteRequest;
import api.ipa.dto.PasteResponse;
import api.ipa.entity.User;
import api.ipa.facade.PasteFacade;
import api.ipa.service.RedisService;
import api.ipa.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/paste")
public class PasteController {

    private final PasteFacade pasteFacade;

    private final RedisService redisService;

    @PostMapping("/new")
    public ResponseEntity<?> createPaste(@RequestBody PasteRequest request,
                                         @AuthenticationPrincipal User user){
        String createdPaste = pasteFacade.createPaste(request, user);
        return new ResponseEntity<>(createdPaste, HttpStatus.OK);
    }

    @GetMapping("/{url}")
    public ResponseEntity<?> getPaste(@PathVariable String url,
                                      HttpServletRequest request,
                                      @AuthenticationPrincipal User user){
        String realIp = RequestUtils.extractRealIp(request);
        String userAgent = request.getHeader("User-Agent");

        PasteResponse response = pasteFacade.getPaste(url, user, realIp, userAgent);
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

    @GetMapping("/feed")
    public ResponseEntity<List<PastePreview>> getPublicFeed(){
        List<PastePreview> feed = redisService.getPublicFeed();

        return new ResponseEntity<>(feed, HttpStatus.OK);
    }

}
