package com.example.brainwashwords;


    public class TestResult {
        private int successRate;

        // חובה שיהיה קונסטרקטור ריק בשביל Firebase
        public TestResult() {
        }

        public TestResult(int successRate) {
            this.successRate = successRate;
        }

        public int getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(int successRate) {
            this.successRate = successRate;
        }
    }


