package com.example.scholarly.service;

import com.example.scholarly.model.Student;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StudentService {

    private final Map<Long, Student> store = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ObjectMapper mapper = new ObjectMapper();
    private final File dataFile = new File("students.json");

    @PostConstruct
    public void init() {
        loadFromFile();
    }

    private synchronized void loadFromFile() {
        if (!dataFile.exists() || !dataFile.isFile()) {
            return;
        }
        try {
            List<Student> list = mapper.readValue(dataFile, new TypeReference<List<Student>>() {});
            store.clear();
            long maxId = 0;
            for (Student s : list) {
                if (s.getId() == null) {
                    s.setId(++maxId);
                }
                s.recalculate();
                store.put(s.getId(), s);
                if (s.getId() > maxId) {
                    maxId = s.getId();
                }
            }
            idGenerator.set(maxId + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void saveToFile() {
        try {
            List<Student> list = new ArrayList<>(store.values());
            mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<Student> findAll() {
        return new ArrayList<>(store.values());
    }

    public synchronized Optional<Student> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public synchronized Student save(Student student) {
        if (student.getId() == null) {
            long id = idGenerator.getAndIncrement();
            student.setId(id);
        }
        student.recalculate();
        store.put(student.getId(), student);
        saveToFile();
        return student;
    }

    public synchronized void deleteById(Long id) {
        store.remove(id);
        saveToFile();
    }

    // replace all students (used for import)
    public synchronized void replaceAll(List<Student> students) {
        store.clear();
        long maxId = 0;
        for (Student s : students) {
            if (s.getId() == null) {
                s.setId(++maxId);
            }
            s.recalculate();
            store.put(s.getId(), s);
            if (s.getId() > maxId) {
                maxId = s.getId();
            }
        }
        idGenerator.set(maxId + 1);
        saveToFile();
    }

    public synchronized double averageOfAll() {
        List<Student> all = findAll();
        if (all.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Student s : all) sum += s.getAverage();
        return sum / all.size();
    }

    public synchronized long count() {
        return store.size();
    }

    public synchronized double averageMath() {
        List<Student> all = findAll();
        if (all.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Student s : all) sum += s.getMathMarks();
        return sum / all.size();
    }

    public synchronized double averageScience() {
        List<Student> all = findAll();
        if (all.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Student s : all) sum += s.getScienceMarks();
        return sum / all.size();
    }

    public synchronized double averageEnglish() {
        List<Student> all = findAll();
        if (all.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Student s : all) sum += s.getEnglishMarks();
        return sum / all.size();
    }

    public synchronized Map<Character, Long> gradeCounts() {
        Map<Character, Long> counts = new LinkedHashMap<>();
        counts.put('A', 0L);
        counts.put('B', 0L);
        counts.put('C', 0L);
        counts.put('D', 0L);
        counts.put('F', 0L);
        for (Student s : store.values()) {
            char g = s.getGrade();
            if (!counts.containsKey(g)) {
                counts.put(g, 0L);
            }
            counts.put(g, counts.get(g) + 1);
        }
        return counts;
    }
}
