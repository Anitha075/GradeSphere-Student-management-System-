package com.example.scholarly.model;

public class Student {

    private Long id;
    private String name;
    private int mathMarks;
    private int scienceMarks;
    private int englishMarks;
    private double average;
    private char grade;

    public Student() {
    }

    public Student(Long id, String name, int mathMarks, int scienceMarks, int englishMarks) {
        this.id = id;
        this.name = name;
        this.mathMarks = mathMarks;
        this.scienceMarks = scienceMarks;
        this.englishMarks = englishMarks;
        recalculate();
    }

    public void recalculate() {
        double avg = (mathMarks + scienceMarks + englishMarks) / 3.0;
        this.average = avg;
        if (avg >= 90) grade = 'A';
        else if (avg >= 80) grade = 'B';
        else if (avg >= 70) grade = 'C';
        else if (avg >= 60) grade = 'D';
        else grade = 'F';
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMathMarks() { return mathMarks; }
    public void setMathMarks(int mathMarks) { this.mathMarks = mathMarks; }

    public int getScienceMarks() { return scienceMarks; }
    public void setScienceMarks(int scienceMarks) { this.scienceMarks = scienceMarks; }

    public int getEnglishMarks() { return englishMarks; }
    public void setEnglishMarks(int englishMarks) { this.englishMarks = englishMarks; }

    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }

    public char getGrade() { return grade; }
    public void setGrade(char grade) { this.grade = grade; }
}
