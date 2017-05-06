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

 *  Please note that significant portions of this code were taken from the JAVA FAP
 *  conversion by Matti Aarnio at http://repo.ham.fi/websvn/java-aprs-fap/
 */
package ax25irc.aprs.parser;


public class PositionPacket extends InformationField implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private Position position;
	private String positionSource;
	private boolean compressedFormat;

	public PositionPacket(byte[] msgBody, String destinationField)
			throws Exception {
		super(msgBody);
		positionSource = "Unknown";
		char packetType = (char) msgBody[0];
		int cursor = 0;
		switch (packetType) {
		case '\'' :
		case '`': // Possibly MICe
			// (char)packet.length >= 9 ?
			type = APRSTypes.T_POSITION;
			position = PositionParser.parseMICe(msgBody, destinationField);
			this.extension = PositionParser.parseMICeExtension(msgBody, destinationField);
			positionSource = "MICe";
			cursor = 10;
			if (cursor < msgBody.length && (msgBody[cursor] == '>' || msgBody[cursor] == ']' || msgBody[cursor] == '`'))
				cursor++;
			if (cursor < msgBody.length && msgBody[cursor] == '"')
				cursor += 4;
			break;
		case '!':
			if (msgBody[1] == 'U' && // "$ULT..." -- Ultimeter 2000 weather
					// instrument
					msgBody[2] == 'L' && msgBody[3] == 'T') {
				type = APRSTypes.T_WX;
				break;
			}
		case '=':
		case '/':
		case '@':
			if (msgBody.length < 10) { // Too short!
				hasFault = true;
			} else {

				// Normal or compressed location packet, with or without
				// timestamp, with or without messaging capability
				// ! and / have messaging, / and @ have a prepended
				// timestamp

				type = APRSTypes.T_POSITION;
				cursor = 1;

				if (packetType == '/' || packetType == '@') {
					// With a prepended timestamp, jump over it.
					cursor += 7;
				}
				char posChar = (char) msgBody[cursor];
				if (validSymTableCompressed(posChar)) { /* [\/\\A-Za-j] */
					// compressed position packet
					position = PositionParser.parseCompressed(msgBody, cursor);
					this.extension = PositionParser.parseCompressedExtension(msgBody, cursor);
					positionSource = "Compressed";
					cursor += 13;
				} else if ('0' <= posChar && posChar <= '9') {
					// normal uncompressed position
					position = PositionParser.parseUncompressed(msgBody);
					try {
						this.extension = PositionParser.parseUncompressedExtension(msgBody, cursor);
					} catch (ArrayIndexOutOfBoundsException oobex) {
						this.extension = null;
					}
					positionSource = "Uncompressed";
					cursor += 19;
				} else {
					hasFault = true;
				}
				break;
			}
		case '$':
			if (msgBody.length > 10) {
				type = APRSTypes.T_POSITION;
				position = PositionParser.parseNMEA(msgBody);
				positionSource = "NMEA";
			} else {
				hasFault = true;
			}
			break;

		}
		if (cursor > 0 && cursor < msgBody.length) {
			comment = new String(msgBody, cursor, msgBody.length - cursor, "UTF-8");
		}
		compressedFormat = false;
	}
	public PositionPacket(Position position, String comment) {
		this.position = position;
		this.type = APRSTypes.T_POSITION;
		this.comment = comment;
		compressedFormat = false;
	}

	public PositionPacket(Position position, String comment, boolean msgCapable) {
		this(position, comment);
		canMessage = msgCapable;
	}

	public void setCompressedFormat(boolean val) {
		compressedFormat = val;
	}

	public boolean getCompressedFormat() {
		return compressedFormat;
	}

	private boolean validSymTableCompressed(char c) {
		if (c == '/' || c == '\\')
			return true;
		if ('A' <= c && c <= 'Z')
			return true;
		if ('a' <= c && c <= 'j')
			return true;
		return false;
	}

	/*
	 * private boolean validSymTableUncompressed(char c) { if (c == '/' || c ==
	 * '\\') return true; if ('A' <= c && c <= 'Z') return true; if ('0' <= c &&
	 * c <= '9') return true; return false; }
	 * 
	 * public String toString() { return "Latitude:  " + position.getLatitude()
	 * + ", longitude: " + position.getLongitude(); }
	 */

	/**
	 * @return the position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public String toString() {
		if (rawBytes != null)
			return new String(rawBytes);
		if (compressedFormat)
			return (canMessage ? "=" : "!") + position.toCompressedString() + comment;
		return (canMessage ? "=" : "!") + position + comment;
	}

	/**
	 * @return the positionSource
	 */
	public String getPositionSource() {
		return positionSource;
	}
}
