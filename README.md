# Pocket AMC Reader
Version 2.0

This application will allow you to read catalogs created by [Ant Movie Catalog](http://www.antp.be/software/moviecatalog) on your Android phone. It will read XML catalog and import it into its own database which is done for performance reasons. On each start the application will check size of the XML file and when it changes, database is updated. Depending on size of your catalog and speed of your phone, it might take several minutes to import all movies and to start up.

For more info check out:  
http://forum.antp.be/phpbb2/viewtopic.php?t=4717  
http://www.antp.be/software/moviecatalog  
https://github.com/elman22/pocket-amc-reader

## Screenshots
[![pocket-amc-reader-v2-screen1-thumbnail.png](https://s20.postimg.org/d2sxhemjh/pocket-amc-reader-v2-screen1-thumbnail.png)](https://postimg.org/image/g9nh116zd/) [![pocket-amc-reader-v2-screen2-thumbnail.png](https://s20.postimg.org/ne5aa2e8t/pocket-amc-reader-v2-screen2-thumbnail.png)](https://postimg.org/image/ogfgslx21/)

## Requirements
- Android 4.1 or later
- AMC database saved as XML
- AMC database saved with relative links to pictures (if you want to see pictures)
- Application needs approximately twice as much disk space as is the size of XML file

## Installation
Install the application via [Google Play](https://play.google.com/store/apps/details?id=com.holdingscythe.pocketamcreader) or download from [GitHub]( https://github.com/elman22/pocket-amc-reader/releases).

## Converting AMC to XML
If you keep your database in AMC format, you will have to export it to XML to be able to use this application. You can easily do that in 5 steps:

1. Open your AMC database in Ant Movie Catalog
2. Select File -> Save as...
3. Select type XML and click Save
4. When asked about pictures answer OK
5. Copy XML and all pictures to your Android device and start Pocket AMC Reader

## Settings explained

#### CATALOG
**Catalog**  
Pick your catalog from file browser. It might take some time to display available catalogs in folders with thousands of files.  
**Catalog encoding**  
Select encoding on your PC to ensure correct import of special characters.  
**Remove bad characters**  
If import crashes leaving you with only some movies imported, check this option to fix it. It has the same functionality as AMC script [RemoveBadChars](http://forum.antp.be/phpbb2/viewtopic.php?p=33221#33221) by soulsnake.

#### LIST
**Fields shown in list**  
You can choose fields to be displayed in movie list.  
**Fields separator**  
If you select two or more fields in any of the lines, values will be separated by text entered here.  
**Show sorted field**  
If you sort your list by field which is not displayed, it will be shown in third line.  
**Show thumbnails**  
Show thumbnails in movie list.

#### DETAILS
**Multi field separator**  
Define your separators in multi value fields (such as Country, Category, Actors, Producer ...). This allows you to filter specific values from details. If you use more separators (e.g. comma for Producer and slash for Category) you can input both as ",/" (without quotes).  
**Fit picture**  
Show pictures in details either resized to full screen or with their original size.

## Known bugs
See https://github.com/elman22/pocket-amc-reader/issues. This is also the place to report any found bugs.

## History
#### 29.06.2017 Version 2.0
- ADDED Complete rewrite and redesign of the app
- ADDED Android Material design
- ADDED Redesigned details page
- ADDED Swiping left and right in movie details now switches movies
- ADDED New file picker works on Android 5+ with SD card
- ADDED Quick return header in movie list
- ADDED Full screen image viewer with zooming
- ADDED Integrated RemoveBadChars script into import
- ADDED Faster file import with updated progress bar
- ADDED Landscape layout for movie details
- ADDED Easier arrangement of fields in list
- ADDED Default handling of IMDb links with IMDb app
- ADDED Automatic support for picture extras
- ADDED New default font

#### 28.09.2013 Version 1.9.1
- FIXED FC on opening details on Android 2 and 3

#### 28.09.2013 Version 1.9
- ADDED Support for new fields introduced by AMC 4.2
- ADDED Support for extras introduced by AMC 4.2. Only picture extras are supported. Extras are displayed in Pictures tab and details can be seen when tapping on a picture. Switch images by sideways sliding
- ADDED Small images are enlarged to fit screen
- ADDED Empty fields in details can be hidden (go to Settings to activate)
- ADDED Option to selectively remove list filters
- ADDED New font size especially for tablets
- ADDED Small improvements to increase speed of the app
- FIXED Pictures in subfolder not being displayed
- FIXED Scroll to the top of screen when switching movies
- FIXED Some bugs when rotating device
- REMOVED Multiple covers option. Use extras instead

#### 21.04.2013 Version 1.8.4
- ADDED French translation (thanks to Xavier Muller)

#### 03.03.2013 Version 1.8.3
- ADDED Date added field can be used in list, sorting and filtering
- ADDED Catalog can now be picked from any location, not just from a SD card
- CHANGED Slightly changed layouts
- FIXED Bug where user could filter on values in details even if they were empty
- FIXED Small bug with summary in basic tab
- FIXED Some other minor bugs

#### 30.09.2012 Version 1.8.2
- CHANGED Optimized performance and memory usage with very large lists (4000+ movies)

#### 08.09.2012 Version 1.8.1
- FIXED Problem when bringing app to front and search text was entered
- FIXED Problem when bringing app to front and using next / previous in details
- FIXED 2 problems when starting the app. I was unable to reproduce them, so if they persist, let me know
- FIXED Some other small bugs

#### 12.08.2012 Version 1.8
- ADDED Support for Android 4.0 and higher
- ADDED Font size can be changed
- ADDED File manager for selecting XML file
- ADDED Multivalue search (see Settings and web page for details)
- ADDED Preview of movie's description can be displayed in details
- ADDED Better support for devices with high resolution displays
- CHANGED Swiping in details is replaced with action bar in Android 4.0
- UPDATED German translation
- FIXED Bug with encoding of the catalog
- FIXED Couple of other minor bugs

#### 28.04.2012 Version 1.7.1
- ADDED German translation (thanks to Klaus Oed)

#### 17.04.2012 Version 1.7
- ADDED Support for Color Tag introduced by AMC 4.0
- ADDED Russian translation (thanks to migver)
- ADDED New permission to prevent phone from sleeping during import
- ADDED Visual indication that button "clear filters" was clicked
- ADDED Title URL is shown in different color
- CHANGED Some info and error messages to be more helpful
- FIXED Keyboard is now shown when adding filter value
- FIXED Small bug during import

#### 05.11.2011 Version 1.6.1
- ADDED Original title and Translated title fields to list, sorting, filtering and details
- CHANGED Some tweaks with movies swiping in details
- FIXED Small issue with displaying fields separator in settings

#### 23.10.2011 Version 1.6
- ADDED Swiping right to left and left to right in movie details goes to next / previous movie
- ADDED Customizable fields in movie lists
- ADDED Customizable values separator in movie list
- ADDED Switch to always show sorted field
- ADDED New filter - movies with no picture
- ADDED Automatic app restart or list refresh if needed
- CHANGED Reorganized settings
- FIXED Several minor bugs

#### 06.08.2011 Version 1.5.1
- CHANGED Thumbnails loading order in movies list with some optimizations to make scrolling more fluent
- CHANGED Labels in sorting and IMDb options
- FIXED Error when clicking on IMDb link and IMDb app is not installed
- FIXED Occasional crashes regarding thumbnails when browsing movies list
- FIXED Incorrectly displayed import dialog when import file was not found

#### 10.07.2011 Version 1.5
- ADDED Sorting by Rating, Label, Length, Year and Borrower
- ADDED Filter option to select all empty or not empty values
- ADDED Official IMDb application can be used to display additional info about movies
- ADDED Slightly increased font in movie list
- ADDED Application supports "Move to SD" feature. Not very useful though since only application itself is moved to SD (about 350 kB) and all data remains in main memory
- ADDED Rating to movie list
- ADDED Hints in Settings now show actual selected value
- ADDED If movie database is empty, user is redirected to Settings
- FIXED Pictures not being shown if backslash is used instead of slash in path to catalog setting
- FIXED Filter fields order
- FIXED Crashing and some dialogs during import

#### 30.05.2011 Version 1.4.2
- ADDED Import performance tweaks. Import is now up to 25% faster
- ADDED Optimizations when creating thumbnails
- FIXED Bug with import dialog being shown when actual import is skipped

#### 01.04.2011 Version 1.4.1
- FIXED Some crashes with thumbnails enabled

#### 20.03.2011 Version 1.4
- ADDED Thumbnails in all movie lists. Thumbnails can be turned off in settings
- ADDED Correct screen rotation handling
- ADDED Support for PNG pictures
- FIXED Rotating screen in details view will always reset the view to first movie
- FIXED Rotating screen during import will crash the application

#### 09.01.2011 Version 1.3.1
- ADDED More code pages (catalog encodings) to choose from
- FIXED Crashing with some non standard characters in catalog. However import is canceled, so catalog needs to be fixed manually in Ant Movie Catalog. Read error message for more information

#### 31.12.2010 Version 1.3
- ADDED Filtering for movie lists which can be done either from menu by  wizard or from details view by clicking on underlined text
- ADDED Possibility to redirect IMDb links to English mobile version
- ADDED Information on how much movies have been imported so far during import
- ADDED Welcome info for users who have troubles setting up catalog
- ADDED Several indexes to database which result in faster list loading but somewhat slower import
- FIXED Sorting of movies is now case insensitive
- FIXED Correct new lines display on Android 1.6
- FIXED Better compatibility with Android 1.6

#### 28.11.2010 Version 1.2
- FIXED Crash when audio rate was empty
- FIXED Displaying cached data if some fields were empty
- ADDED Fast scroll to lists
- CHANGED Some styles in movie list

#### 08.11.2010 Version 1.1
- ADDED Sorting in movies list
- ADDED Movie number to movies list
- ADDED Next, previous and random movie menu in movie details

#### 04.11.2010 Version 1.0
- Initial release

## License

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

Pocket AMC Reader for Android (c) 2010-2017 Holding Scythe.
Ant Movie Catalog was created by Antoine Potten and Mickaël Vanneufville.

