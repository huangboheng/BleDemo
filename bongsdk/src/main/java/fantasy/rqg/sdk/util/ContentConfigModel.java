package fantasy.rqg.sdk.util;

import com.google.gson.annotations.Expose;

/**
 * * Created by rqg on 9/28/16.
 */

public class ContentConfigModel {
    @Expose
    boolean showStep;

    @Expose
    boolean showDistance;

    @Expose
    boolean showCal;

    @Expose
    boolean showHeart;


    @Expose
    Weather weather;


    public boolean isShowStep() {
        return showStep;
    }

    public void setShowStep(boolean showStep) {
        this.showStep = showStep;
    }

    public boolean isShowDistance() {
        return showDistance;
    }

    public void setShowDistance(boolean showDistance) {
        this.showDistance = showDistance;
    }

    public boolean isShowCal() {
        return showCal;
    }

    public void setShowCal(boolean showCal) {
        this.showCal = showCal;
    }

    public boolean isShowHeart() {
        return showHeart;
    }

    public void setShowHeart(boolean showHeart) {
        this.showHeart = showHeart;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public static class Weather {
        @Expose
        boolean showWeather;

        @Expose
        boolean autoLocation;

        @Expose
        String cityName;

        @Expose
        double lat;
        @Expose
        double lon;

        public boolean isFirst = false;


        public boolean isShowWeather() {
            return showWeather;
        }

        public void setShowWeather(boolean showWeather) {
            this.showWeather = showWeather;
        }

        public boolean isAutoLocation() {
            return autoLocation;
        }

        public void setAutoLocation(boolean autoLocation) {
            this.autoLocation = autoLocation;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }
    }
}
