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

 * Please note that significant portions of this code were taken from the JAVA FAP
 * translation by Matti Aarnio at http://repo.ham.fi/websvn/java-aprs-fap/
 * 
 */

package ax25irc.aprs.parser;

import java.util.ArrayList;
/**
 * 
 * @author johng
 *	This is the code parser for AX25 UI packets that are traditionally used in APRS networks, in TNC2
 * format.  TNC2 format is defined as:
 * SOURCE>DESTIN,VIA,VIA:payload
 * In APRS packets, the first character of the payload is the Data Type Identifier, which is the key for
 * further parsing of the message.  This class parses raw TNC2 packets and returns instances of APRSPackets
 */
public class Parser {
	
	public static void main( String[] args ) {
		if ( args.length > 0 ) {
			try {
				APRSPacket packet = Parser.parse(args[0]);
				System.out.println("Packet parsed as a "+packet.getType());
				System.out.println("From:	"+packet.getSourceCall());
				System.out.println("To:	"+packet.getDestinationCall());
				System.out.println("Via:	"+packet.getDigiString());
				System.out.println("DTI:	"+packet.getDti());
				System.out.println("Valid:	"+packet.isAprs());
				InformationField data = packet.getAprsInformation();
				System.out.println("Data:	" + data);
				if ( packet.isAprs() && data != null) {
					System.out.println("    Type:	" + data.getClass().getName());
					System.out.println("    Messaging:	" + data.canMessage);
					System.out.println("    Comment:	" + data.getComment());
					System.out.println("    Extension:	" + data.getExtension());
					if (data instanceof PositionPacket)
						System.out.println("    Position:       " + ((PositionPacket)data).getPosition());
					if (data instanceof ObjectPacket)
						System.out.println("    Position:       " + ((ObjectPacket)data).getPosition());
				}
			} catch ( Exception ex ) {
				System.err.println("Unable to parse:  "+ex);
				ex.printStackTrace();
			}
		}
	}
    
    public static APRSPacket parsePacket(byte[] rawPacket) {
        //if ( packet.getDti() == '!' || packet.getDti() == '=' ) {
            // !3449.94N/08448.56W_203/000g000t079P133h85b10149OD1
        return new APRSPacket(null, null, null, null);
    }
    
    public static APRSPacket parse(final String packet) throws Exception {
        int cs = packet.indexOf('>');
        String source = packet.substring(0,cs).toUpperCase();
        int ms = packet.indexOf(':');
        String digiList = packet.substring(cs+1,ms);
        String[] digiTemp = digiList.split(",");
        String dest = digiTemp[0].toUpperCase();
        ArrayList<Digipeater> digis = Digipeater.parseList(digiList, false);
        String body = packet.substring(ms+1);
        APRSPacket ap = parseBody(source, dest, digis, body);
        ap.setOriginalString(packet);
        return ap;
    }

    public static APRSPacket parseAX25(byte[] packet) throws Exception {
	    int pos = 0;
	    String dest = new Callsign(packet, pos).toString();
	    pos += 7;
	    String source = new Callsign(packet, pos).toString();
	    pos += 7;
	    ArrayList<Digipeater> digis = new ArrayList<Digipeater>();
	    while ((packet[pos - 1] & 1) == 0) {
		    Digipeater d =new Digipeater(packet, pos);
		    digis.add(d);
		    pos += 7;
	    }
	    if (packet[pos] != 0x03 || packet[pos+1] != -16 /*0xf0*/)
		    throw new IllegalArgumentException("control + pid must be 0x03 0xF0!");
	    pos += 2;
	    String body = new String(packet, pos, packet.length - pos);
	    return parseBody(source, dest, digis, body);
    }

    public static APRSPacket parseBody(String source, String dest, ArrayList<Digipeater> digis, String body) throws Exception {
        byte[] bodyBytes = body.getBytes();
        byte dti = bodyBytes[0];
        InformationField infoField = null;
        APRSTypes type = APRSTypes.T_UNSPECIFIED;
        boolean hasFault = false;
        switch ( dti ) {
        	case '!':
        	case '=':
        	case '/':
        	case '@':
        	case '`':
        	case '\'':
        	case '$':
        		if ( body.startsWith("$ULTW") ) {
        			// Ultimeter II weather packet
        		} else {
        			type = APRSTypes.T_POSITION;
        			infoField = new PositionPacket(bodyBytes,dest);
        		}
    			break;
        	case ':':
        		infoField = new MessagePacket(bodyBytes,dest);
                        type = APRSTypes.T_MESSAGE;
        		break;
    		case ';':
    			if (bodyBytes.length > 29) {
    				//System.out.println("Parsing an OBJECT");
				type = APRSTypes.T_OBJECT;
    				infoField = new ObjectPacket(bodyBytes);
    			} else {
    				System.err.println("Object packet body too short for valid object");
    				hasFault = true; // too short for an object
    			}
    			break;
    		case '>':
    			type = APRSTypes.T_STATUS;
    			break;
    		case '<':
    			type = APRSTypes.T_STATCAPA;
    			break;
    		case '?':
    			type = APRSTypes.T_QUERY;
    			break;
    		case ')':
			type = APRSTypes.T_ITEM;
    			if (bodyBytes.length > 18) {
				infoField = new ItemPacket(bodyBytes);
    			} else {
    				hasFault = true; // too short
    			}
    			break;
    		case 'T':
    			if (bodyBytes.length > 18) {
    				//System.out.println("Parsing TELEMETRY");
    				//parseTelem(bodyBytes);
    			} else {
    				hasFault = true; // too short
    			}
    			break;
    		case '#': // Peet Bros U-II Weather Station
    		case '*': // Peet Bros U-II Weather Station
    		case '_': // Weather report without position
    			type = APRSTypes.T_WX;
    			break;
    		case '{':
    			type = APRSTypes.T_USERDEF;
    			break;
    		case '}': // 3rd-party
    			type = APRSTypes.T_THIRDPARTY;
    			break;

    		default:
    			hasFault = true; // UNKNOWN!
    			break;

        }
	if (infoField == null)
		infoField = new UnsupportedInfoField(bodyBytes);
        APRSPacket returnPacket = new APRSPacket(source,dest,digis,infoField);
        returnPacket.setType(type);
        returnPacket.setHasFault(hasFault);
        return returnPacket;
        
    }
    
}
