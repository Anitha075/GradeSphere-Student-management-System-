package com.example.scholarly.controller;

import com.example.scholarly.model.Student;
import com.example.scholarly.service.StudentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class StudentController {

    private final StudentService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public StudentController(StudentService service) {
        this.service = service;
    }

    // --- login helpers ---
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    // --- login ---
    @GetMapping({ "/", "/login" })
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        if ("admin".equals(username) && "password".equals(password)) {
            session.setAttribute("user", username);
            return "redirect:/students";
        }
        model.addAttribute("error", "Invalid credentials");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- students UI ---
    @GetMapping("/students")
    public String listStudents(Model model, HttpSession session) {
        if (!isLoggedIn(session))
            return "redirect:/login";

        List<Student> all = service.findAll();
        model.addAttribute("students", all);
        model.addAttribute("count", service.count());
        model.addAttribute("avgAll", service.averageOfAll());
        model.addAttribute("avgMath", service.averageMath());
        model.addAttribute("avgScience", service.averageScience());
        model.addAttribute("avgEnglish", service.averageEnglish());
        model.addAttribute("gradeCounts", service.gradeCounts());

        return "students";
    }

    @GetMapping("/students/new")
    public String newStudentForm(Model model, HttpSession session) {
        if (!isLoggedIn(session))
            return "redirect:/login";
        model.addAttribute("student", new Student());
        return "form";
    }

    @PostMapping("/students")
    public String saveStudent(@ModelAttribute Student student, HttpSession session) {
        if (!isLoggedIn(session))
            return "redirect:/login";
        service.save(student);
        return "redirect:/students";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model, HttpSession session) {
        if (!isLoggedIn(session))
            return "redirect:/login";
        Student s = service.findById(id).orElseThrow();
        model.addAttribute("student", s);
        return "form";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, HttpSession session) {
        if (!isLoggedIn(session))
            return "redirect:/login";
        service.deleteById(id);
        return "redirect:/students";
    }

    // --- export JSON ---
    @GetMapping("/students/export/json")
    public ResponseEntity<byte[]> exportJson(HttpSession session) throws IOException {
        if (!isLoggedIn(session)) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<Student> all = service.findAll();
        byte[] data = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(all);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    // --- export CSV ---
    @GetMapping("/students/export/csv")
    public ResponseEntity<byte[]> exportCsv(HttpSession session) {
        if (!isLoggedIn(session)) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<Student> all = service.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,name,math,science,english,average,grade\n");
        for (Student s : all) {
            sb.append(s.getId()).append(",")
                    .append(escapeCsv(s.getName())).append(",")
                    .append(s.getMathMarks()).append(",")
                    .append(s.getScienceMarks()).append(",")
                    .append(s.getEnglishMarks()).append(",")
                    .append(String.format("%.2f", s.getAverage())).append(",")
                    .append(s.getGrade()).append("\n");
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "";
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        String v = value.replace("\"", "\"\"");
        if (needsQuotes) {
            return "\"" + v + "\"";
        }
        return v;
    }

    // --- import JSON ---
    @PostMapping("/students/import/json")
    public String importJson(@RequestParam("file") MultipartFile file,
            HttpSession session) {
        if (!isLoggedIn(session))
            return "redirect:/login";

        if (file.isEmpty()) {
            return "redirect:/students";
        }

        try {
            List<Student> list = mapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<Student>>() {
                    });
            service.replaceAll(list);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/students";
    }
}
