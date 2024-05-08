package com.example.demo.controller;

import com.example.demo.model.Doctor;
import com.example.demo.model.Patient;
import com.example.demo.model.RequestStatus;
import com.example.demo.service.DoctorService;
import com.example.demo.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "/doctor")
@Slf4j
public class DoctorController{

    @Autowired
    PatientService patientService;

    @Autowired
    DoctorService doctorService;

    @GetMapping(value="/accept/{doctorEmail}/{patientEmail}")
    RedirectView accept(@PathVariable("doctorEmail")String doctorEmail, @PathVariable("patientEmail")String patientEmail, RedirectAttributes attributes, HttpServletResponse response, HttpServletRequest request) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String currentLoggedInUser = request.getUserPrincipal().getName();
        if (currentLoggedInUser.equals(doctorEmail)) {
            Patient patient = patientService.getPatientByEmail(patientEmail);
            Doctor doctor = doctorService.getDoctorByEmail(doctorEmail);
            if (!doctor.getPatientsRequests().contains(patient)) {
                attributes.addFlashAttribute("requestAlreadyAcceptedOrDeclined", true);
                log.info("Request for PATIENT:{} could not be accepted, request was already accepted or declined DOCTOR: {}",patientEmail,doctorEmail);
                return new RedirectView("/doctor/"+doctorEmail);
            }
            doctorService.acceptPatient(doctorEmail, patientEmail);
            if (patient.getRequestStatus().equals(RequestStatus.NO_REQUEST)) {
                attributes.addFlashAttribute("requestWithdrawn", true);
                log.info("Request for PATIENT:{} could not be accepted, request was withdrawn by the patient for DOCTOR: {}",patientEmail,doctorEmail);
                return new RedirectView("/doctor/"+doctorEmail);
            }
            return new RedirectView("/doctor/"+doctorEmail);
        }else return new RedirectView("/login");
    }

    @GetMapping(value="/decline/{doctorEmail}/{patientEmail}")
    RedirectView decline(@PathVariable("doctorEmail")String doctorEmail,@PathVariable("patientEmail")String patientEmail,RedirectAttributes attributes, HttpServletResponse response,HttpServletRequest request){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String currentLoggedInUser = request.getUserPrincipal().getName();
        if (currentLoggedInUser.equals(doctorEmail)) {
            Doctor doctor = doctorService.getDoctorByEmail(doctorEmail);
            Patient patient = patientService.getPatientByEmail(patientEmail);
            if (!doctor.getPatientsRequests().contains(patient)) {
                attributes.addFlashAttribute("requestAlreadyAcceptedOrDeclined", true);
                log.info("Request for PATIENT:{} could not be accepted, request was already accepted or declined DOCTOR: {}",patientEmail,doctorEmail);
                return new RedirectView("/doctor/"+doctorEmail);
            }
            doctorService.declinePatient(doctorEmail, patientEmail);
            if (patient.getRequestStatus().equals(RequestStatus.NO_REQUEST)) {
                attributes.addFlashAttribute("requestWithdrawn", true);
                return new RedirectView("/doctor/"+doctorEmail);
            }
            log.info("DOCTOR: {} declined request with PATIENT:{}",doctorEmail,patientEmail);
            return new RedirectView("/doctor/"+doctorEmail);
        }else return new RedirectView("/login");
    }

    @GetMapping(value="/remove/accepted/{doctorEmail}/{patientEmail}")
    String removeAccepted(@PathVariable("doctorEmail")String doctorEmail,@PathVariable("patientEmail")String patientEmail, HttpServletResponse response,HttpServletRequest request){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String currentLoggedInUser = request.getUserPrincipal().getName();
        if (currentLoggedInUser.equals(doctorEmail)) {
            doctorService.removeAccepted(doctorEmail, patientEmail);
            log.info("DOCTOR: {} removed PATIENT: {} from patients accepted.",doctorEmail,patientEmail);
            return "redirect:/doctor/" + doctorEmail;
        }else return "redirect:/login";
    }

    @GetMapping(value="/remove/declined/{doctorEmail}/{patientEmail}")
    String removeDeclined(@PathVariable("doctorEmail")String doctorEmail,@PathVariable("patientEmail")String patientEmail, HttpServletResponse response,HttpServletRequest request){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String currentLoggedInUser = request.getUserPrincipal().getName();
        if (currentLoggedInUser.equals(doctorEmail)) {
            doctorService.removeDeclined(doctorEmail, patientEmail);
            log.info("DOCTOR: {} removed PATIENT: {} from patients declined.",doctorEmail,patientEmail);
            return "redirect:/doctor/" + doctorEmail;
        }else return "redirect:/login";
    }

    @GetMapping(value = "/{doctorEmail}")
    String doctor(@PathVariable("doctorEmail")String doctorEmail, Model model, HttpServletResponse response, HttpServletRequest request,@ModelAttribute("requestWithdrawn")String requestWithdrawn,@ModelAttribute("requestAlreadyAcceptedOrDeclined")String requestAlreadyAcceptedOrDeclined){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String currentLoggedInUser = request.getUserPrincipal().getName();
        if (currentLoggedInUser.equals(doctorEmail)) {
            Doctor doctor = doctorService.getDoctorByEmail(doctorEmail);
            model.addAttribute("doctor", doctor);
            if(requestAlreadyAcceptedOrDeclined.isEmpty())model.addAttribute("requestAlreadyAcceptedOrDeclined",false);
            else model.addAttribute("requestAlreadyAcceptedOrDeclined",true);
            if(requestWithdrawn.isEmpty())model.addAttribute("requestWithdrawn",false);
            else model.addAttribute("requestWithdrawn",true);
            return "doctor";
        }else return "redirect:/login";
    }

}
