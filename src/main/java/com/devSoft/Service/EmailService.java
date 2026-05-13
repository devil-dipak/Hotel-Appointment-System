package com.devSoft.Service;

import com.devSoft.Model.Appointment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAppointmentConfirmation(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();
        String dateStr = appointment.getAppointmentDate().toString();
        String timeStr = appointment.getAppointmentTime().toString();
        String reason = appointment.getReason() != null ? appointment.getReason() : "N/A";
        String doctorName = appointment.getDoctor().getName();
        String patientName = appointment.getPatient().getName();

        String patientBody = String.format(
            "Dear %s,\n\nYour appointment has been confirmed.\n\n" +
            "Doctor: Dr. %s\nDate: %s\nTime: %s\nReason: %s\nStatus: %s\n\n" +
            "Please arrive on time. Thank you.",
            patientName, doctorName, dateStr, timeStr, reason, "SCHEDULED"
        );
        sendEmail(patientEmail, "Appointment Confirmed - " + dateStr, patientBody);

        String doctorBody = String.format(
            "Dear Dr. %s,\n\nA new appointment has been scheduled.\n\n" +
            "Patient: %s\nDate: %s\nTime: %s\nReason: %s\nStatus: %s",
            doctorName, patientName, dateStr, timeStr, reason, "SCHEDULED"
        );
        sendEmail(doctorEmail, "New Appointment - " + dateStr, doctorBody);
    }

    public void sendAppointmentUpdate(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();
        String dateStr = appointment.getAppointmentDate().toString();
        String timeStr = appointment.getAppointmentTime().toString();
        String reason = appointment.getReason() != null ? appointment.getReason() : "N/A";
        String doctorName = appointment.getDoctor().getName();
        String patientName = appointment.getPatient().getName();

        String patientBody = String.format(
            "Dear %s,\n\nYour appointment has been updated.\n\n" +
            "Doctor: Dr. %s\nDate: %s\nTime: %s\nReason: %s\nStatus: %s\n\n" +
            "Please check the details above.",
            patientName, doctorName, dateStr, timeStr, reason, appointment.getStatus()
        );
        sendEmail(patientEmail, "Appointment Updated - " + dateStr, patientBody);

        String doctorBody = String.format(
            "Dear Dr. %s,\n\nAn appointment has been updated.\n\n" +
            "Patient: %s\nDate: %s\nTime: %s\nReason: %s\nStatus: %s",
            doctorName, patientName, dateStr, timeStr, reason, appointment.getStatus()
        );
        sendEmail(doctorEmail, "Appointment Updated - " + dateStr, doctorBody);
    }

    public void sendAppointmentCancellation(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();
        String dateStr = appointment.getAppointmentDate().toString();
        String timeStr = appointment.getAppointmentTime().toString();
        String doctorName = appointment.getDoctor().getName();
        String patientName = appointment.getPatient().getName();

        String patientBody = String.format(
            "Dear %s,\n\nYour appointment has been CANCELLED.\n\n" +
            "Doctor: Dr. %s\nDate: %s\nTime: %s\n\n" +
            "Please contact the hospital to reschedule if needed.",
            patientName, doctorName, dateStr, timeStr
        );
        sendEmail(patientEmail, "Appointment Cancelled - " + dateStr, patientBody);

        String doctorBody = String.format(
            "Dear Dr. %s,\n\nAn appointment has been CANCELLED.\n\n" +
            "Patient: %s\nDate: %s\nTime: %s",
            doctorName, patientName, dateStr, timeStr
        );
        sendEmail(doctorEmail, "Appointment Cancelled - " + dateStr, doctorBody);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
