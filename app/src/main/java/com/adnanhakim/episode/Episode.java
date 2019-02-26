package com.adnanhakim.episode;

public class Episode {

    private int no;
    private double rating;
    private String date, title, overview, imageURL;

    public Episode(int no, String date, String title, String overview, double rating, String imageURL) {
        this.no = no;
        this.date = date;
        this.title = title;
        this.overview = overview;
        this.rating = rating;
        this.imageURL = imageURL;
    }

    public int getNo() {
        return no;
    }

    public double getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getImageURL() {
        return imageURL;
    }
}
