package com.insura.claims.service;

import com.insura.claims.entity.Claim;
import com.insura.claims.repository.ClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClaimService {
    @Autowired
    private ClaimRepository repository;

    public List<Claim> getAllClaims() {
        return repository.findAll();
    }

    public Claim createClaim(Claim claim) {
        claim.setStatus("PENDING");
        claim.setDateFiled(java.time.LocalDate.now());
        return repository.save(claim);
    }

    public Claim approveClaim(Long id) {
        Claim claim = repository.findById(id).orElseThrow();
        claim.setStatus("APPROVED");
        return repository.save(claim);
    }

    public Claim rejectClaim(Long id) {
        Claim claim = repository.findById(id).orElseThrow();
        claim.setStatus("REJECTED");
        return repository.save(claim);
    }
}
