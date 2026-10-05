package com.college.inventory.model;

/**
 * Represents a College Department or Academic Lab Unit.
 */
public class Department {
    private int id;
    private String code;
    private String name;
    private String headOfDepartment;
    private String contactEmail;

    public Department() {}

    public Department(int id, String code, String name, String headOfDepartment, String contactEmail) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.headOfDepartment = headOfDepartment;
        this.contactEmail = contactEmail;
    }

    public Department(String code, String name, String headOfDepartment, String contactEmail) {
        this(-1, code, name, headOfDepartment, contactEmail);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHeadOfDepartment() { return headOfDepartment; }
    public void setHeadOfDepartment(String headOfDepartment) { this.headOfDepartment = headOfDepartment; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
