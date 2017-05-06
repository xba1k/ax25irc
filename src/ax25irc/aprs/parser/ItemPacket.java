package ax25irc.aprs.parser;

import java.io.Serializable;

public class ItemPacket extends ObjectPacket implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean live = true;

	/** parse an APRS item message
	 * @return new ItemPacket instance with the parsed data
	 */
	public ItemPacket(byte[] msgBody) throws Exception {
		this.rawBytes = msgBody;
		setDataTypeIdentifier((char)rawBytes[0]);
		String body = new String(msgBody);
		int name_length = body.indexOf("!") - 1;
		if (name_length < 1 || name_length > 9) {
			name_length = body.indexOf("_");
			if (name_length < 1 || name_length > 9)
				throw new Exception("Invalid ITEM packet, missing '!' or '_'.");
			this.live = false;
		} else this.live = true;
		this.objectName = new String(msgBody, 1, name_length).trim();
		int cursor = name_length + 2;
		if ( msgBody[cursor] > '0' && msgBody[cursor] < '9' ) {
		    this.position = PositionParser.parseUncompressed(msgBody, cursor);
		    cursor += 19;
		} else {
		    this.position = PositionParser.parseCompressed(msgBody, cursor);
		    cursor += 12;
		}
		comment = new String(msgBody, cursor, msgBody.length - cursor, "UTF-8").trim();
	}

	public ItemPacket( String objectName, boolean live, Position position, String comment) {
		super(objectName, live, position, comment);
	}

	@Override
	public String toString() {
		if (rawBytes != null)
			return new String(rawBytes);
		return ")"+this.objectName+( live ? "!":"_")+position.toString()+comment;
	}
}
