package com.devSoft.Controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.devSoft.Service.AppointmentService;
import com.devSoft.Service.DoctorService;
import com.devSoft.Service.PatientService;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;

    public DashboardController(DoctorService doctorService,
                               PatientService patientService,
                               AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = Map.of(
            "totalDoctors", doctorService.getDoctorCount(),
            "totalPatients", patientService.getPatientCount(),
            "totalAppointments", appointmentService.getAppointmentCount(),
            "scheduledAppointments", appointmentService.getScheduledCount(),
            "completedAppointments", appointmentService.getCompletedCount(),
            "cancelledAppointments", appointmentService.getCancelledCount()
        );
        return ResponseEntity.ok(stats);
    }
}