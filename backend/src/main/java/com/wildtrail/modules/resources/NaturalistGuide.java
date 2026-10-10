package com.wildtrail.modules.resources;
public final class NaturalistGuide extends Guide {
    public NaturalistGuide(int id,String name,String phone,String license,boolean available){super(id,name,phone,license,"NATURALIST",available);}
    @Override public String getAssignmentRole(){return "Wildlife naturalist and safari driver";}
}
