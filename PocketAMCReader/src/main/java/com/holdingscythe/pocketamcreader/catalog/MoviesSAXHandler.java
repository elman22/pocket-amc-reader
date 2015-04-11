package com.holdingscythe.pocketamcreader.catalog;

import android.util.Log;

import com.holdingscythe.pocketamcreader.S;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class MoviesSAXHandler extends DefaultHandler {
    private long lastInsertedRowId;
    private final MoviesDataProvider moviesDataProvider;

    public MoviesSAXHandler(MoviesDataProvider m) {
        moviesDataProvider = m;
    }

    @Override
    public void startDocument() throws SAXException {
        /** Delete all data before import */
        moviesDataProvider.deleteAll();
    }

    @Override
    public void endDocument() throws SAXException {
    }

    /**
     * Gets called on opening tags like: <tag> Can provide attribute(s),
     * when XML was like: <tag attribute="attributeValue">
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("Movie")) {
            try {
                // read attributes
                String Number = atts.getValue(Movies.NUMBER);
                String Checked = atts.getValue(Movies.CHECKED);
                String FormattedTitle = atts.getValue(Movies.FORMATTED_TITLE);
                String MediaLabel = atts.getValue(Movies.MEDIA_LABEL);
                String MediaType = atts.getValue(Movies.MEDIA_TYPE);
                String Source = atts.getValue(Movies.SOURCE);
                String Date = atts.getValue(Movies.DATE);
                String Borrower = atts.getValue(Movies.BORROWER);
                String Rating = parseFloat(atts.getValue(Movies.RATING));
                String OriginalTitle = atts.getValue(Movies.ORIGINAL_TITLE);
                String TranslatedTitle = atts.getValue(Movies.TRANSLATED_TITLE);
                String Director = atts.getValue(Movies.DIRECTOR);
                String Producer = atts.getValue(Movies.PRODUCER);
                String Country = atts.getValue(Movies.COUNTRY);
                String Category = atts.getValue(Movies.CATEGORY);
                String Year = atts.getValue(Movies.YEAR);
                String Length = atts.getValue(Movies.LENGTH);
                String Actors = atts.getValue(Movies.ACTORS);
                String URL = atts.getValue(Movies.URL);
                String Description = parseMultiline(atts.getValue(Movies.DESCRIPTION));
                String Comments = parseMultiline(atts.getValue(Movies.COMMENTS));
                String VideoFormat = atts.getValue(Movies.VIDEO_FORMAT);
                String VideoBitrate = atts.getValue(Movies.VIDEO_BITRATE);
                String AudioFormat = atts.getValue(Movies.AUDIO_FORMAT);
                String AudioBitrate = atts.getValue(Movies.AUDIO_BITRATE);
                String Resolution = atts.getValue(Movies.RESOLUTION);
                String Framerate = parseFloat(atts.getValue(Movies.FRAMERATE));
                String Languages = atts.getValue(Movies.LANGUAGES);
                String Subtitles = atts.getValue(Movies.SUBTITLES);
                String Size = atts.getValue(Movies.SIZE);
                String Disks = atts.getValue(Movies.DISKS);
                String Picture = parsePath(atts.getValue(Movies.PICTURE));
                String ColorTag = atts.getValue(Movies.COLOR_TAG);
                String DateWatched = atts.getValue(Movies.DATE_WATCHED);
                String UserRating = parseFloat(atts.getValue(Movies.USER_RATING));
                String Writer = atts.getValue(Movies.WRITER);
                String Composer = atts.getValue(Movies.COMPOSER);
                String Certification = atts.getValue(Movies.CERTIFICATION);
                String FilePath = atts.getValue(Movies.FILE_PATH);

                this.lastInsertedRowId = moviesDataProvider.insert(Number, Checked, FormattedTitle, MediaLabel,
                        MediaType, Source, Date, Borrower, Rating, OriginalTitle, TranslatedTitle, Director,
                        Producer, Country, Category, Year, Length, Actors, URL, Description, Comments, VideoFormat,
                        VideoBitrate, AudioFormat, AudioBitrate, Resolution, Framerate, Languages, Subtitles, Size,
                        Disks, Picture, ColorTag, DateWatched, UserRating, Writer, Composer, Certification, FilePath);

                // log event
                if (S.VERBOSE)
                    Log.v(S.TAG, "Imported movie: " + FormattedTitle);
            } catch (Exception e) {
                if (S.ERROR)
                    Log.e(S.TAG, atts.getValue("FormattedTitle") + " -> " + e.toString());
            }
        } else if (localName.equals("Extra")) {
            try {
                // read attributes
                String EPicture = parsePath(atts.getValue(Movies.E_PICTURE));
                // only read pictures, other extras are ignored
                if (EPicture == null) {
                    // log event
                    if (S.VERBOSE)
                        Log.v(S.TAG, "  - skipped extra");
                } else {
                    String EChecked = atts.getValue(Movies.E_CHECKED);
                    String ETag = atts.getValue(Movies.E_TAG);
                    String ETitle = atts.getValue(Movies.E_TITLE);
                    String ECategory = atts.getValue(Movies.E_CATEGORY);
                    String EURL = atts.getValue(Movies.E_URL);
                    String EDescription = parseMultiline(atts.getValue(Movies.E_DESCRIPTION));
                    String EComments = parseMultiline(atts.getValue(Movies.E_COMMENTS));
                    String ECreatedBy = atts.getValue(Movies.E_CREATED_BY);

                    moviesDataProvider.insertExtra(this.lastInsertedRowId, EChecked, ETag, ETitle, ECategory, EURL,
                            EDescription, EComments, ECreatedBy, EPicture);

                    // log event
                    if (S.VERBOSE)
                        Log.v(S.TAG, "  + imported extra: " + ETitle);
                }
            } catch (Exception e) {
                if (S.ERROR)
                    Log.e(S.TAG, atts.getValue("ETitle") + " -> " + e.toString());
            }
        }
    }

    /**
     * Gets called on closing tags like: </tag>
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    }

    @Override
    public void error(SAXParseException e) {
        if (S.ERROR)
            Log.e(S.TAG, e.toString());
    }

    @Override
    public void fatalError(SAXParseException e) {
        if (S.ERROR)
            Log.e(S.TAG, e.toString());
    }

    @Override
    public void warning(SAXParseException e) {
        if (S.WARN)
            Log.w(S.TAG, e.toString());
    }

    private String parseMultiline(String s) {
        if (s != null)
            s = s.replace("|", "\n");
        return s;
    }

    private String parseFloat(String s) {
        if (s != null)
            s = s.replace(",", ".");
        return s;
    }

    private String parsePath(String s) {
        if (s != null)
            s = s.replace("\\", "/");
        return s;
    }
}
