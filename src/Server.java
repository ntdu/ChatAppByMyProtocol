import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class Server {
	
	static int Port;
	
	static LinkedList<User> UserList;
	static LinkedList<User> UserOnline;
	
	static int groupNumber;
	static LinkedList<GroupChat> groupChats;
	
	static String fileUser;
	
	////////////////////////////////////////////////////////////////
	// Ham doc va ghi file
	public static void writeLine(String fileName, String aLine) {
		try {
			
			File file = new File(fileName);
			
			if(!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(aLine);
			bw.newLine();
			
			bw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeFile(String fileName, LinkedList<User> list) {
		try {
			
			File file = new File(fileName);
			
			file.createNewFile();
			
			FileWriter fw = new FileWriter(file.getAbsolutePath(), false);
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(User user : list) {
				String line = user.getUserName() + " " + user.getPassWord();
				bw.write(line);
				bw.newLine();
			}
			bw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Boolean readLine(String fileName, String line) {
		try {
			File file = new File(fileName);
			
			if(!file.exists()) {
				return false;
			}
			
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			line = br.readLine();
			br.close();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static User convertStringToUser(String line) {
		User user = new User();
		if(line != null && line != "") {
			int size = line.length();
			int begin = 0;
			int end = 0;
			while(end < size && line.charAt(end) != '\0' && line.charAt(end) != ' ') {
				end++;
			}
			user.setUserName(line.substring(begin, end));
			begin = ++end;
			
			while(end < size && line.charAt(end) != '\0' && line.charAt(end) != ' ') {
				end++;
			}
			user.setPassWord(Integer.parseInt(line.substring(begin, end)));
		}
		return user;
	}
	
	public static Boolean readFile(String fileName, LinkedList<User> list) {
		try {
			File file = new File(fileName);
			
			if(!file.exists()) {
				return false;
			}
			
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line = br.readLine()) != null) {
				list.add(convertStringToUser(line));
			}
			br.close();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	///////////////////////////////////////////////////////////////
	
	
	public static class GroupChat {
		int GroupNumber;
		LinkedList<User> UserListIntoGroup;
		LinkedList<String> FileList;
		
		public GroupChat(int groupNumber, LinkedList<String> userNames) {
			GroupNumber = groupNumber;
			UserListIntoGroup = new LinkedList<User>();
			FileList = new LinkedList<String>();
			for (String userName : userNames) {
				for (User user : UserOnline) {
					if(user.getUserName().equals(userName)) {
						UserListIntoGroup.add(user);
					}
				}
			}
		}
		
		public void sendMS(String userName, String ms) {
			for(User user : UserListIntoGroup) {
				if(user.getUserName().equals(userName)) {
					continue;
				}
				try {
					user.getDout().writeUTF(ms);
					user.getDout().flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public int getGroupNumber() {
			return GroupNumber;
		}
		
		public LinkedList<User> getUserList() {
			return UserListIntoGroup;
		}
		
		public void sendMS(String ms) {
			for (User user : UserListIntoGroup) {
				try {
					user.getDout().writeUTF(ms);
					user.getDout().flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public LinkedList<String> getFileList() {
			return FileList; 	
		}
		
		public void addFile(String fileName) {
			FileList.add(fileName);
		}
	}
	
	
	public static class Service implements Runnable {
		private User user;
		private Socket socket;
		
		//khoi tao String message
		private String msClient = "";
		
		public Service(Socket theConnection) {
			try {
				socket = theConnection;
				user = new User();
				user.setDin(socket.getInputStream());
				user.setDout(socket.getOutputStream());
				user.setIPAddress(socket.getInetAddress().getHostAddress());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		public void run() {
			try {
				//khoi tao va chay thread phan tich ms
				AnalysisMessage ThreadShowText = new AnalysisMessage();
	            Thread thread = new Thread(ThreadShowText);
	            thread.start();
	            thread.join();
	            
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//class phan tich ms
		public class AnalysisMessage implements Runnable {
			public void run() {
				try {
					while(true) {
						if(!socket.isClosed()) {
							//lay ms tu client
							msClient = user.getDin().readUTF();
							if(msClient.equals("EXIT")) {
								System.exit(0);
							}
							//giai ma ms
							DecodingMS(user, msClient, socket);
							//xuat ms cua client ra console
							System.out.println(user.getUserName() + ": " + msClient);
						}
						else {
							break;
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		//ham giai ma ms
		public static void DecodingMS(User user, String ms, Socket socket) {
			int[] index = new int[2];
			index[0] = 0;
			index[1] = -1;
			checkSpace(ms, index);
			if(ms.substring(index[0], index[1]).equals("SIGNIN")) {
				checkSpace(ms, index);
				String userName = ms.substring(index[0], index[1]);
				
				checkSpace(ms, index);
				String pw = ms.substring(index[0], index[1]);
				
				if(CheckSignIn(userName, user, pw)) {
					try {
						user.getDout().writeUTF("SIGNIN SUCCESS");
						user.getDout().flush();
						
						String message = "USERLIST";
						for (User user1 : UserOnline) {
							message += " " + user1.getUserName();
						}
						
						for (User user2 : UserOnline) {
							user2.getDout().writeUTF(message);
							user2.getDout().flush();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						user.getDout().writeUTF("SIGNIN FAILED");
						user.getDout().flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else if(ms.substring(index[0], index[1]).equals("SIGNUP")) {
				checkSpace(ms, index);
				String userName = ms.substring(index[0], index[1]);
				
				checkSpace(ms, index);
				String pw = ms.substring(index[0], index[1]);
				
				if(CheckSignUp(userName, pw)) {
					try {
						user.getDout().writeUTF("SIGNUP SUCCESS");
						user.getDout().flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						user.getDout().writeUTF("SIGNUP DUPLICATE");
						user.getDout().flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else if(ms.substring(index[0], index[1]).equals("MESSAGE")) {
				checkSpace(ms, index);
				String groupNumber = ms.substring(index[0], index[1]);
				checkSpace(ms, index);
				String userName = ms.substring(index[0], index[1]);
				try {
					int group = Integer.parseInt(groupNumber);
					for (GroupChat groupChat : groupChats) {
						if(groupChat.GroupNumber == group) {
							groupChat.sendMS(userName, ms);
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if(ms.substring(index[0], index[1]).equals("CHAT")) {
				LinkedList<String> userNames = new LinkedList<String>();
				while(index[1] < ms.length()) {
					checkSpace(ms, index);
					userNames.add(ms.substring(index[0], index[1]));
				}
				userNames.add(user.getUserName());
				groupChats.add(new GroupChat(groupNumber++, userNames));
				
				String message = "CHAT " + groupChats.getLast().getGroupNumber();
				for (String userName : userNames) {
					message += " " + userName;
				}
				
				groupChats.getLast().sendMS(message);
			}
			else if(ms.substring(index[0], index[1]).equals("LOGOUT")) {
				try {
					user.getDout().writeUTF("LOGOUT");
					user.getDout().flush();
					
					for(int i = 0; i < UserOnline.size(); i++) {
						if(UserOnline.get(i).getUserName().equals(user.getUserName())) {
							UserOnline.remove(i);
							break;
						}
					}
					
					for (GroupChat groupChat : groupChats) {
						for (int i = 0; i < groupChat.getUserList().size(); i++) {
							if(groupChat.getUserList().get(i).getUserName().equals(user.getUserName())) {
								groupChat.getUserList().remove(i);
								break;
							}
						}
					}
					
					for (User user3 : UserOnline) {
						user3.getDout().writeUTF("LOGOUT " + user.getUserName());
						user3.getDout().flush();
					}
					
					for (User user1 : UserOnline) {
						System.out.println(user1.getUserName());
					}
					
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if(ms.substring(index[0], index[1]).equals("CLOSE")) {
				checkSpace(ms, index);
				String groupNumber = ms.substring(index[0], index[1]);
				
				for (GroupChat groupChat : groupChats) {
					if(groupChat.getGroupNumber() == Integer.parseInt(groupNumber)) {
						for(int i = 0; i < groupChat.getUserList().size(); i++) {
							if(groupChat.getUserList().get(i).getUserName().equals(user.getUserName())) {
								groupChat.getUserList().remove(i);
								break;
							}
						}
						
						String message = "CLOSE " + groupNumber + " " + user.getUserName();
						
						for (User user1 : groupChat.getUserList()) {
							try {
								user1.getDout().writeUTF(message);
								user1.getDout().flush();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			else if(ms.substring(index[0], index[1]).equals("UPLOAD")) {
				checkSpace(ms, index);
				String groupNumber = ms.substring(index[0], index[1]);
				String fileName = ms.substring(index[1] + 1, ms.length());
				try {
					File file = new File(fileName);
					if(file.exists()) {
						user.getDout().writeUTF("UPLOAD " + groupNumber + " REFUSE");
						user.getDout().flush();
					}
					else {
						user.getDout().writeUTF("UPLOAD " + groupNumber + " ACEPT");
						user.getDout().flush();
						
						for (GroupChat groupChat : groupChats) {
							if(groupChat.getGroupNumber() == Integer.parseInt(groupNumber)) {
								for (User user1 : groupChat.getUserList()) {
									if(!user1.getUserName().equals(user.getUserName())) {
										user1.getDout().writeUTF("FILE " + groupNumber + " " + fileName);
									}
								}
								groupChat.addFile(fileName);
								break;
							}
						}
						file.createNewFile();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if(ms.substring(index[0], index[1]).equals("FILE")) {
				index[0] = ++index[1];
				
				while (ms.charAt(index[1]) != '#') {
					index[1]++;
				}
				
				String fileName = ms.substring(index[0], index[1]);
				String data = ms.substring(index[1] + 1, ms.length());
				
				try {
					File file = new File(fileName);
					FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(data);
					bw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if(ms.substring(index[0], index[1]).equals("DOWNLOAD")) {
				checkSpace(ms, index);
				String groupNumber = ms.substring(index[0], index[1]);
				String fileName = ms.substring(index[1] + 1, ms.length());
				File file = new File(fileName);
				if(file.exists()) {
					try {
						user.getDout().writeUTF("DOWNLOAD " + groupNumber + " ACEPT");
						user.getDout().flush();
						
						FileInputStream fin = new FileInputStream(file);

						String CODE = "DATA ";
						int sizeData = 4096;
						byte[] sendData = new byte[sizeData];
						int byteNumber = 0;
						while ((byteNumber = fin.read(sendData)) != -1) {
							user.getDout().writeUTF(CODE + new String(sendData).substring(0, byteNumber));
							user.getDout().flush();
						}
						fin.close();
						
						user.getDout().writeUTF("EOF");
						user.getDout().flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						user.getDout().writeUTF("DOWNLOAD " + groupNumber + " REFUSE");
						user.getDout().flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		// return 0(jpg, png), 1(txt)
		public static int getFileFormat(String fileName) {
			int begin = fileName.length() - 1;
			while(fileName.charAt(begin) != '.') {
				begin--;
			}
			String format = fileName.substring(begin + 1, fileName.length());
			if(format.equals("txt")) {
				return 0;
			}
			else {
				return 1;
			}
		}
		
		public static Boolean CheckSignUp(String userName, String pw) {
			if(!CheckUserList(UserList, userName)) {
				UserList.add(new User(userName, pw.hashCode()));
				
				for (User user : UserList) {
					System.out.println(user.getUserName() + " " + user.getPassWord());
				}
				
				writeFile(fileUser, UserList);
				
				System.out.println("Signin is successed!");
				return true;
			}
			else {
				System.out.println("Signin is failed!");
				return false;
			}
		}
		
		public static Boolean CheckUserList(LinkedList<User> userList, String userName) {
			for (User user : userList) {
				if(user.getUserName().equals(userName)) {
					return true;
				}
			}
			return false;
		}
		
		public static Boolean CheckSignIn(String userName, User user, String pw) {
			if(!CheckUserList(UserOnline, userName, pw.hashCode())) {
				if(CheckUserList(UserList, userName, pw.hashCode())) {
					user.setUserName(userName);
					user.setPassWord(pw.hashCode());
					UserOnline.add(user);
					System.out.println("Signin is successed!");
					
					for (User user1 : UserOnline) {
						System.out.println(user.getUserName() + " " + user1.getPassWord());
					}
					
					return true;
				}
			}
			System.out.println("Signin is failed!");
			return false;
		}
		
		public static void checkSpace(String ms, int[] index) {
			index[0] = index[1] + 1;
			index[1] = index[0];
			while(ms != null && index[1] < ms.length() && ms.charAt(index[1]) != ' ') {
				index[1] += 1;
			}
		}
		
		public static Boolean CheckUserList(LinkedList<User> userList, String userName, int pw) {
			for (User user : userList) {
				if(user.getUserName().equals(userName) && user.getPassWord() == pw) {
					return true;
				}
			}
			return false;
		}
	}
	
	public static void CreateSevice(Socket socket) {
		try {
			Service service = new Service(socket);
			Thread thread = new Thread(service);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//ham chinh
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			InetAddress[] address = InetAddress.getAllByName(localhost.getHostName());
			
			Port = 6666;
			System.out.println("Port: " + Port);
			
			fileUser = "UserList.txt";
			
			UserList = new LinkedList<User>();
			UserOnline = new LinkedList<User>();
			
			if(!readFile(fileUser, UserList)) {
				System.out.println("Read file is failed!");
			}
			else {
				System.out.println("Read file is success!");
			}
		
			for(User user : UserList) {
				System.out.println(user.getUserName() + " " + user.getPassWord());
			}
			
			groupNumber = 0;
			groupChats = new LinkedList<GroupChat>();
			
			ServerSocket theServer = new ServerSocket(Port);
			
			String addresses = "";
			for(int i = 0; i < address.length; i++) {
				addresses += address[i].toString() + '\n';
				System.out.println(address[i].toString());
			}
			
			JFrame frame = new JFrame("My IP address");
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int height = screenSize.height/2;
			int width = screenSize.width/2;
			frame.setBounds(width - 150, height - 100, 300, 250);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.getContentPane().setLayout(null);
			
			JTextArea textArea = new JTextArea();
			textArea.setEditable(false);
			textArea.setText(addresses);
		    
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setBounds(0, 0, 300, 172);
			scrollPane.setWheelScrollingEnabled(true);
			frame.getContentPane().add(scrollPane);
			
			JLabel label = new JLabel("Server is running...");
			label.setBounds(95, 183, 106, 23);
			frame.getContentPane().add(label);
			
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				@SuppressWarnings("deprecation")
				@Override
        	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        	    	try {
        	    		for (User user1 : UserOnline) {
    						user1.getDout().writeUTF("EXIT");
    						user1.getDout().flush();
    					}
        	    		frame.disable();
        	    		System.exit(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
        	    }
        	});
			
			frame.setResizable(false);
			frame.setVisible(true);
			
			//Lang nghe client
			while (true) {
				CreateSevice(theServer.accept());
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
