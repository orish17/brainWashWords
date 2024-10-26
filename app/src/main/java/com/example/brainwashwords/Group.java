package com.example.brainwashwords;

public class Group {

        private String name;
        private String id;

        public Group(String name, String id) {
            this.name = name;
            this.id = id;
        }
        public Group(String name) {
        this.name = name;
        }

        public String getName() {
            return name;
        }

         public void setName(String name) {
        this.name = name;
         }

        public String getId() {
            return id;
        }

          public void setId(String id) {
        this.id = id;
         }
    }
