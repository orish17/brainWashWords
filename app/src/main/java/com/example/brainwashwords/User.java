package com.example.brainwashwords;

import com.example.brainwashwords.TestResult;
import java.util.Map;

public class User {
    private String name;
    private String email;
    private String password;
    private String displayName; // ✅ חייב להיות מוגדר
    private Map<String, TestResult> tests;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.displayName = name; // ✅ ברירת מחדל: השם האמיתי שהוזן
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Map<String, TestResult> getTests() { return tests; }
    public void setTests(Map<String, TestResult> tests) { this.tests = tests; }
}
