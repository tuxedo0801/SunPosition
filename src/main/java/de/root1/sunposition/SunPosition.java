/*
 * Copyright (C) 2015 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 *   SunPosition is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SunPosition is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SunPosition.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.sunposition;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Calculates the sun position (azimuth/altitude) based on a given geolocation
 * @author achristian
 */
public class SunPosition {

    private final double K = Math.PI / 180D;
    private final double lon;
    private final double lat;

    /**
     * Container object with calculation results
     */
    public class Sun {

        private double altitude;
        private double azimuth;
        private Calendar cal;

        private void setAzimuth(double azimuth) {
            this.azimuth = azimuth;
        }

        private void setAltitude(double altitude) {
            this.altitude = altitude;
        }

        private void setCal(Calendar cal) {
            this.cal = cal;
        }

        /**
         * Get azimuth of sun position 
         * @return 
         */
        public double getAzimuth() {
            return azimuth;
        }

        /**
         * Get altitude of sun position
         * @return 
         */
        public double getAltitude() {
            return altitude;
        }

        /**
         * Get date of current sun position calculation
         * @return 
         */
        public Calendar getCal() {
            return cal;
        }

        @Override
        public String toString() {
            return "Sun{" + "altitude=" + altitude + ", azimuth=" + azimuth + ", cal=" + new Date(cal.getTimeInMillis()) + '}';
        }
        

    }

    /**
     * Creates a new sun position object based on a geolocation
     * @param latitude e.g. 49,4499314D
     * @param longitude e.g. 8,6712089D
     */
    public SunPosition(double latitude, double longitude) {
        this.lon = longitude;
        this.lat = latitude;
    }
    
    /**
     * Does the sun position calculation and returns a sun object, containing the position of the sun at the given date
     * @param c
     * @return 
     */
    public Sun calc(Calendar c) {
        Sun s = new Sun();
        s.setCal(c);

        // Formula based on "Logikbaustein - 19820", see download area on www.KNX-User-Forum.de
        double dayOfYear = c.get(Calendar.DAY_OF_YEAR);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        double timediff = hour + minute / 60d - (15d - lon) / 15d - 11d;
        double declin = -23.45 * Math.cos(K * 360 * (dayOfYear + 10) / 365);

        double x = Math.sin(K * lat) * Math.sin(K * declin) + Math.cos(K * lat) * Math.cos(K * declin) * Math.cos(K * 15 * timediff);
        double height = Math.asin(x) / K;

        double y = -1 * (Math.sin(K * lat) * x - Math.sin(K * declin)) / (Math.cos(K * lat) * Math.sin(Math.acos(x)));
        if (y > 0.9999) {
            y = 0.9999;
        } else if (y < -0.9999) {
            y = -0.9999;
        }
        double azimuth=0;
        if (timediff<=-12) {
            azimuth = 360-(Math.acos(y)/K);
        } else if (timediff>-12 && timediff<=0) {
            azimuth = Math.acos(y)/K;
        } else if (timediff>0 && timediff<=12) {
            azimuth = 360-(Math.acos(y)/K);
        } else if (timediff>12) {
            azimuth = Math.acos(y)/K;
        }

        s.setAzimuth(azimuth);
        s.setAltitude(height);
        return s;
    }

    /**
     * To the current position calculation based on GMT timezone and current time.
     * @return 
     */
    public Sun calcGMT() {
        return calc(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
    }

}
