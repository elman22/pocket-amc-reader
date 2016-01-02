package com.holdingscythe.pocketamcreader.model;

/**
 * Data Model for Movie
 * Created by Elman on 2. 1. 2016.
 */
public class MovieModel {
    private String Number;
    private String Checked;
    private String FormattedTitle;
    private String MediaLabel;
    private String MediaType;
    private String Source;
    private String Date;
    private String Borrower;
    private String Rating;
    private String OriginalTitle;
    private String TranslatedTitle;
    private String Director;
    private String Producer;
    private String Country;
    private String Category;
    private String Length;
    private String Year;
    private String Actors;
    private String URL;
    private String Description;
    private String Comments;
    private String VideoFormat;
    private String VideoBitrate;
    private String AudioFormat;
    private String AudioBitrate;
    private String Resolution;
    private String Framerate;
    private String Languages;
    private String Subtitles;
    private String Size;
    private String Disks;
    private String Picture;
    private String ColorTag;
    private String DateWatched;
    private String UserRating;
    private String Writer;
    private String Composer;
    private String Certification;
    private String FilePath;

    public MovieModel(String Number, String Checked, String FormattedTitle, String MediaLabel, String MediaType,
                      String Source, String Date, String Borrower, String Rating, String OriginalTitle,
                      String TranslatedTitle, String Director, String Producer, String Country, String Category,
                      String Length, String Year, String Actors, String URL, String Description, String Comments,
                      String VideoFormat, String VideoBitrate, String AudioFormat, String AudioBitrate,
                      String Resolution, String Framerate, String Languages, String Subtitles, String Size,
                      String Disks, String Picture, String ColorTag, String DateWatched, String UserRating,
                      String Writer, String Composer, String Certification, String FilePath) {
        this.Number = Number;
        this.Checked = Checked;
        this.FormattedTitle = FormattedTitle;
        this.MediaLabel = MediaLabel;
        this.MediaType = MediaType;
        this.Source = Source;
        this.Date = Date;
        this.Borrower = Borrower;
        this.Rating = Rating;
        this.OriginalTitle = OriginalTitle;
        this.TranslatedTitle = TranslatedTitle;
        this.Director = Director;
        this.Producer = Producer;
        this.Country = Country;
        this.Category = Category;
        this.Length = Length;
        this.Year = Year;
        this.Actors = Actors;
        this.URL = URL;
        this.Description = Description;
        this.Comments = Comments;
        this.VideoFormat = VideoFormat;
        this.VideoBitrate = VideoBitrate;
        this.AudioFormat = AudioFormat;
        this.AudioBitrate = AudioBitrate;
        this.Resolution = Resolution;
        this.Framerate = Framerate;
        this.Languages = Languages;
        this.Subtitles = Subtitles;
        this.Size = Size;
        this.Disks = Disks;
        this.Picture = Picture;
        this.ColorTag = ColorTag;
        this.DateWatched = DateWatched;
        this.UserRating = UserRating;
        this.Writer = Writer;
        this.Composer = Composer;
        this.Certification = Certification;
        this.FilePath = FilePath;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getChecked() {
        return Checked;
    }

    public void setChecked(String checked) {
        Checked = checked;
    }

    public String getFormattedTitle() {
        return FormattedTitle;
    }

    public void setFormattedTitle(String formattedTitle) {
        FormattedTitle = formattedTitle;
    }

    public String getMediaLabel() {
        return MediaLabel;
    }

    public void setMediaLabel(String mediaLabel) {
        MediaLabel = mediaLabel;
    }

    public String getMediaType() {
        return MediaType;
    }

    public void setMediaType(String mediaType) {
        MediaType = mediaType;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getBorrower() {
        return Borrower;
    }

    public void setBorrower(String borrower) {
        Borrower = borrower;
    }

    public String getRating() {
        return Rating;
    }

    public void setRating(String rating) {
        Rating = rating;
    }

    public String getOriginalTitle() {
        return OriginalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        OriginalTitle = originalTitle;
    }

    public String getTranslatedTitle() {
        return TranslatedTitle;
    }

    public void setTranslatedTitle(String translatedTitle) {
        TranslatedTitle = translatedTitle;
    }

    public String getDirector() {
        return Director;
    }

    public void setDirector(String director) {
        Director = director;
    }

    public String getProducer() {
        return Producer;
    }

    public void setProducer(String producer) {
        Producer = producer;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getLength() {
        return Length;
    }

    public void setLength(String length) {
        Length = length;
    }

    public String getYear() {
        return Year;
    }

    public void setYear(String year) {
        Year = year;
    }

    public String getActors() {
        return Actors;
    }

    public void setActors(String actors) {
        Actors = actors;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getComments() {
        return Comments;
    }

    public void setComments(String comments) {
        Comments = comments;
    }

    public String getVideoFormat() {
        return VideoFormat;
    }

    public void setVideoFormat(String videoFormat) {
        VideoFormat = videoFormat;
    }

    public String getVideoBitrate() {
        return VideoBitrate;
    }

    public void setVideoBitrate(String videoBitrate) {
        VideoBitrate = videoBitrate;
    }

    public String getAudioFormat() {
        return AudioFormat;
    }

    public void setAudioFormat(String audioFormat) {
        AudioFormat = audioFormat;
    }

    public String getAudioBitrate() {
        return AudioBitrate;
    }

    public void setAudioBitrate(String audioBitrate) {
        AudioBitrate = audioBitrate;
    }

    public String getResolution() {
        return Resolution;
    }

    public void setResolution(String resolution) {
        Resolution = resolution;
    }

    public String getFramerate() {
        return Framerate;
    }

    public void setFramerate(String framerate) {
        Framerate = framerate;
    }

    public String getLanguages() {
        return Languages;
    }

    public void setLanguages(String languages) {
        Languages = languages;
    }

    public String getSubtitles() {
        return Subtitles;
    }

    public void setSubtitles(String subtitles) {
        Subtitles = subtitles;
    }

    public String getSize() {
        return Size;
    }

    public void setSize(String size) {
        Size = size;
    }

    public String getDisks() {
        return Disks;
    }

    public void setDisks(String disks) {
        Disks = disks;
    }

    public String getPicture() {
        return Picture;
    }

    public void setPicture(String picture) {
        Picture = picture;
    }

    public String getColorTag() {
        return ColorTag;
    }

    public void setColorTag(String colorTag) {
        ColorTag = colorTag;
    }

    public String getDateWatched() {
        return DateWatched;
    }

    public void setDateWatched(String dateWatched) {
        DateWatched = dateWatched;
    }

    public String getUserRating() {
        return UserRating;
    }

    public void setUserRating(String userRating) {
        UserRating = userRating;
    }

    public String getWriter() {
        return Writer;
    }

    public void setWriter(String writer) {
        Writer = writer;
    }

    public String getComposer() {
        return Composer;
    }

    public void setComposer(String composer) {
        Composer = composer;
    }

    public String getCertification() {
        return Certification;
    }

    public void setCertification(String certification) {
        Certification = certification;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

}
