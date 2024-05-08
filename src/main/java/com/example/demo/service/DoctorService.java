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
public class DoctorService {
    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PatientRepo patientRepo;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public Doctor saveDoctor(Doctor doctor) {
        doctor.setPassword(encoder.encode(doctor.getPassword()));
        return doctorRepo.save(doctor) ;
    }

    public Doctor getDoctorByEmail(String email){
        return doctorRepo.findByEmail(email);
    }

    public Doctor acceptPatient(String doctorEmail,String patientEmail){
        Patient patient = patientRepo.findByEmail(patientEmail);
        Doctor doctor=doctorRepo.findByEmail(doctorEmail);
        if(!patient.getRequestStatus().equals(RequestStatus.NO_REQUEST) && doctor.getPatientsRequests().contains(patient)) {
            List<Patient> patientsAccepted = doctor.getPatientsAccepted();
            patientsAccepted.add(patient);
            doctor.setPatientsAccepted(patientsAccepted);
            List<Patient> patientsRequests = doctor.getPatientsRequests();
            patientsRequests.remove(patient);
            doctor.setPatientsRequests(patientsRequests);
            patient.setRequestStatus(RequestStatus.ACCEPTED);
            patientRepo.save(patient);
            return doctorRepo.save(doctor);
        }else {
            deleteRequest(doctorEmail, patient.getEmail());
            return doctor;
        }
    }

    public List<Doctor> getAllDoctors(){
        return doctorRepo.findAll();
    }

    public Doctor removeAccepted(String  doctorEmail, String patientEmail){
        Patient patient = patientRepo.findByEmail(patientEmail);
        Doctor doctor = doctorRepo.findByEmail(doctorEmail);
        List<Patient>patientsAccepted=doctor.getPatientsAccepted();
        patientsAccepted.remove(patient);
        doctor.setPatientsAccepted(patientsAccepted);
        return doctorRepo.save(doctor);
    }

    public Doctor removeDeclined(String doctorEmail, String patientEmail) {
        Patient patient = patientRepo.findByEmail(patientEmail);
        Doctor doctor = doctorRepo.findByEmail(doctorEmail);
        List<Patient>patientsDeclined=doctor.getPatientsDeclined();
        patientsDeclined.remove(patient);
        doctor.setPatientsDeclined(patientsDeclined);
        return doctorRepo.save(doctor);
    }

    public Doctor declinePatient(String doctorEmail, String patientEmail) {
        Doctor doctor=doctorRepo.findByEmail(doctorEmail);
        Patient patient = patientRepo.findByEmail(patientEmail);
        if(!patient.getRequestStatus().equals(RequestStatus.NO_REQUEST)&& doctor.getPatientsRequests().contains(patient)) {
            List<Patient> patientsDeclined = doctor.getPatientsDeclined();
            patientsDeclined.add(patient);
            doctor.setPatientsDeclined(patientsDeclined);
            List<Patient> patientsRequests = doctor.getPatientsRequests();
            patientsRequests.remove(patient);
            doctor.setPatientsRequests(patientsRequests);
            patient.setRequestStatus(RequestStatus.DECLINED);
            patientRepo.save(patient);
            return doctorRepo.save(doctor);
        }else {
            deleteRequest(doctorEmail, patientEmail);
            return doctor;
        }
    }

    public Doctor deleteRequest(String doctorEmail, String patientEmail) {
        Doctor doctor=doctorRepo.findByEmail(doctorEmail);
        Patient patient = patientRepo.findByEmail(patientEmail);
        List<Patient>patientsRequests=doctor.getPatientsRequests();
        patientsRequests.remove(patient);
        doctor.setPatientsRequests(patientsRequests);
        return doctorRepo.save(doctor);
    }
}
