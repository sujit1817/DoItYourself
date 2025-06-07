package com.insura.claims.controller;

import com.insura.claims.entity.Claim;
import com.insura.claims.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    @Autowired
    private ClaimService service;

    @GetMapping
    public List<Claim> getAllClaims() {
        return service.getAllClaims();
    }

    @PostMapping
    public Claim createClaim(@RequestBody Claim claim) {
        return service.createClaim(claim);
    }

    @PutMapping("/{id}/approve")
    public Claim approve(@PathVariable Long id) {
        return service.approveClaim(id);
    }

    @PutMapping("/{id}/reject")
    public Claim reject(@PathVariable Long id) {
        return service.rejectClaim(id);
    }
}
