package team085;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Comms {
	public static enum Type {
		NONE,
		CONVERGE;
	}
	
	public static class Message {
		Type type;
		MapLocation loc;
		int val;
		int id;
		
		public Message(Type type, MapLocation loc, int val, int id) {
			this.type = type;
			this.loc = loc;
			this.val = val;
			this.id = id;
		}
		
		public static Message create(Type type, MapLocation loc, int val, int id) {
			return new Message(type, loc, val, id);
		}
		
		public int encode() {
			int m = 0;
			m |= type.ordinal() & 0xFF;
			if (loc != null) {
				m |= (loc.x & 0xFF) << 8;
				m |= (loc.y & 0xFF) << 16;
			}
			m |= (val & 0xF) << 24;
			m |= (id & 0xF) << 28;
			return m;
		}
		
		public static Message decode(int m) {
			int t = (m) & 0xFF;
			if (t > 0) {
				int x = (m >>> 8) & 0xFF;
				int y = (m >>> 16) & 0xFF;
				int v = (m >>> 24) & 0xF;
				int id = (m >>> 28) & 0xF;
				return Message.create(Type.values()[t], new MapLocation(x,y), v, id);
			} else {
				return null;
			}
		}
	}
	
	public static void BroadcastMessage(RobotController rc, Message message) throws GameActionException {
		rc.broadcast(0, message.encode());
	}
	
	public static Message ReadMessage(RobotController rc) throws GameActionException {
		int id = rc.getRobot().getID();	
		Message m = Message.decode(rc.readBroadcast(0));
		if (m == null || m.id == id ) {
			return null;
		} else if (m.type == Type.CONVERGE) {
			m.val--;
			if (m.val > 0) {
				BroadcastMessage(rc, m);
			} else {
				BroadcastMessage(rc, new Message(Type.NONE, null, 0, rc.getRobot().getID()));
			}
		}
		return m;
	}
}

