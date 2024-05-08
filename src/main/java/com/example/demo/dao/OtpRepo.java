package com.example.demo.dao;

import com.example.demo.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepo extends JpaRepository<Otp,String> {
    Otp findByEmail(String email);
}
