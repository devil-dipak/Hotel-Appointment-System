package com.devSoft.Controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private final ResourceLoader resourceLoader;

    public UserController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = {"/", "/login", "/login.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> login() throws IOException {
        return serve("classpath:static/login.html");
    }

    @GetMapping(value = {"/dashboard", "/dashboard.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> dashboard() throws IOException {
        return serve("classpath:static/dashboard.html");
    }

    @GetMapping(value = {"/doctors", "/doctors.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> doctors() throws IOException {
        return serve("classpath:static/doctors.html");
    }

    @GetMapping(value = {"/patients", "/patients.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> patients() throws IOException {
        return serve("classpath:static/patients.html");
    }

    @GetMapping(value = {"/appointments", "/appointments.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> appointments() throws IOException {
        return serve("classpath:static/appointments.html");
    }

    @GetMapping(value = {"/signup", "/signup.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> signup() throws IOException {
        return serve("classpath:static/signup.html");
    }

    @GetMapping(value = {"/profile", "/profile.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> profile() throws IOException {
        return serve("classpath:static/profile.html");
    }

    private ResponseEntity<String> serve(String path) throws IOException {
        var resource = resourceLoader.getResource(path);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        var content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(content);
    }
}