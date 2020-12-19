

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class User {
	private String UserName;
	private int PassWordHashCode;
	private String IPAddress;
	private DataInputStream din;
	private DataOutputStream dout;
	
	public User() {
		UserName = "";
		PassWordHashCode = 0;
		IPAddress = "";
		din = null;
		dout = null;
	}
	
	public User(String user, int pw, String ip) {
		UserName = user;
		PassWordHashCode = pw;
		IPAddress = ip;
	}
	
	public User(String user, int pw) {
		UserName = user;
		PassWordHashCode = pw;
	}
	
	public User(User user) {
		UserName = user.getUserName();
		PassWordHashCode = user.getPassWord();
		IPAddress = user.getIPAddress();
	}
	
	public String getUserName() {
		return UserName;
	}
	
	public int getPassWord() {
		return PassWordHashCode;
	}
	
	public String getIPAddress() {
		return IPAddress;
	}
	
	public void setUserName(String user) {
		UserName = user;
	}
	
	public void setPassWord(int pw) {
		PassWordHashCode = pw;
	}
	
	public void setIPAddress(String ip) {
		IPAddress = ip;
	}

	public DataInputStream getDin() {
		return din;
	}
	
	public void setDin(InputStream inputStream) {
		din = new DataInputStream(inputStream);
	}
	
	public DataOutputStream getDout() {
		return dout;
	}
	
	public void setDout(OutputStream outputStream) {
		dout = new DataOutputStream(outputStream);
	}
}
