package org.beanio.spring;

public class Human {

    public static final char FRIEND = 'F';
    public static final char COWORKER = 'C';
    public static final char NEIGHBOR = 'N';
    
    private char type;
    private char gender;
    private String name;
    
    public Human(char type, String name, char gender) {
        this.type = type;
        this.name = name;
        this.gender = gender;
    }

    public char getGender() {
        return gender;
    }

    public char getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
