package ax25irc;

import ax25irc.ax25modem.sivantoledo.ax25.Packet;

public class AX25FileTransfer extends AX25Packet {
    
    public enum TransferState {
        
        HEADER,
        DATA
        
    };
    
    TransferState state;
    
    public AX25FileTransfer(Packet frame) {
        this.type = PacketType.AX25FileTransfer;
        this.frame = frame;
 
        switch(frame.payload[1]) {
            
            case '1' : state = TransferState.HEADER; break;
            case '2' : state = TransferState.DATA; break;
            
        }
        
    }
    
    public TransferState getState() {
       return state;
    }
    
}
