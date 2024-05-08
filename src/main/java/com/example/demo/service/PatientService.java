package com.example.demo.service;

import com.example.demo.dao.DoctorRepo;
import com.example.demo.dao.PatientRepo;
import com.example.demo.model.Doctor;
import com.example.demo.model.Patient;
import com.example.demo.model.RequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public Patient savePatient(Patient patient) {
        patient.setPassword(encoder.encode(patient.getPassword()));
        return patientRepo.save(patient) ;

    }

    public Patient getPatientByEmail(String email){
        return patientRepo.findByEmail(email);
    }


    public Patient updateRequestStatus(Patient patient, RequestStatus requestStatus) {
        patient.setRequestStatus(requestStatus);
        return patientRepo.save(patient);
    }

    public Patient selectDoctor(String patientEmail, String doctorEmail) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail);
        Patient patient = patientRepo.findByEmail(patientEmail);
        List<Patient> patientRequests = doctor.getPatientsRequests();
        patientRequests.add(patient);
        doctorRepo.save(doctor);
        patient.setRequestStatus(RequestStatus.PENDING);
        patient.setDoctorRequested(doctor);
        return patientRepo.save(patient);
    }

    public Patient createNewRequest(String patientEmail) {
        Patient patient = patientRepo.findByEmail(patientEmail);
        patient.setRequestStatus(RequestStatus.NO_REQUEST);
        return patientRepo.save(patient);
    }


}
