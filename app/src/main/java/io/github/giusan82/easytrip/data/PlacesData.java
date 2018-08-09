package io.github.giusan82.easytrip.data;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import timber.log.Timber;

public class PlacesData {
    private static final String SPLITTER_PARENT_ID = "wv__";

    private static final String KEY_RESULTS = "results";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ESTIMATED_TOTAL = "estimated_total";
    private static final String KEY_SCORE = "score";
    private static final String KEY_IMAGES = "images";
    private static final String KEY_SIZES = "sizes";
    private static final String KEY_THUMBNAIL = "thumbnail";
    private static final String KEY_IMAGE_SIZE_MEDIUM = "medium";
    private static final String KEY_IMAGE_SIZE_ORIGINAL = "original";
    private static final String KEY_IMAGE_URL = "url";
    private static final String KEY_COUNTRY_ID = "country_id";
    private static final String KEY_COORDINATES = "coordinates";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_PARENT_ID = "parent_id";
    private static final String KEY_LOCATION_ID = "location_id";
    private static final String KEY_INTRO = "intro";

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_LOCATION_ID = "location_id";
    public static final String EXTRA_PARENT_NAME = "parent_name";
    public static final String EXTRA_COUNTRY_NAME = "country_name";
    public static final String EXTRA_IMAGE_URL = "image_url";

    public static final String BUNDLE_KEY_PLACE_NAME = "place_name";
    public static final String BUNDLE_KEY_COUNTRY_NAME = "country_name";
    public static final String BUNDLE_KEY_PARENT_NAME = "parent_name";
    public static final String BUNDLE_KEY_PLACE_IMAGE_URL = "image_url";
    public static final String BUNDLE_KEY_PARENT_PLACE_IMAGE_URL = "parent_image_url";
    public static final String BUNDLE_KEY_INTRO = "intro";
    public static final String BUNDLE_KEY_PLACES_LIST = "places_list";

    public static final String ACTION_KEY_PLACES = "places";
    public static final String ACTION_KEY_LOCATION = "location";

    @SerializedName(KEY_RESULTS)
    private Results[] mResults;
    @SerializedName(KEY_ESTIMATED_TOTAL)
    private int mEstimatedTotal;

    public PlacesData() {
    }

    public int getEstimatedTotal() {
        return mEstimatedTotal;
    }

    public Results[] getResults() {
        return mResults;
    }

    @Parcel
    public static class Results {
        @SerializedName(KEY_ID)
        public String mID;
        @SerializedName(KEY_NAME)
        public String mName;
        @SerializedName(KEY_SCORE)
        public double mScore;
        @SerializedName(KEY_IMAGES)
        public Images[] mImages;
        @SerializedName(KEY_COUNTRY_ID)
        public String mCountryId;
        @SerializedName(KEY_COORDINATES)
        public Coordinates mCoordinates;
        @SerializedName(KEY_PARENT_ID)
        public String mParentId;
        @SerializedName(KEY_LOCATION_ID)
        public String mLocationId;
        @SerializedName(KEY_INTRO)
        public String mIntro;

        public Results() {
        }

        public String getID() {
            return mID;
        }

        public String getName() {
            return mName;
        }

        public double getScore() {
            return mScore;
        }

        public Images[] getImages() {
            return mImages;
        }

        public String getCountryId() {
            return mCountryId;
        }

        public String getLocationId() {
            return mLocationId;
        }

        public String getIntro() {
            return mIntro;
        }

        public String getCountryName() {
            String name = "";
            if (mCountryId != null) {
                name = mCountryId.replace("_", " ");
            }
            return name;
        }

        public Coordinates getCoordinates() {
            return mCoordinates;
        }

        public String getmParentId() {
            return mParentId;
        }

        public String getParentName() {
            String name = "";

            if (mID != null) {
                String[] parts = mID.split(SPLITTER_PARENT_ID);
                Timber.d(mID);
                if (mID.contains(SPLITTER_PARENT_ID)) {
                    name = parts[1].replace("_", " ");
                    Timber.d(name);
                }
            }
            return name;
        }

        @Parcel
        public static class Images {
            @SerializedName(KEY_SIZES)
            public Sizes mSizes;

            public Sizes getSizes() {
                return mSizes;
            }

            @Parcel
            public static class Sizes {
                @SerializedName(KEY_THUMBNAIL)
                public Thumbnail mThumbnail;
                @SerializedName(KEY_IMAGE_SIZE_MEDIUM)
                public Medium mMedium;
                @SerializedName(KEY_IMAGE_SIZE_ORIGINAL)
                public Original mOriginal;

                public Thumbnail getThumbnail() {
                    return mThumbnail;
                }

                public Medium getMedium() {
                    return mMedium;
                }

                public Original getOriginal() {
                    return mOriginal;
                }

                @Parcel
                public static class Thumbnail {
                    @SerializedName(KEY_IMAGE_URL)
                    public String mImageUrl;

                    public String getImageUrl() {
                        return mImageUrl;
                    }
                }

                @Parcel
                public static class Medium {
                    @SerializedName(KEY_IMAGE_URL)
                    public String mImageUrl;

                    public String getImageUrl() {
                        return mImageUrl;
                    }
                }

                @Parcel
                public static class Original {
                    @SerializedName(KEY_IMAGE_URL)
                    public String mImageUrl;

                    public String getImageUrl() {
                        return mImageUrl;
                    }
                }
            }
        }

        @Parcel
        public static class Coordinates {
            @SerializedName(KEY_LATITUDE)
            public String mLatitude;
            @SerializedName(KEY_LONGITUDE)
            public String mLogintude;

            public Coordinates() {
            }

            public String getLatitude() {
                return mLatitude;
            }

            public String getLogintude() {
                return mLogintude;
            }
        }
    }
}
