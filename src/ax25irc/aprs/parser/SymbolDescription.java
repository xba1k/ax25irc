/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc.aprs.parser;

/**
 *
 * @author alex
 */
public class SymbolDescription {

    // generated from http://www.aprs.net/vm/DOS/SYMBOLS.HTM
    public static String[] table1 = new String[]{
        "Police",
        "reserved",
        "DIGI (white)",
        "Phone",
        "DX Cluster",
        "HF Gate",
        "Aircraft (small)",
        "Cloudy",
        "reserved",
        "Snow",
        "Red Cross",
        "reverse L",
        "House QTH",
        "X",
        "Dot",
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "Fire",
        "Campground",
        "Motorcycle",
        "Railroad Engine",
        "CAR",
        "File Server",
        "Storm",
        "Air Station",
        "BBS",
        "Canoe",
        "D",
        "E",
        "F",
        "Grid Square",
        "Hotel",
        "TCP-IP",
        "J",
        "School",
        "avail",
        "MacAPRS",
        "NTS Station",
        "Balloon",
        "Police",
        "TBD",
        "RV",
        "Shuttle",
        "Thunderstorm",
        "Bus",
        "TBD",
        "Weather Station",
        "HELO",
        "Yacht",
        "WinAPRS",
        "Runner",
        "Triangle",
        "PBBS",
        "Large Aircraft",
        "Weather Station",
        "Dish Antenna",
        "Ambulance",
        "Bike",
        "tbd",
        "Fire Dept",
        "Horse",
        "Fire Truck",
        "Glider",
        "Hospital",
        "IOTA",
        "Jeep",
        "Truck",
        "Area Locations",
        "Mic-Repeater",
        "Node",
        "EOC",
        "Rover",
        "GRID SQ",
        "Antenna",
        "Ship",
        "Truck Stop",
        "Truck",
        "VAN",
        "Water Station",
        "xAPRS",
        "YAGI@QTH",
        "Z",
        "{",
        "|",
        "}",
        "~"

    };

    public static String[] table2 = new String[]{};

    public static String decode(char table, char symbol) {

        switch (table) {

            case '/':
                return table1[symbol - '!'];
            case '\\':
                return "unimplemented symbol table";

        }

        return "uknown symbol table";

    }

}
