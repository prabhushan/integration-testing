package com.java.bytes.patientServices;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.java.bytes.data.repositories.PatientRepo;
import com.java.bytes.entities.Patient;
import com.java.bytes.external.ExternalUserInfo;
import com.java.bytes.external.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PatientServices {

	private static final String SUCCESSFULL = "ok";
	private static final String FAILURE = "failure";

	@Autowired
	private AppointmentServices appointmentServices;

	@Autowired
	private PatientRepo patientRepo;

	@Autowired
	private ExternalUserInfo externalUserInfo;

	@Transactional
	public UUID createPatients(PatientDTO patientDTO) throws Exception {

		try {
			User user = externalUserInfo.getUserId(1);

			log.info("## {}", user.toString());
			Patient patient = Patient.builder().firstName(patientDTO.getFirstName()).lastName(patientDTO.getLastName())
					.phoneNumber(user.getPhone()).emailAddress(user.getEmail())
					.dateOfBirth(patientDTO.getDateOfBirth()).build();
			return patientRepo.save(patient).getId();
		} catch (feign.RetryableException ex) {
			throw new Exception("User Info failed");

		}


	}

	public PatientDTO getPatient(UUID id) {
		Patient patient = patientRepo.getPatientInfoByID(id);
		if (patient == null) {
			log.warn("Patient not found for id {}", id);
			throw new EntityNotFoundException();
		}
		return PatientDTO.builder().firstName(patient.getFirstName()).lastName(patient.getLastName())
				.emailAddress(patient.getEmailAddress()).phoneNumber(patient.getPhoneNumber())
				.dateOfBirth(patient.getDateOfBirth()).build();

	}

	public String bookAppointments(PatientDTO patient, LocalDateTime appointmentDateTime) {
		if (patient != null && StringUtils.hasText(patient.getFirstName())) {
			return postProcessing(patient, appointmentDateTime);

		}

		throw new IllegalArgumentException("Patient firstName cannot be empty");

	}

	private String postProcessing(PatientDTO patient, LocalDateTime appointmentDateTime) {
		PatientDTO normalizedPatient = normalize(patient);
		boolean isAppointmentBooked = appointmentServices.bookAppointment(normalizedPatient, appointmentDateTime);
		if (isAppointmentBooked) {
			return SUCCESSFULL;
		}
		return FAILURE;
	}

	private PatientDTO normalize(PatientDTO patient) {
		return PatientDTO.builder().firstName(patient.getFirstName().toUpperCase())
				.lastName(StringUtils.capitalize(patient.getLastName().toUpperCase())).dateOfBirth(patient.getDateOfBirth()).build();

	}

}
