//hello, world
//hello, world
//hello, world
//branch-3 1
//branch-4 1
//branch-5 2
package com.example.demo.controller;
//branch-6 1
import com.example.demo.model.*;
import com.example.demo.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

@Slf4j
@Controller
public class DemoController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PatientService patientService;

    @Autowired
    DoctorService doctorService;

    @Autowired
    UserService userService;

    @Autowired
    EmailService emailService;

    @Autowired
    OtpService otpService;

    @Autowired
    JwtService jwtService;

    @Autowired
    HttpSessionSecurityContextRepository securityContextRepository;

    @GetMapping(value="/register")
    String register(HttpServletResponse response,Model model,@ModelAttribute("userAlreadyExists")String userAlreadyExists){
        Patient patientTemp=new Patient();
        patientTemp.setEmail("gssoodan3281@gmail.com_PATIENT");
        patientTemp.setPassword("1234");
        patientTemp.setName("guneet_pat");
        patientService.savePatient(patientTemp);
        Doctor doctorTemp=new Doctor();
        doctorTemp.setEmail("gssoodan3281@gmail.com_DOCTOR");
        doctorTemp.setPassword("1234");
        doctorTemp.setName("guneet_doc");
        doctorService.saveDoctor(doctorTemp);
        Patient patientTemp1=new Patient();
        patientTemp1.setEmail("rksoodan3281@gmail.com_PATIENT");
        patientTemp1.setPassword("1234");
        patientTemp1.setName("rks_pat");
        patientService.savePatient(patientTemp1);
        Doctor doctorTemp1=new Doctor();
        doctorTemp1.setEmail("rksoodan3281@gmail.com_DOCTOR");
        doctorTemp1.setPassword("1234");
        doctorTemp1.setName("rks_doc");
        doctorService.saveDoctor(doctorTemp1);

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        if(userAlreadyExists.equals("true")) model.addAttribute("userAlreadyExists",true);
        else model.addAttribute("userAlreadyExists",false);
        return "register";
    }

    @PostMapping(value="/register")
    RedirectView register(User user, @RequestParam("role")String role, HttpServletResponse response, RedirectAttributes redirectAttributes){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String userEmail=user.getEmail();
        if(!userService.ifUserExists(userEmail+"_"+role)) {
            if(role.equals("DOCTOR")){
                Doctor doctor = new Doctor();
                doctor.setEmail(userEmail+"_"+role);
                doctor.setName(user.getName());
                doctor.setPassword(user.getPassword());
                doctorService.saveDoctor(doctor);
            } else {
                Patient patient = new Patient();
                patient.setEmail(userEmail+"_"+role);
                patient.setName(user.getName());
                patient.setPassword(user.getPassword());
                patientService.savePatient(patient);
            }
            log.info("New {} account created for email: {}, name: {}",role,userEmail,user.getName());
            return new RedirectView("/login");
        }
        else {
            redirectAttributes.addFlashAttribute("userAlreadyExists", true);
            return new RedirectView("/register");
        }
    }

    @GetMapping(value="/login")
    String login( HttpServletResponse response,Model model,@ModelAttribute("incorrectDetails")String incorrectDetails){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        if(incorrectDetails.equals("true")) model.addAttribute("incorrectDetails",true);
        else model.addAttribute("incorrectDetails",false);
        return "login";
    }


    @PostMapping(value="/login")
    RedirectView login(User user, @RequestParam("role")String role, Model model, HttpServletResponse response, HttpServletRequest request,RedirectAttributes redirectAttributes){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String userEmail=user.getEmail();
        try {
                Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(userEmail + "_" + role, user.getPassword());
                Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);
                authenticationResponse.isAuthenticated();
                SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
                SecurityContext context = securityContextHolderStrategy.createEmptyContext();
                context.setAuthentication(authenticationResponse);
                securityContextHolderStrategy.setContext(context);
                securityContextRepository.saveContext(context, request, response);
                String token = jwtService.generateToken(userEmail + "_" + role);
                log.info("JWT {} generated for {}:, email: {}", token, user.getName(), userEmail);
                Cookie cookie = new Cookie("token", token);
                cookie.setMaxAge(Integer.MAX_VALUE);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                response.addCookie(cookie);
                log.info("JWT saved in an http cookie on the client browser, email: {}", userEmail);
                if (role.equals("DOCTOR")) {
                    log.info("DOCTOR: {} logged in", userEmail);
                    return new RedirectView("/doctor/" + userEmail + "_" + role);
                } else {
                    log.info("PATIENT: {} logged in", userEmail);
                    return new RedirectView("/patient/" + userEmail + "_" + role);
                }
        }catch (Exception exception){
            log.warn("Incorrect details entered by user");
            redirectAttributes.addFlashAttribute("incorrectDetails","true");
            return new RedirectView("/login");
        }
    }

    @GetMapping("/logout/{email}")
    String logout(@PathVariable("email")String email, Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String currentLoggedInUserEmail = request.getUserPrincipal().getName();
        if (currentLoggedInUserEmail.equals(email)) {
            Arrays.stream(request.getCookies()).forEach(cookie -> {
                cookie.setMaxAge(0);
                cookie.setValue(null);
                cookie.setPath("/");
                response.addCookie(cookie);
            });
            log.info("All cookies were deleted for {}",email);
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, authentication);
            request.getSession().invalidate();
            log.info("User: {} successfully logged out",email);
            return "redirect:/login";
        }else if(doctorService.getDoctorByEmail(email)!=null)return "redirect:/doctor/"+email;
        else return "redirect:/patient/"+email;
    }

    @GetMapping("/forgotPassword")
    String forgotPassword(HttpServletResponse response){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    Object forgotPassword(@RequestParam("email")String email,@RequestParam("role")String role,HttpServletResponse response,RedirectAttributes redirectAttributes,Model model){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        if(userService.ifUserExists(email+"_"+role)){
            Random random=new Random();
            Integer otp=random.nextInt(1000,9999);
            otpService.saveOtp(email,otp);
            emailService.sendEmail(email,"Reset Password","Otp: "+otp);
            log.info("Otp sent to user email: {}",email);
        }
        else {
            redirectAttributes.addFlashAttribute("userDoesNotExists",true);
            return new RedirectView("/forgotPassword");
        }
        model.addAttribute("incorrectDetails",false);
        return "verifyPassword";
    }

    @PostMapping("/verifyPassword")
    String verifyPassword(@RequestParam("email")String email,@RequestParam("otp")Integer otp,@RequestParam("newPassword")String newPassword,@RequestParam("role")String role,HttpServletResponse response,Model model){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        if(userService.ifUserExists(email+"_"+role)){
            Otp systemGeneratedOtp=otpService.getOtp(email);
            if(systemGeneratedOtp.getValue().equals(otp)){
                User user=userService.getUserByEmail(email+"_"+role);
                user.setPassword(newPassword);
                userService.saveUser(user);
                log.info("Password successfully changed for email: {}, role: {}",email,role);
                return "redirect:/login";
            }
        }
        model.addAttribute("incorrectDetails",true);
        return "verifyPassword";
    }
}
