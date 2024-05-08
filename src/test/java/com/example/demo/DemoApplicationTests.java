package com.example.demo;

import com.example.demo.dao.DoctorRepo;
import com.example.demo.dao.PatientRepo;
import com.example.demo.model.Doctor;
import com.example.demo.model.Patient;
import com.example.demo.model.RequestStatus;
import com.example.demo.service.DoctorService;
import com.example.demo.service.PatientService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class DemoApplicationTests {

	@Mock
	PatientRepo patientRepo;

	@Mock
	DoctorRepo doctorRepo;

	@InjectMocks
	private DoctorService doctorService;

	@InjectMocks
	private PatientService patientService;


	@Test
	void acceptPatient(){
		String patientEmail="patientEmail";
		String doctorEmail="doctorEmail";

		Patient patient=new Patient();
		patient.setEmail(patientEmail);
		patient.setRequestStatus(RequestStatus.PENDING);
		when(patientRepo.findByEmail("patientEmail")).thenReturn(patient);

		Doctor doctor=new Doctor();
		doctor.setEmail(doctorEmail);
		doctor.setPatientsAccepted(new ArrayList<Patient>());
		doctor.setPatientsRequests(new ArrayList<Patient>());
		List<Patient>patientsRequests=doctor.getPatientsRequests();
		patientsRequests.add(patient);
		doctor.setPatientsAccepted(patientsRequests);
		doctor.setPatientsDeclined(new ArrayList<Patient>());
		when(doctorRepo.findByEmail("doctorEmail")).thenReturn(doctor);

		when(patientRepo.save(patient)).thenReturn(patient);
		when(doctorRepo.save(doctor)).thenReturn(doctor);

		Doctor doctor1=doctorService.acceptPatient(doctorEmail,patientEmail);

		assertTrue(doctor1.getPatientsAccepted().contains(patient));

	}

	@Test
	void removeAccepted(){
		String patientEmail="patientEmail";
		String doctorEmail="doctorEmail";

		Patient patient=new Patient();
		patient.setEmail(patientEmail);
		patient.setRequestStatus(RequestStatus.ACCEPTED);
		when(patientRepo.findByEmail("patientEmail")).thenReturn(patient);

		Doctor doctor=new Doctor();
		doctor.setEmail(doctorEmail);
		doctor.setPatientsAccepted(new ArrayList<Patient>());
		List<Patient> patientsAccepted=doctor.getPatientsAccepted();
		patientsAccepted.add(patient);
		doctor.setPatientsRequests(new ArrayList<Patient>());
		doctor.setPatientsDeclined(new ArrayList<Patient>());
		when(doctorRepo.findByEmail("doctorEmail")).thenReturn(doctor);

		when(doctorRepo.save(doctor)).thenReturn(doctor);

		Doctor doctor1=doctorService.removeAccepted(doctorEmail,patientEmail);

		assertTrue(!doctor1.getPatientsAccepted().contains(patient));
	}

	@Test
	void deleteRequest(){
		String patientEmail="patientEmail";
		String doctorEmail="doctorEmail";

		Patient patient=new Patient();
		patient.setEmail(patientEmail);
		patient.setRequestStatus(RequestStatus.ACCEPTED);
		when(patientRepo.findByEmail("patientEmail")).thenReturn(patient);

		Doctor doctor=new Doctor();
		doctor.setEmail(doctorEmail);
		doctor.setPatientsAccepted(new ArrayList<Patient>());
		doctor.setPatientsRequests(new ArrayList<Patient>());
		List<Patient> patientsRequests=doctor.getPatientsRequests();
		patientsRequests.add(patient);
		doctor.setPatientsDeclined(new ArrayList<Patient>());
		when(doctorRepo.findByEmail("doctorEmail")).thenReturn(doctor);

		when(doctorRepo.save(doctor)).thenReturn(doctor);

		Doctor doctor1=doctorService.deleteRequest(doctorEmail,patientEmail);

		assertTrue(!doctor1.getPatientsRequests().contains(patient));
	}

	@Test
	void selectDoctor(){
		String patientEmail="patientEmail";
		String doctorEmail="doctorEmail";

		Patient patient=new Patient();
		patient.setEmail(patientEmail);
		patient.setRequestStatus(RequestStatus.NO_REQUEST);
		when(patientRepo.findByEmail("patientEmail")).thenReturn(patient);

		Doctor doctor=new Doctor();
		doctor.setEmail(doctorEmail);
		doctor.setPatientsAccepted(new ArrayList<Patient>());
		doctor.setPatientsRequests(new ArrayList<Patient>());
		doctor.setPatientsDeclined(new ArrayList<Patient>());
		when(doctorRepo.findByEmail("doctorEmail")).thenReturn(doctor);

		when(doctorRepo.save(doctor)).thenReturn(doctor);
		when(patientRepo.save(patient)).thenReturn(patient);

		Patient patient1=patientService.selectDoctor(patientEmail,doctorEmail);

		assertTrue(patient1.getDoctorRequested().equals(doctor));
		assertTrue(patient1.getRequestStatus().equals(RequestStatus.PENDING));
	}

	@Test
	void createNewRequest(){
		String patientEmail="patientEmail";
		String doctorEmail="doctorEmail";

		Patient patient=new Patient();
		patient.setEmail(patientEmail);
		patient.setRequestStatus(RequestStatus.PENDING);
		when(patientRepo.findByEmail("patientEmail")).thenReturn(patient);

		Doctor doctor=new Doctor();
		doctor.setEmail(doctorEmail);
		doctor.setPatientsAccepted(new ArrayList<Patient>());
		doctor.setPatientsRequests(new ArrayList<Patient>());
		doctor.setPatientsDeclined(new ArrayList<Patient>());
		when(doctorRepo.findByEmail("doctorEmail")).thenReturn(doctor);

		when(doctorRepo.save(doctor)).thenReturn(doctor);
		when(patientRepo.save(patient)).thenReturn(patient);

		Patient patient1=patientService.createNewRequest(patientEmail);

		assertTrue(patient1.getRequestStatus().equals(RequestStatus.NO_REQUEST));
	}
}
