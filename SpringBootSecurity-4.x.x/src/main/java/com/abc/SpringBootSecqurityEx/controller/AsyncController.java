package com.abc.SpringBootSecqurityEx.controller;

import com.abc.SpringBootSecqurityEx.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/test")
@Profile("dev")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AsyncController {

    @Autowired
    private TestService emailService;




    @GetMapping("/sendMail")
    public ResponseEntity<String> sendEmail() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            emailService.sendDailyReportEmail(i + "");
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        return ResponseEntity.ok("Emails sent successfully in " + executionTime + " ms");
    }

}
