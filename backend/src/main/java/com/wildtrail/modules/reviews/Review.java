package com.wildtrail.modules.reviews;
import com.wildtrail.exception.ApiException;
/** Validated review text is hidden behind a small domain contract. */
public class Review {
    private final int rating;
    private final String comment;
    public Review(int rating,String comment){
        if(rating<1 || rating>5 || comment==null || comment.isBlank() || comment.length()>1000) throw ApiException.badRequest("Review must have 1-5 stars and a comment of up to 1000 characters");
        this.rating=rating;this.comment=comment.trim();
    }
    public int getRating(){return rating;}
    public String getComment(){return comment;}
    public String getType(){return "CUSTOMER";}
}
