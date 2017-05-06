package ax25irc.aprs.parser;

import java.io.Serializable;

public class ObjectPacket extends InformationField implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String objectName;
	protected boolean live = true;
	protected Position position;

	protected ObjectPacket() {
	}

	/** parse an APRS object message
	 * @return new ObjectPacket instance with the parsed data
	 */
	public ObjectPacket(byte[] msgBody) throws Exception {
		super(msgBody);
		this.objectName = new String(msgBody, 1, 9).trim();
		this.live = (msgBody[10] == '*');
		int cursor = 18;
		if ( msgBody[cursor] > '0' && msgBody[cursor] < '9' ) {
		    this.position = PositionParser.parseUncompressed(msgBody, cursor);
		    cursor += 19;
		} else {
		    this.position = PositionParser.parseCompressed(msgBody, cursor);
		    cursor += 12;
		}
		comment = new String(msgBody, cursor, msgBody.length - cursor, "UTF-8").trim();
	}

	public ObjectPacket( String objectName, boolean live, Position position, String comment) {
		this.objectName = objectName;
		this.live = live;
		this.position = position;
		this.comment = comment;
	}

	/**
	 * @return the objectName
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 * @param objectName the objectName to set
	 */
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	/**
	 * @return the live
	 */
	public boolean isLive() {
		return live;
	}

	/**
	 * @param live the live to set
	 */
	public void setLive(boolean live) {
		this.live = live;
	}

	/**
	 * @return the position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Position position) {
		this.position = position;
	}

	/**
	 * @param extension the extension to set
	 */
	public void setExtension(DataExtension extension) {
		this.extension = extension;
	}

	@Override
	public String toString() {
		if (rawBytes != null)
			return new String(rawBytes);
		return String.format(";%-9s%c%s%s", this.objectName, live ? '*':'_', position.toString(), comment);
	}
}
