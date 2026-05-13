package com.devSoft.Service;

import com.devSoft.Model.Appointment;
import com.devSoft.Model.Doctor;
import com.devSoft.Model.Patient;
import com.devSoft.Repository.AppointmentRepository;
import com.devSoft.Repository.DoctorRepository;
import com.devSoft.Repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
    }

    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        Doctor managedDoctor = doctorRepository.findById(appointment.getDoctor().getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + appointment.getDoctor().getId()));
        Patient managedPatient = patientRepository.findById(appointment.getPatient().getId())
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + appointment.getPatient().getId()));

        // Conflict check: same doctor + same date + same time
        List<Appointment> conflicts = appointmentRepository
                .findByDoctorIdAndAppointmentDate(managedDoctor.getId(), appointment.getAppointmentDate());
        for (Appointment existing : conflicts) {
            if (existing.getAppointmentTime().equals(appointment.getAppointmentTime())
                    && !"CANCELLED".equals(existing.getStatus())) {
                throw new RuntimeException("Doctor is already booked at " + appointment.getAppointmentTime()
                        + " on " + appointment.getAppointmentDate());
            }
        }

        appointment.setDoctor(managedDoctor);
        appointment.setPatient(managedPatient);
        appointment.setStatus("SCHEDULED");
        Appointment saved = appointmentRepository.save(appointment);

        emailService.sendAppointmentConfirmation(saved);
        return saved;
    }

    @Transactional
    public Appointment updateAppointment(Long id, Appointment appointmentDetails) {
        Appointment appointment = getAppointmentById(id);
        if (appointmentDetails.getDoctor() != null && appointmentDetails.getDoctor().getId() != null) {
            Doctor managedDoctor = doctorRepository.findById(appointmentDetails.getDoctor().getId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            appointment.setDoctor(managedDoctor);
        }
        if (appointmentDetails.getPatient() != null && appointmentDetails.getPatient().getId() != null) {
            Patient managedPatient = patientRepository.findById(appointmentDetails.getPatient().getId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            appointment.setPatient(managedPatient);
        }
        appointment.setAppointmentDate(appointmentDetails.getAppointmentDate());
        appointment.setAppointmentTime(appointmentDetails.getAppointmentTime());
        appointment.setReason(appointmentDetails.getReason());
        appointment.setStatus(appointmentDetails.getStatus());
        appointment.setNotes(appointmentDetails.getNotes());
        Appointment saved = appointmentRepository.save(appointment);
        emailService.sendAppointmentUpdate(saved);
        return saved;
    }

    @Transactional
    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getTodaysAppointments() {
        return appointmentRepository.findByAppointmentDate(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Appointment> searchAppointments(LocalDate date, String status, Long doctorId) {
        if (date != null && status != null && !status.isEmpty() && doctorId != null) {
            return appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date).stream()
                    .filter(a -> a.getStatus().equalsIgnoreCase(status))
                    .toList();
        }
        if (date != null) return appointmentRepository.findByAppointmentDate(date);
        if (status != null && !status.isEmpty()) return appointmentRepository.findByStatus(status.toUpperCase());
        if (doctorId != null) return appointmentRepository.findByDoctorId(doctorId);
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public void writePdf(OutputStream outputStream) throws DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Appointments Report", titleFont);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        CMYKColor headerColor = new CMYKColor(0, 0, 0, 0.8f);
        String[] headers = {"ID", "Doctor", "Patient", "Date", "Time", "Reason", "Status", "Notes"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerColor);
            table.addCell(cell);
        }

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (Appointment a : appointmentRepository.findAll()) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(a.getId()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(a.getDoctor().getName(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(a.getPatient().getName(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(a.getAppointmentDate().toString(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(a.getAppointmentTime().toString(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(a.getReason() != null ? a.getReason() : "", cellFont)));
            table.addCell(new PdfPCell(new Phrase(a.getStatus(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(a.getNotes() != null ? a.getNotes() : "", cellFont)));
        }

        document.add(table);
        document.close();
    }

    @Transactional
    public Appointment cancelAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatus("CANCELLED");
        Appointment saved = appointmentRepository.save(appointment);
        emailService.sendAppointmentCancellation(saved);
        return saved;
    }

    @Transactional
    public Appointment completeAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatus("COMPLETED");
        Appointment saved = appointmentRepository.save(appointment);
        emailService.sendAppointmentUpdate(saved);
        return saved;
    }

    public long getAppointmentCount() { return appointmentRepository.count(); }
    public long getScheduledCount() { return appointmentRepository.countByStatus("SCHEDULED"); }
    public long getCompletedCount() { return appointmentRepository.countByStatus("COMPLETED"); }
    public long getCancelledCount() { return appointmentRepository.countByStatus("CANCELLED"); }
}
