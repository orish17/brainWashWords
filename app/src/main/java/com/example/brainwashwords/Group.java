package com.example.brainwashwords;

public class Group {

        private String name;

        public Group(String name) {
            this.name = name;
        }

        public Group()
        {
            // need for firebase
        }

        public String getName() {
            return name;
        }

         public void setName(String name) {
        this.name = name;
         }
    }
