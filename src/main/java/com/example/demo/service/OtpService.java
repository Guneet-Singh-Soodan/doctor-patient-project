package com.example.demo.service;

import com.example.demo.dao.OtpRepo;
import com.example.demo.model.Otp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    @Autowired
    OtpRepo otpRepo;

    public Otp saveOtp(String email,int value){
        Otp otp=new Otp(email, value);
        otpRepo.save(otp);
        return otp;
    }

    public Otp getOtp(String email){
        Otp otp=otpRepo.findByEmail(email);
        return otp;
    }
}
