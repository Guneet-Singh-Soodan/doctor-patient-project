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

import java.util.List;

@Controller
@RequestMapping(value="/patient")
@Slf4j
public class PatientController {
    @Autowired
    PatientService patientService;

    @Autowired
    DoctorService doctorService;

    @PostMapping(value = "/select/{patientEmail}")
    String select(@PathVariable("patientEmail") String patientEmail, String doctorEmail, HttpServletResponse response, HttpServletRequest request) {
        response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
        String currentLoggedInUser = request.getUserPrincipal().getName();
        if (currentLoggedInUser.equals(patientEmail)) {
            Patient patient=patientService.selectDoctor(patientEmail,doctorEmail);
            log.info("PATIENT: {} sent request to DOCTOR: {}",patient.getEmail(),patient.getDoctorRequested().getEmail());
            return"redirect:/patient/"+patientEmail;
        } else return "redirect:/login";
    }

    @GetMapping(value="/request/new/{patientEmail}")
    String requestNew(@PathVariable("patientEmail")String patientEmail, HttpServletResponse response,HttpServletRequest request){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String currentLoggedInUser=request.getUserPrincipal().getName();
        if (currentLoggedInUser.equals(patientEmail)) {
            Patient patient=patientService.createNewRequest(patientEmail);
            log.info("PATIENT {} request status updated: {}, user can now generate new request",patientEmail,patient.getRequestStatus());
            return "redirect:/patient/" + patientEmail;
        } else return "redirect:/login";
    }

    @GetMapping(value="/request/delete/{patientEmail}/{doctorEmail}")
    RedirectView requestDelete(@PathVariable("doctorEmail")String doctorEmail, @PathVariable("patientEmail")String patientEmail, RedirectAttributes attributes, HttpServletResponse response, HttpServletRequest request){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String currentLoggedInUser=request.getUserPrincipal().getName();
        if (currentLoggedInUser.equals(patientEmail)) {
            Patient patient = patientService.getPatientByEmail(patientEmail);
            Doctor doctor = doctorService.getDoctorByEmail(doctorEmail);
            if(!doctor.getPatientsRequests().contains(patient)){
                attributes.addFlashAttribute("alreadyAcceptedOrDeclined", "true");
                log.info("PATIENT: {} Delete request unsuccessful, DOCTOR: {} already accepted the request",patientEmail,doctorEmail);
                return new RedirectView("/patient/"+patientEmail);
            }
            patient = patientService.updateRequestStatus(patient, RequestStatus.NO_REQUEST);
            if(doctor.getPatientsRequests().contains(patient))doctorService.deleteRequest(doctorEmail, patientEmail);
            else doctorService.removeAccepted(patientEmail,doctorEmail);
            log.info("PATIENT: {} Delete request successful, request deleted for DOCTOR: {}",patientEmail,doctorEmail);
            return new RedirectView("/patient/"+patientEmail);
        } else return new RedirectView("/login");
    }

    @GetMapping(value = "/{patientEmail}")
    String patient(@PathVariable("patientEmail")String patientEmail, Model model, HttpServletResponse response,HttpServletRequest request,@ModelAttribute("alreadyAcceptedOrDeclined")String alreadyAcceptedOrDeclined) {
        String currentLoggedInUser = request.getUserPrincipal().getName();
        if (currentLoggedInUser.equals(patientEmail)) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            Patient patient = patientService.getPatientByEmail(patientEmail);
            List<Doctor> allDoctors = doctorService.getAllDoctors();
            model.addAttribute("patient", patient);
            model.addAttribute("allDoctors", allDoctors);
            if(allDoctors.isEmpty())model.addAttribute("noDoctorsAvailable",true);
            else model.addAttribute("noDoctorsAvailable",false);
            if(alreadyAcceptedOrDeclined.equals("true"))model.addAttribute("alreadyAcceptedOrDeclined",true);
            else model.addAttribute("alreadyAcceptedOrDeclined",false);
            return "patient";
        } else return "redirect:/login";
    }
}

