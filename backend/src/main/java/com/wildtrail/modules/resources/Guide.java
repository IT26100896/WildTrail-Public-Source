package com.wildtrail.modules.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wildtrail.exception.ApiException;

/** Encapsulates validated guide details; subtypes decide their specialty. */
public class Guide {
    private final int id;
    private final String fullName, phone, licenseNumber, specialty;
    private final boolean available;
    @JsonCreator
    public Guide(@JsonProperty("id") int id, @JsonProperty("fullName") String fullName,
                 @JsonProperty("phone") String phone, @JsonProperty("licenseNumber") String licenseNumber,
                 @JsonProperty("specialty") String specialty, @JsonProperty("available") boolean available) {
        if(fullName==null || fullName.isBlank() || licenseNumber==null || licenseNumber.isBlank()) throw ApiException.badRequest("Name and licence number are required");
        if(fullName.length()>120 || licenseNumber.length()>60 || phone==null || !phone.matches("[+0-9 ()-]{7,40}")) throw ApiException.badRequest("Guide details are invalid");
        if(!java.util.Set.of("DRIVER","NATURALIST").contains(specialty)) throw ApiException.badRequest("Invalid guide specialty");
        this.id=id; this.fullName=fullName; this.phone=phone; this.licenseNumber=licenseNumber; this.specialty=specialty; this.available=available;
    }
    public int getId(){return id;}
    public String getFullName(){return fullName;}
    public String getPhone(){return phone;}
    public String getLicenseNumber(){return licenseNumber;}
    public String getSpecialty(){return specialty;}
    public boolean isAvailable(){return available;}
    public String getAssignmentRole(){return "Safari driver";}
    public static Guide create(int id,String name,String phone,String license,String specialty,boolean available) {
        return specialty.equals("NATURALIST") ? new NaturalistGuide(id,name,phone,license,available) : new Guide(id,name,phone,license,specialty,available);
    }
}
