package com.devSoft.Controller;

import com.devSoft.Model.Doctor;
import com.devSoft.Model.Patient;
import com.devSoft.Model.User;
import com.devSoft.Service.DoctorService;
import com.devSoft.Service.PatientService;
import com.devSoft.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public AuthController(UserService userService, PatientService patientService, DoctorService doctorService) {
        this.userService = userService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        try {
            User user = userService.register(
                body.get("name"),
                body.get("email"),
                body.get("password"),
                body.get("role")
            );
            String role = user.getRole();
            if ("PATIENT".equals(role) && patientService.findByEmail(user.getEmail()).isEmpty()) {
                Patient p = new Patient();
                p.setName(user.getName());
                p.setEmail(user.getEmail());
                patientService.createPatient(p);
            } else if ("DOCTOR".equals(role) && doctorService.findByEmail(user.getEmail()).isEmpty()) {
                Doctor d = new Doctor();
                d.setName(user.getName());
                d.setEmail(user.getEmail());
                d.setSpecialization("General");
                doctorService.createDoctor(d);
            }
            return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", role
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        var userOpt = userService.authenticate(body.get("email"), body.get("password"));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
    }
}
