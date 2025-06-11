package com.example.brainwashwords;

/**
 * A model class for storing the result of a test/quiz.
 * This class is used to save and retrieve test results from Firebase.
 */
public class TestResult {

    /**
     * The success rate of the test, as a percentage (0-100).
     */
    private int successRate;

    /**
     * Default constructor required for Firebase deserialization.
     * Firebase uses this constructor when converting data snapshots into objects.
     */
    public TestResult() {
    }

    /**
     * Constructor that initializes the object with a given success rate.
     *
     * @param successRate the percentage score achieved by the user in a test.
     */
    public TestResult(int successRate) {
        this.successRate = successRate;
    }

    /**
     * Returns the success rate of the test.
     *
     * @return an integer from 0 to 100 representing the user's performance.
     */
    public int getSuccessRate() {
        return successRate;
    }

    /**
     * Sets the success rate of the test.
     *
     * @param successRate the new percentage score to store.
     */
    public void setSuccessRate(int successRate) {
        this.successRate = successRate;
    }
}
