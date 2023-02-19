package com.radio.kutai.constants;

import android.Manifest;

public interface IXRadioConstants {

	boolean DEBUG = false;

	boolean ALLOW_FB_ADS = false; //true if you want to use FB ads, if not let put it to be false

	boolean SAVE_FAVORITE_SDCARD = true; //false if you don't want to save favorite to SDCARD, vice versa
	String[] LIST_PERMISSIONS= {Manifest.permission.WRITE_EXTERNAL_STORAGE};

	String TAG = "DCM";

	boolean USE_BLUR_EFFECT = false; //use blur effect for background
	int EQUALIZER_DURATION = 2000; // 2 seconds

	int NUMBER_INSTALL_DAYS=0;//it is the number install days to show dialog rate.default is 0
	int NUMBER_LAUNCH_TIMES=3;//it is the number launch times to show dialog rate.default is 3
	int REMIND_TIME_INTERVAL=1;//it is the number repeat days to show dialog rate.default is 1

	//true if you just want to use the in-app webview when you click link in the application
	boolean USE_INTERNAL_WEB_BROWSER = true;

	//number grid column in app for Flat Grid, Card Grid
	int NUMBER_GRID_COLUMN = 2;

    String YOUR_CONTACT_EMAIL = "radiokutai@gmail.com";
    String URL_FACEBOOK = "https://facebook.com/";
    String URL_TWITTER = "https://twitter.com/";
    String URL_WEBSITE = "";
    String URL_INSTAGRAM = "https://instagram.com/";

	String URL_TERM_OF_USE = "https://app.indonesiastreaming.com/radiokutai/term_of_use.php";
	String URL_PRIVACY_POLICY = "https://app.indonesiastreaming.com/radiokutai/privacy_policy.php";

	// if set it to be true, all icons (wifi, battery..) on status bar will be black
	boolean USE_LIGHT_STATUS_BAR = false;

	//true if you just want to use offline config. It will save request
	boolean OFFLINE_UI_CONFIG = false;

	boolean SHOW_ADS = false; //enable all ads
	boolean SHOW_SPLASH_INTERSTITIAL_ADS = false; //enable interstitial splash ads

	boolean AUTO_PLAY_IN_SINGLE_MODE = true; //enable auto play in single mode

	int INTERSTITIAL_FREQUENCY = 2; //click each item radio to show this one

	boolean RESET_TIMER = true; // reset timer when exiting application

	boolean AUTO_NEXT_WHEN_COMPLETE = false; // auto change to next station when your radio is complete

	//TODO PLEASE KEEP IT false AS NOW. BE CAREFULLY IF YOU WANT TO CHANGE THIS PARAM. LET EMAIL us if you want to know
	boolean IS_MUSIC_PLAYER = false; // set it to be true if you want your app to be music player

	String DIR_CACHE = "kutai";

	String ADMOB_TEST_DEVICE = "D4BE0E7875BD1DDE0C1C7C9CF169EB6E";
	String FACEBOOK_TEST_DEVICE = "fa7ca73be399926111af1f5aa142b2d2";

	int NUMBER_ITEM_PER_PAGE = 10;
	int MAX_PAGE = 20;

	int TYPE_TAB_FEATURED = 2;
	int TYPE_TAB_GENRE = 3;
	int TYPE_TAB_THEMES = 4;
	int TYPE_TAB_FAVORITE = 5;
	int TYPE_UI_CONFIG = 6;
	int TYPE_DETAIL_GENRE = 7;
	int TYPE_SEARCH = 8;
	int TYPE_SINGLE_RADIO = 9;

	String KEY_ALLOW_MORE = "allow_more";
	String KEY_IS_TAB = "is_tab";
	String KEY_TYPE_FRAGMENT = "type";
	String KEY_ALLOW_READ_CACHE = "read_cache";
	String KEY_ALLOW_REFRESH = "allow_refresh";
	String KEY_ALLOW_SHOW_NO_DATA = "allow_show_no_data";
    String KEY_READ_CACHE_WHEN_NO_DATA = "cache_when_no_data";
    String KEY_GENRE_ID = "cat_id";
    String KEY_SEARCH = "search_data";

	String KEY_NUMBER_ITEM_PER_PAGE = "number_item_page";
	String KEY_MAX_PAGE = "max_page";
	String KEY_OFFLINE_DATA = "offline_data";

	String DIR_TEMP = ".temp";

	String FILE_CONFIGURE= "config.json";
	String FILE_RADIOS= "radios.json";
	String FILE_THEMES= "themes.json";
	String FILE_UI= "ui.json";
	String FILE_GENRES= "genres.json";


	int TYPE_APP_SINGLE= 1;
	int TYPE_APP_MULTI= 2;

	int UI_FLAT_GRID= 1;
	int UI_FLAT_LIST= 2;
	int UI_CARD_GRID= 3;
	int UI_CARD_LIST= 4;
	int UI_MAGIC_GRID= 5;
	int UI_HIDDEN= 0;

	int UI_BG_JUST_ACTIONBAR = 0;
	int UI_BG_FULL = 1;

	int UI_PLAYER_SQUARE_DISK= 1;
	int UI_PLAYER_CIRCLE_DISK= 2;
	int UI_PLAYER_ROTATE_DISK= 3;

	int UI_PLAYER_NO_LAST_FM_SQUARE_DISK= 4;
	int UI_PLAYER_NO_LAST_FM_CIRCLE_DISK= 5;
	int UI_PLAYER_NO_LAST_FM_ROTATE_DISK= 6;

	float RATE_MAGIC_HEIGHT=1.5f;

	String TAG_FRAGMENT_DETAIL_GENRE = "TAG_FRAGMENT_DETAIL_GENRE";
	String TAG_FRAGMENT_DETAIL_SEARCH = "TAG_FRAGMENT_DETAIL_SEARCH";
	boolean ALLOW_DRAG_DROP_WHEN_EXPAND = false;

	String FORMAT_LAST_FM = "http://ws.audioscrobbler.com/2.0/?method=track.search&track=%1$s&api_key=%2$s&format=json&limit=1";

	long DELTA_TIME = 30000;
	long DEGREE = 6;
	long ONE_HOUR = 3600000;

	int MAX_SLEEP_MODE = 120;
	int MIN_SLEEP_MODE = 5;
	int STEP_SLEEP_MODE = 5;

	String DEFAULT_START_COLOR = "#ebdb34";
	String DEFAULT_END_COLOR = "#ffea00";

}
