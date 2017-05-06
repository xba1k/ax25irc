/*
 * AVRS - http://avrs.sourceforge.net/
 *
 * Copyright (C) 2011 John Gorkos, AB0OO
 *
 * AVRS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * AVRS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AVRS; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */
package ax25irc.aprs.parser;

import java.util.Date;

/**
 *
 * @author johng This class represents a Position as specified by the APRS
 * specification. This includes a symbol table and actual symbol, and a possible
 * timestamp.
 *
 */
public class Position implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private Double latitude = 0d, longitude = 0d;
    private Integer altitude = -1;
    private Integer positionAmbiguity;
    private Date timestamp;
    private char symbolTable, symbolCode;
    private String csTField = " sT";

    public Position() {
        timestamp = new Date();
    }

    public Position(double lat, double lon, int posAmb, char st, char sc) {
        this.latitude = Math.round(lat * 100000) * 0.00001D;
        this.longitude = Math.round(lon * 100000) * 0.00001D;
        this.positionAmbiguity = posAmb;
        this.symbolTable = st;
        this.symbolCode = sc;
        this.timestamp = new Date();
    }

    public Position(double lat, double lon) {
        this.latitude = Math.round(lat * 100000) * 0.00001D;
        this.longitude = Math.round(lon * 100000) * 0.00001D;
        this.positionAmbiguity = 0;
        this.symbolTable = '\\';
        this.symbolCode = '.';
        this.timestamp = new Date();
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the altitude
     */
    public int getAltitude() {
        return altitude;
    }

    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    /**
     * @return the positionAmbiguity
     */
    public int getPositionAmbiguity() {
        return positionAmbiguity;
    }

    /**
     * @param positionAmbiguity the positionAmbiguity to set
     */
    public void setPositionAmbiguity(int positionAmbiguity) {
        this.positionAmbiguity = positionAmbiguity;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return this.timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the symbolTable
     */
    public char getSymbolTable() {
        return symbolTable;
    }

    /**
     * @param symbolTable the symbolTable to set
     */
    public void setSymbolTable(char symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * @return the symbolCode
     */
    public char getSymbolCode() {
        return symbolCode;
    }

    /**
     * @param symbolCode the symbolCode to set
     */
    public void setSymbolCode(char symbolCode) {
        this.symbolCode = symbolCode;
    }

    public String getDMS(double decimalDegree, boolean isLatitude) {
        int minFrac = (int) Math.round(decimalDegree * 6000); ///< degree in 1/100s of a minute
        boolean negative = (minFrac < 0);
        if (negative) {
            minFrac = -minFrac;
        }
        int deg = minFrac / 6000;
        int min = (minFrac / 100) % 60;
        minFrac = minFrac % 100;
        String ambiguousFrac;

        switch (positionAmbiguity) {
            case 1: // "dd  .  N"
                ambiguousFrac = "  .  ";
                break;
            case 2: // "ddm .  N"
                ambiguousFrac = String.format("%d .  ", min / 10);
                break;
            case 3: // "ddmm.  N"
                ambiguousFrac = String.format("%02d.  ", min);
                break;
            case 4: // "ddmm.f N"
                ambiguousFrac = String.format("%02d.%d ", min, minFrac / 10);
                break;
            default: // "ddmm.ffN"
                ambiguousFrac = String.format("%02d.%02d", min, minFrac);
                break;
        }
        if (isLatitude) {
            return String.format("%02d%s%s", deg, ambiguousFrac, (negative ? "S" : "N"));
        } else {
            return String.format("%03d%s%s", deg, ambiguousFrac, (negative ? "W" : "E"));
        }
    }

    @Override
    public String toString() {
        return getDMS(latitude, true) + symbolTable + getDMS(longitude, false) + symbolCode;
    }

    public String toDecimalString() {
        return latitude + ", " + longitude;
    }

    public void setCsTField(String val) {
        if (val == null || val == "") {
            val = " sT";
        }
        csTField = val;
    }

    public String getCsTField() {
        return csTField;
    }

    public String toCompressedString() {
        long latbase = Math.round(380926 * (90 - this.latitude));
        long latchar1 = latbase / (91 * 91 * 91) + 33;
        latbase = latbase % (91 * 91 * 91);
        long latchar2 = latbase / (91 * 91) + 33;
        latbase = latbase % (91 * 91);
        int latchar3 = (int) (latbase / 91) + 33;
        int latchar4 = (int) (latbase % 91) + 33;
        long lonbase = Math.round(190463 * (180 + this.longitude));
        long lonchar1 = lonbase / (91 * 91 * 91) + 33;
        lonbase %= (91 * 91 * 91);
        long lonchar2 = lonbase / (91 * 91) + 33;
        lonbase = lonbase % (91 * 91);
        int lonchar3 = (int) (lonbase / 91) + 33;
        int lonchar4 = (int) (lonbase % 91) + 33;

        return "" + symbolTable + (char) latchar1 + (char) latchar2 + (char) latchar3 + (char) latchar4
                + "" + (char) lonchar1 + (char) lonchar2 + (char) lonchar3 + (char) lonchar4 + symbolCode + csTField;
    }

    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
        
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        return new Float(dist).floatValue();

        /*
        
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = 0; //el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return new Float(Math.sqrt(distance)).floatValue() / 1000;*/
        
    }

    public float distance(Position position2) {
        double lat1 = this.getLatitude();
        double lat2 = position2.getLatitude();
        double lng1 = this.getLongitude();
        double lng2 = position2.getLongitude();
        return distFrom(lat1, lng1, lat2, lng2);
    }

    public String bearing(Position position2) {

        double lat1 = this.getLatitude();
        double lon1 = this.getLongitude();

        double lat2 = position2.getLatitude();
        double lon2 = position2.getLongitude();

        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);
        double resultDegree = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        String coordNames[] = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N"};
        double directionid = Math.round(resultDegree / 22.5);

        if (directionid < 0) {
            directionid = directionid + 16;
        }

        String compasLoc = coordNames[(int) directionid];

        return Math.round(resultDegree) + " " + compasLoc;

    }

    public static void main(String[] args) {
        Position pos = new Position(45.52, -122.681944);

        Position pos2 = new Position(45.427778, -122.778889);
        Position pos3 = new Position(34.05, -118.25);

        System.out.println("Distance : " + Math.round(pos.distance(pos2)));
        System.out.println("Bearing : " + pos.bearing(pos2));

        System.out.println("Distance : " + Math.round(pos.distance(pos3)));
        System.out.println("Bearing : " + pos.bearing(pos3));

    }

}
