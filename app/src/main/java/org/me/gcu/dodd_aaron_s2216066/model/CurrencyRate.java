package org.me.gcu.dodd_aaron_s2216066.model;

public class CurrencyRate {

    private String title;

    private String code;

    private String name;

    private String country;

    private double rate;

    private String pubDate;

    public CurrencyRate() {
    }

    public CurrencyRate(String title,
                        String code,
                        String name,
                        String country,
                        double rate,
                        String pubDate) {
        this.title = title;
        this.code = code;
        this.name = name;
        this.country = country;
        this.rate = rate;
        this.pubDate = pubDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    @Override
    public String toString() {
        return "CurrencyRate{" +
                "title='" + title + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", rate=" + rate +
                ", pubDate='" + pubDate + '\'' +
                '}';
    }
}

