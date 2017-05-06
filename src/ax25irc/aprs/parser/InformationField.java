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

import java.io.Serializable;

/**
 * 
 * @author johng
 *  This class represents the "payload" of a TNC2 style AX25 packet, stripped of the source call,
 *  destination call, and digi VIAs.  Note this class is abstract:  only subclasses of it may be 
 *  instantiated.  Per the APRS spec, these classes include Position, Direction Finding, Objects
 *  and Items, Weather, Telemetry, Messages, Bulletins, Annoucements, Queries, Responses, Statuses,
 *  and User-defined Others.
 */
public abstract class InformationField implements Serializable {
	private static final long serialVersionUID = 1L;
	private char dataTypeIdentifier;
    protected byte[] rawBytes;
    protected APRSTypes type;
	protected boolean hasFault = false;
    protected boolean canMessage = false;
    DataExtension extension = null;
	protected String comment = "";

    public InformationField() {
    }
    
    public InformationField( byte[] rawBytes ) {
        if ( rawBytes.length < 1 ) {
            System.err.println("Parse error:  zero length information field");
        }
        this.rawBytes = rawBytes;
        this.dataTypeIdentifier = (char)rawBytes[0];
        switch ( dataTypeIdentifier ) {
        	case '@' :
        	case '=' :
        	case '\'':
        	case ':' : this.canMessage = true;
        }
    }
    
    public char getDateTypeIdentifier() {
        return dataTypeIdentifier;
    }

    public void setDataTypeIdentifier(char dti) {
        this.dataTypeIdentifier = dti;
    }

    /**
     * @return the rawBytes
     */
    public byte[] getRawBytes() {
	if (rawBytes != null)
		return rawBytes;
	else
		return toString().getBytes();
    }
    
    public byte[] getBytes(int start, int end) {
        byte[] returnArray = new byte[end-start];
        System.arraycopy(getRawBytes(), start, returnArray, 0, end-start);
        return returnArray;
    }
    
	/**
	 * @return the comment string which was embedded in the packet
	 */
    public String getComment() {
        return comment;
    }
    
    @Override
    public String toString() {
        return new String(rawBytes);
    }
	/**
	 * @return the hasFault
	 */
	public boolean isHasFault() {
		return hasFault;
	}

	/**
	 * @return the extension
	 */
	public final DataExtension getExtension() {
		return extension;
	}
}
