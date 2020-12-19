import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.MaskFormatter;

public class Client {
	static int Port;
	static Socket Client;
	
	static User user;
	static LinkedList<String> UserOnline;
	
	static SignIn signIn;
	
	static JFrame frame;
	
	static FormHome formHome;
	
	static String path;
	static volatile Boolean busy;
	
	//khoi tao String message
	static String msHost = "";
	
	// class phan tich ms
	public static class AnalysisMessage implements Runnable {
		public void run() {
			try {
				while(true) {
					//lay ms cua server gui den
					msHost = user.getDin().readUTF();
					System.out.println(msHost);
					//giai ma ms
					DecodingMS(msHost);
					//xuat ms cua server len console
					System.out.println("Server: " + msHost);
					if(msHost.equals("LOGOUT") || msHost.equals("EXIT")) {
						System.exit(0);
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Boolean CheckSignIn(String ms) {
		if(ms.equals("SINGIN SUCCESS")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void CheckSpace(String ms, int[] index) {
		index[0] = index[1] + 1;
		index[1] = index[0];
		while(ms != null && index[1] < ms.length() && ms.charAt(index[1]) != ' ') {
			index[1] += 1;
		}
	}
	
	@SuppressWarnings("static-access")
	public static void DecodingMS(String ms) {
		int[] index = new int[2];
		index[0] = 0;
		index[1] = -1;
		CheckSpace(ms, index);
		if(ms.substring(index[0], index[1]).equals("SIGNIN")) {
			CheckSpace(ms, index);
			if(ms.substring(index[0], index[1]).equals("SUCCESS")) {
				signIn.setUser();
				formHome = new FormHome();
				formHome.CreateFormHome();
				System.out.println("Signin is success!");
			}
			else {
				signIn.setSignInFailed();
			}
		}
		else if(ms.substring(index[0], index[1]).equals("SIGNUP")) {
			CheckSpace(ms, index);
			if(ms.substring(index[0], index[1]).equals("SUCCESS")) {
				signIn.signUp.setSignUp();
			}
			else {
				signIn.signUp.setSignUpFailed();
			}
		}
		else if(ms.substring(index[0], index[1]).equals("MESSAGE")) {
			CheckSpace(ms, index);
			String groupNumber = ms.substring(index[0], index[1]);
			CheckSpace(ms, index);
			String userName = ms.substring(index[0], index[1]);
			///////////////////////////////////////////////////
			String message = ms.substring(index[1] + 1, ms.length());
			
			int group = Integer.parseInt(groupNumber);
			
			for (FormGroupChat groupChat : formHome.getGroupChat()) {
				if(groupChat.getGroupNumber() == group) {
					groupChat.appendTextArea(userName, message);
					break;
				}
			}
		}
		else if(ms.substring(index[0], index[1]).equals("USERLIST")) {
			System.out.println(ms);
			LinkedList<String> userNames = new LinkedList<String>();
			while (index[1] < ms.length()) {
				CheckSpace(ms, index);
				userNames.add(ms.substring(index[0], index[1]));
			}
			
			userNames.remove(userNames.indexOf(user.getUserName()));
			
			while (UserOnline.size() > 0) {
				UserOnline.remove();
			}
			
			formHome.removeAll();
			
			for (String userName : userNames) {
				UserOnline.add(userName);
				formHome.appendUser(userName);
			}
			
			String message = "";
			for (String userName : UserOnline) {
				message += " " + userName;
			}
			
			System.out.println(message);
		}
		else if(ms.substring(index[0], index[1]).equals("CHAT")) {
			CheckSpace(ms, index);
			String groupNumber = ms.substring(index[0], index[1]);
				
			LinkedList<String> userNames = new LinkedList<String>();
			
			while(index[1] < ms.length()) {
				CheckSpace(ms, index);
				userNames.add(ms.substring(index[0], index[1]));
			}

			userNames.remove(userNames.indexOf(user.getUserName()));
			
			formHome.getGroupChat().add(new FormGroupChat(Integer.parseInt(groupNumber), userNames));
			formHome.getGroupChat().getLast().CreateFormGroupChat();
		}
		else if(ms.substring(index[0], index[1]).equals("LOGOUT")) {
			if(index[1] + 1 < ms.length()) {
				String userName = ms.substring(index[1] + 1, ms.length());
				
				UserOnline.remove(userName);
				
				formHome.removeUser(userName);
				
				for (int i = 0; i < formHome.getGroupChat().size(); i++) {
					formHome.getGroupChat().get(i).removeUser(userName);
					if(formHome.getGroupChat().get(i).getUserList().size() == 0) {
						formHome.getGroupChat().get(i).dispose();
						formHome.getGroupChat().remove(i);
					}
				}
			}
			else {
				try {
					Client.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else if(ms.substring(index[0], index[1]).equals("CLOSE")) {
			CheckSpace(ms, index);
			String groupNumber = ms.substring(index[0], index[1]);
			CheckSpace(ms, index);
			String userName = ms.substring(index[0], index[1]);
			
			for (int i = 0; i < formHome.getGroupChat().size(); i++) {
				if(formHome.getGroupChat().get(i).getGroupNumber() == Integer.parseInt(groupNumber)) {
					formHome.getGroupChat().get(i).removeUser(userName);
					if(formHome.getGroupChat().get(i).getUserList().size() == 0) {
						formHome.getGroupChat().get(i).dispose();
						formHome.getGroupChat().remove(i);
					}
					break;
				}
			}
		}
		else if(ms.substring(index[0], index[1]).equals("UPLOAD")) {
			CheckSpace(ms, index);
			String groupNumber = ms.substring(index[0], index[1]);
			CheckSpace(ms, index);
			String reponse = ms.substring(index[0], index[1]);
			if(reponse.equals("ACEPT")) {
				for (FormGroupChat formGroupChat : formHome.getGroupChat()) {
					if(formGroupChat.GroupNumber == Integer.parseInt(groupNumber)) {
						formGroupChat.appendTextArea("SERVER", "UPLOAD FILE ACEPT");
					}
					break;
				}
				
				try {
					File file = new File(path);
					String fileName = getFileName(path);
					FileInputStream fin = new FileInputStream(file);
					
					String CODE = "FILE " + fileName + "#";
					int sizeData = 4096;
					byte[] sendData = new byte[sizeData];
					int byteNumber = 0;
					while ((byteNumber = fin.read(sendData)) != -1) {
						user.getDout().writeUTF(CODE + new String(sendData).substring(0, byteNumber));
						user.getDout().flush();
					}
					fin.close();
					path = "NULL";
					busy = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				for (FormGroupChat formGroupChat : formHome.getGroupChat()) {
					if(formGroupChat.GroupNumber == Integer.parseInt(groupNumber)) {
						formGroupChat.appendTextArea("SERVER", "UPLOAD FILE REFUSE");
					}
					break;
				}
				path = "NULL";
				busy = false;
			}
		}
		else if(ms.substring(index[0], index[1]).equals("FILE")) {
			CheckSpace(ms, index);
			String groupNumber = ms.substring(index[0], index[1]);
			String fileName = ms.substring(index[1] + 1, ms.length());
			
			for (FormGroupChat formGroupChat : formHome.groupChats) {
				if(formGroupChat.getGroupNumber() == Integer.parseInt(groupNumber)) {
					formGroupChat.appendFile(fileName);
					break;
				}
			}
		}
		else if(ms.substring(index[0], index[1]).equals("DOWNLOAD")) {
			CheckSpace(ms, index);
			String groupNumber = ms.substring(index[0], index[1]);
			CheckSpace(ms, index);
			String reponse = ms.substring(index[0], index[1]);
			if(reponse.equals("ACEPT")) {
				for (FormGroupChat formGroupChat : formHome.getGroupChat()) {
					if(formGroupChat.GroupNumber == Integer.parseInt(groupNumber)) {
						formGroupChat.appendTextArea("SERVER", "DOWNLOAD FILE ACEPT");
					}
					break;
				}
				try {
					File file = new File(path);
					file.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				for (FormGroupChat formGroupChat : formHome.getGroupChat()) {
					if(formGroupChat.GroupNumber == Integer.parseInt(groupNumber)) {
						formGroupChat.appendTextArea("SERVER", "DOWNLOAD FILE REFUSE");
					}
					break;
				}
			}
		}
		else if(ms.substring(index[0], index[1]).equals("DATA")) {
			String data = ms.substring(index[1] + 1, ms.length());
			try {
				File file = new File(path);
				FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(data);
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(ms.substring(index[0], index[1]).equals("EOF")) {
			path = "NULL";
			busy = false;
		}
		else if(ms.substring(index[0], index[1]).equals("EXIT")) {	
			try {
				for (int i = 0; i < formHome.getGroupChat().size(); i++) {
					formHome.getGroupChat().get(i).dispose();
					formHome.getGroupChat().remove(i);
				}
				user.getDout().writeUTF("EXIT");
				user.getDout().flush();
				Client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class IPTextFieldVerifier extends InputVerifier {
	   public boolean verify(JComponent input) {
	      if (input instanceof JFormattedTextField) {
	         JFormattedTextField ftf = (JFormattedTextField)input;
	         AbstractFormatter formatter = ftf.getFormatter();
	         if (formatter != null) {
	            String text = ftf.getText();
	            StringTokenizer st = new StringTokenizer(text, ".");
	            while (st.hasMoreTokens()) {
	               int value = Integer.parseInt((String) st.nextToken());
	               if (value < 0 || value > 255) {
	                  // to prevent recursive calling of the
	                  // InputVerifier, set it to null and
	                  // restore it after the JOptionPane has
	                  // been clicked.
	                  input.setInputVerifier(null);
	                  JOptionPane.showMessageDialog(new Frame(), "Malformed IP Address!", "Error",
	                                                JOptionPane.ERROR_MESSAGE);
	                  input.setInputVerifier(this); 
	                  return false;
	               }
	            }
	            return true;
	         }
	      }
	      return true;
	   }
	}
	
	public static class InputIPAddress implements Runnable {
		LinkedList<String> address;
		volatile Boolean finish = false;
		
		public InputIPAddress(LinkedList<String> address) {
			this.address = address;
		}
		
		public void run() {
			try {
	    		// XXX
	    		JFrame frame = new JFrame();
	    		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    		int height = screenSize.height/2;
	    		int width = screenSize.width/2;
	    		frame.setBounds(width - 150, height - 50, 200, 150);
	    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    		frame.getContentPane().setLayout(null);
	                
	            JLabel lbIP = new JLabel("IP Address:");
	            lbIP.setBounds(10, 20, 100, 20);
	            frame.getContentPane().add(lbIP);
	    		
	    		MaskFormatter formatter = new MaskFormatter("###.###.###.###");
	    	    formatter.setPlaceholderCharacter('0');
	    	
	    	    JFormattedTextField formattedTf = new JFormattedTextField(formatter);
	    	    formattedTf.setInputVerifier(new IPTextFieldVerifier());
	    	    formattedTf.setVisible(true);
	    	    formattedTf.setBounds(80, 20, 100, 20);
	    	    // XXX
	    	    frame.getContentPane().add(formattedTf);
	    	    
	    	    JButton btnOK = new JButton("OK");
	        	btnOK.setBounds(75, 70, 60, 30);
	        	frame.getContentPane().add(btnOK);
	        	btnOK.addActionListener(new ActionListener() {
	    			@Override
	    			public void actionPerformed(ActionEvent e) {
	    				if(address.size() < 1) {
	    					address.add(formattedTf.getText());
	    				}
	    				else {
	    					address.set(0, formattedTf.getText());
	    				}
	    				
	    				finish = true;
	    				frame.dispose();
	    			}
	    		});
	        	
	        	formattedTf.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						btnOK.doClick();
					}
				});
	        	
	        	frame.addWindowListener(new java.awt.event.WindowAdapter() {
	        	    @SuppressWarnings("deprecation")
					@Override
	        	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
	        	    	String ms = "Bạn có chắc muốn đóng cửa sổ?";
	        	    	String title = "Đóng cửa sổ?";
	        	        if (JOptionPane.showConfirmDialog(frame, ms, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
	        	        	finish = true;
	        	        	frame.disable();
	        	        }
	        	    }
	        	});
	        	
	        	frame.getRootPane().setDefaultButton(btnOK);
	        	frame.setResizable(false);
	        	frame.setVisible(true);
	        	
	        	while (!finish) {}
	    	}
	    	catch (Exception e) {
				// TODO: handle exception
	    		e.printStackTrace();
			}
		}
	}
	
	public static class UserNameList {
		LinkedList<JCheckBox> checkBoxs;
		LinkedList<JLabel> labels;
		
		public UserNameList() {
			checkBoxs = new LinkedList<JCheckBox>();
			labels = new LinkedList<JLabel>();
		}
		
		public UserNameList(LinkedList<User> userList) {
			checkBoxs = new LinkedList<JCheckBox>();
			labels = new LinkedList<JLabel>();
			for (User user : userList) {
				checkBoxs.add(new JCheckBox(user.getUserName()));
				labels.add(new JLabel(user.getUserName()));
			}
		}
		
		public LinkedList<JCheckBox> getJCheckBox() {
			return checkBoxs;
		}
		
		public LinkedList<JLabel> getJLabel() {
			return labels;
		}
		
		public void add(String userName) {
			checkBoxs.add(new JCheckBox(userName));
			labels.add(new JLabel(userName));
		}
		
		public JCheckBox getJCheckBox(int index) {
			return checkBoxs.get(index);
		}
		
		public JLabel getJLabel(int index) {
			return labels.get(index);
		}
		
		public JCheckBox getFisrtJCheckBox() {
			return checkBoxs.getFirst();
		}
		
		public JLabel getFisrtJLabel() {
			return labels.getFirst();
		}
		
		public JCheckBox getLastJCheckBox() {
			return checkBoxs.getLast();
		}
		
		public JLabel getLastJLabel() {
			return labels.getLast();
		}
		
		public void remove(int index) {
			checkBoxs.remove(index);
			labels.remove(index);
		}
		
		public int size() {
			return checkBoxs.size();
		}
		
		public void setIsSelectAll(Boolean value) {
			for (JCheckBox checkBox : checkBoxs) {
				checkBox.setSelected(value);
			}
		}
		
		public void setSelect(int index, Boolean value) {
			checkBoxs.get(index).setSelected(value);
		}
	}
	
	// lay ten file tu duong dan
	public static String getFileName(String path) {
		int begin = path.length() - 1;
		while (path.charAt(begin) != '\\') {
			begin--;
		}
		return path.substring(begin + 1, path.length());
	}
	
	// tao tep
	public static void createFile(String fileName) {
		try {
			File file = new File(fileName);
			if(!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// ghi vao tep
	public static void writeFile(String fileName, String aLine) {
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
	
	// Class form chat nhom
	public static class FormGroupChat {
		static int GroupNumber;
		JTextArea textArea;
		UserNameList userNameList;
		volatile JPanel panel;
		FileList fileList;
		volatile JPanel panel1;
		JFrame frame;
		static String fileName;
	
		static int returnVal;
		static JFileChooser chooser;
		static JFileChooser chooser1;
		
		public FormGroupChat() {
			GroupNumber = 0;
		} 
		
		public FormGroupChat(int groupNumber, LinkedList<String> userNames) {
			GroupNumber = groupNumber;
			userNameList = new UserNameList();
			for (String userName : userNames) {
				userNameList.add(userName);
			}
			fileList = new FileList();
		}
		
		public static class LabelFile {
			JLabel label;
			public LabelFile(String labelName) {
				label = new JLabel(labelName);
				
				label.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						try {
							if(!busy) {
								if(path == null || path.equals("NULL")) {
									path = getPath();
									if(!path.equals("NULL")) {
										fileName = label.getText();
										path += "\\" + fileName;
										File file = new File(path);
										if(!file.exists()) {
											busy = true;
											user.getDout().writeUTF("DOWNLOAD " + GroupNumber + " " + fileName);
											user.getDout().flush();
										}
										else {
											busy = false;
											path = "NULL";
											JOptionPane.showMessageDialog(null, "Tập tin đã tồn tại!", "Thông báo!", JOptionPane.OK_OPTION);
										}
									}
								}
							}
							else {
								JOptionPane.showMessageDialog(null, "Đường truyền tệp đang bận!\nXin vui lòng thử lại sau.", "Thông báo", JOptionPane.OK_OPTION);
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				});
			}
			
			public JLabel get() {
				return label;
			}
			
			public void set(String labelName) {
				label.setText(labelName);
			}
		}
		
		public static class FileList {
			LinkedList<LabelFile> labelFiles;
			
			public FileList() {
				labelFiles = new LinkedList<LabelFile>();
			}
			
			public void add(String labelName) {
				labelFiles.add(new LabelFile(labelName));
			}
			
			public void removeAll() {
				while (labelFiles.size() > 0) {
					labelFiles.remove();
				}
			}
			
			public LabelFile getFirst() {
				return labelFiles.getFirst();
			}
			
			public LabelFile getLast() {
				return labelFiles.getLast();
			}
			
			public LabelFile get(int index) {
				return labelFiles.get(index);
			}
			
			public int size() {
				return labelFiles.size();
			}
		}
		
		// lay duong dan luu tep
		public static String getPath() {
			returnVal = chooser1.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				String s = getFileName(chooser1.getSelectedFile().getAbsolutePath());
				if(!s.contains(".")) {
					s = chooser1.getSelectedFile().getAbsolutePath();
				}
				else {
					s = "NULL";
				}
				chooser.setSelectedFile(null);
				return s;
			}
			return "NULL";
		}
		
		public void CreateFormGroupChat() {
			frame = new JFrame(user.getUserName());
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int height = screenSize.height/2;
			int width = screenSize.width/2;
			frame.setBounds(width - 250, height - 300, 500, 600);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.getContentPane().setLayout(null);
			
			textArea = new JTextArea();
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setBounds(10, 10, 250, 520);
			scrollPane.setWheelScrollingEnabled(true);
			frame.getContentPane().add(scrollPane);
			
			JTextField textField = new JTextField();
			textField.setBounds(10, 540, 250, 25);
			frame.getContentPane().add(textField);
			
			JButton btnSend = new JButton("Gửi");
			btnSend.setBounds(300, 540, 55, 23);
			frame.getContentPane().add(btnSend);
			
			btnSend.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						if(!textField.getText().equals("")) {
							textArea.append('\n' + user.getUserName() + ": " + textField.getText());
							user.getDout().writeUTF("MESSAGE " + GroupNumber + " " + user.getUserName() + " " + textField.getText());
							user.getDout().flush();
							textField.setText("");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			JLabel jLabel = new JLabel("Người dùng trong nhóm");
			jLabel.setBounds(300, 10, 140, 23);
			frame.getContentPane().add(jLabel);
			
			panel = new JPanel();
			panel.setLayout(new GridLayout(0, 1));
			
			int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED ;
		    int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED ;
		    
			JScrollPane scrollPane1 = new JScrollPane(panel, v, h);
			scrollPane1.setBounds(300, 35, 160, 200);
			scrollPane1.setWheelScrollingEnabled(true);
			scrollPane1.setBackground(Color.GREEN);
			scrollPane1.setViewportBorder(new LineBorder(Color.GREEN));
			scrollPane1.getViewport().setLayout(new FlowLayout(FlowLayout.LEADING));
			frame.getContentPane().add(scrollPane1);
			
			
			
			for(int i = 0; i < userNameList.size(); i++) {
				panel.add(userNameList.getJLabel(i));
			}
			
			JLabel jLabel2 = new JLabel("Danh sách tệp");
			jLabel2.setBounds(300, 245, 100, 23);
			frame.getContentPane().add(jLabel2);
			
			panel1 = new JPanel();
			panel1.setLayout(new GridLayout(0, 1));
			
			JScrollPane scrollPane2 = new JScrollPane(panel1, v, h);
			scrollPane2.setBounds(300, 270, 160, 200);
			scrollPane2.setWheelScrollingEnabled(true);
			scrollPane2.setBackground(Color.GREEN);
			scrollPane2.setViewportBorder(new LineBorder(Color.GREEN));
			scrollPane2.getViewport().setLayout(new FlowLayout(FlowLayout.LEADING));
			frame.getContentPane().add(scrollPane2);
			
			// chon file
			chooser = new JFileChooser();
			FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("TXT, CPP, JAVA", "txt", "cpp", "java");
			chooser.setFileFilter(extensionFilter);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);	
			
			// chon duong dan
			chooser1 = new JFileChooser();
			chooser1.setFileFilter(extensionFilter);
			
			chooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			// nut chon tep
			JButton btnChoosen = new JButton("Chọn tệp");
			btnChoosen.setBounds(375, 500, 85, 23);
			frame.getContentPane().add(btnChoosen);
			btnChoosen.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(!busy) {
						returnVal = chooser.showOpenDialog(null);
						if(returnVal == JFileChooser.APPROVE_OPTION) {
							if(!busy) {
								path = chooser.getSelectedFile().getPath();
							}
							chooser.setSelectedFile(null);
						}
					}
					else {
						JOptionPane.showMessageDialog(null, "Đường truyền tệp đang bận!\nXin vui lòng thử lại sau.", "Thông báo", JOptionPane.OK_OPTION);
					}
				}
			});
			
			// nut gui tep
			JButton btnSendFile = new JButton("Gửi tệp");
			btnSendFile.setBounds(375, 540, 75, 23);
			frame.getContentPane().add(btnSendFile);
			btnSendFile.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
						if(!busy) {
							if(!path.equals("NULL")) {
								try {
									fileName = getFileName(path);
									user.getDout().writeUTF("UPLOAD " + GroupNumber + " " + fileName);
									user.getDout().flush();
									busy = true;
								} catch (Exception e2) {
									e2.printStackTrace();
								}
							}
							else {
								JOptionPane.showMessageDialog(null, "Bạn chưa chọn tập tin!", "Thông báo!", JOptionPane.OK_OPTION);
							}
						}
						else {
							JOptionPane.showMessageDialog(null, "Đường truyền tệp đang bận!\nXin vui lòng thử lại sau.", "Thông báo", JOptionPane.OK_OPTION);
						}
						
						System.out.println(path);
				}
			});
			
			
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
        	    @Override
        	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        	    	String ms = "Bạn có chắc muốn đóng cửa sổ?";
        	    	String title = "Đóng cửa sổ?";
        	        if (JOptionPane.showConfirmDialog(frame, ms, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
        	        	try {
        	        		user.getDout().writeUTF("CLOSE " + GroupNumber);
        	        		user.getDout().flush();
        	        		
        	        		for(int i = 0; i < formHome.groupChats.size(); i++) {
            	        		if(formHome.groupChats.get(i).getGroupNumber() == GroupNumber) {
            	        			formHome.groupChats.remove(i);
            	        			break;
            	        		}
            	        	}
        	        		frame.dispose();
						} catch (Exception e) {
							e.printStackTrace();
						}
        	        }
        	    }
        	});
			
			frame.getRootPane().setDefaultButton(btnSend);
			frame.setResizable(false);
			frame.setVisible(true);
		}
		
		public void appendUser(String userName) {
			userNameList.add(userName);
			panel.add(userNameList.getLastJCheckBox());
			panel.validate();
			panel.revalidate();
			panel.repaint();
		}
		
		public void removeUser(String userName) {
			for(int i = 0; i < userNameList.size(); i++) {
				if(userNameList.getJLabel(i).getText().equals(userName)) {
					userNameList.remove(i);
					panel.remove(i);
					panel.validate();
					panel.revalidate();
					panel.repaint();
					break;
				}
			}
		}
		
		public void appendTextArea(String userName, String ms) {
			textArea.append('\n' + userName + ": " + ms);
		}
		
		public int getGroupNumber() {
			return GroupNumber;
		}
		
		public void setGroupNumber(int groupNumber) {
			GroupNumber = groupNumber;
		}
		
		public void appendFile(String fileName) {
			fileList.add(fileName);
			panel1.add(fileList.getLast().get());
			panel1.validate();
			panel1.revalidate();
			panel1.repaint();
		}
		
		public void dispose() {
			frame.dispose();
		}
		
		public UserNameList getUserList() {
			return userNameList;
		}
	}
	
	// class home
	public static class FormHome {
		JScrollPane scrollPane;
		UserNameList userNameList;
		volatile JPanel panel;
		LinkedList<FormGroupChat> groupChats;
		
		public FormHome() {
			userNameList = new UserNameList();
			groupChats = new LinkedList<FormGroupChat>();
		}
		
		public void CreateFormHome() {
			
			for (String userName : UserOnline) {
				userNameList.add(userName);
			}
			
			JFrame frame = new JFrame(user.getUserName());
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int height = screenSize.height/2;
			int width = screenSize.width/2;
			frame.setBounds(width - 150, height - 200, 300, 460);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.getContentPane().setLayout(null);
		
			panel = new JPanel();
			panel.setLayout(new GridLayout(0, 1));
			
			int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED ;
		    int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED ;
		    
			this.scrollPane = new JScrollPane(panel, v, h);
			this.scrollPane.setBounds(10, 10, 280, 350);
			this.scrollPane.setWheelScrollingEnabled(true);
			this.scrollPane.setBackground(Color.GREEN);
			this.scrollPane.setViewportBorder(new LineBorder(Color.GREEN));
			this.scrollPane.getViewport().setLayout(new FlowLayout(FlowLayout.LEADING));
			
			frame.getContentPane().add(this.scrollPane);
			
			for(int i = 0; i < userNameList.size(); i++) {
				panel.add(userNameList.getJCheckBox(i));
			}
			
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
        	    @Override
        	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        	    	String ms = "Bạn có chắc muốn đóng cửa sổ?";
        	    	String title = "Đóng cửa sổ?";
        	        if (JOptionPane.showConfirmDialog(frame, ms, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
        	        	try {
        	        		user.getDout().writeUTF("LOGOUT");
        	        		user.getDout().flush();
        	        		
        	        		while (formHome.getGroupChat().size() > 0) {
								formHome.getGroupChat().getFirst().dispose();
								formHome.getGroupChat().remove();
							}
        	        		
        	        		frame.dispose();
        	        		System.exit(0);
						} catch (Exception e) {
							e.printStackTrace();
						}
        	        }
        	    }
        	});
			
			JButton btnCreateGroup = new JButton("Tạo nhóm chat");
			btnCreateGroup.setBounds(90, 380, 120, 23);
			frame.getContentPane().add(btnCreateGroup);
			
			btnCreateGroup.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					Boolean flag = false;
	
					String message = "CHAT";
					for (JCheckBox checkBox : userNameList.getJCheckBox()) {
						if(checkBox.isSelected()) {
							message += " " + checkBox.getText();
							flag = true;
						}
					}
					
					try {
						if(flag) {
							user.getDout().writeUTF(message);
							user.getDout().flush();
						}
						else {
							JOptionPane.showMessageDialog(null, "Hãy chọn ít nhất một người dùng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					
					userNameList.setIsSelectAll(false);
				}
			});
			
			frame.setResizable(false);
			frame.setVisible(true);
		}
		
		public void appendUser(String userName) {
			userNameList.add(userName);
			panel.add(userNameList.getLastJCheckBox());
			panel.validate();
			panel.revalidate();
			panel.repaint();
		}
		
		public LinkedList<FormGroupChat> getGroupChat() {
			return groupChats;
		}
		
		public void removeUser(String userName) {
			int index = 0;
			for(int i = 0; i < userNameList.size(); i++) {
				if(userNameList.getJCheckBox(i).getText().equals(userName)) {
					break;
				}
				index++;
			}
			
			userNameList.remove(index);
			panel.remove(index);
			panel.revalidate();
			panel.repaint();
		}
		
		public void removeAll() {
			while (userNameList.getJCheckBox().size() > 0) {
				userNameList.remove(0);
				panel.remove(0);
				panel.revalidate();
				panel.repaint();
			}
		}
	}
	
	public static void tryToConnectToServer(Socket socket, int port) throws IOException {
		try {
			int timeOut = 3000;
			LinkedList<String> address = new LinkedList<String>();
			
			InputIPAddress inputIPAddress = new InputIPAddress(address);
			Thread thread = new Thread(inputIPAddress);
			thread.start();
			thread.join();
			
			SocketAddress socketAddress = new InetSocketAddress(address.getLast(), port);
				
			socket.connect(socketAddress, timeOut);
		} catch (Exception e) {
			e.printStackTrace();
			socket = new Socket();
			tryToConnectToServer(socket, port);
		}
	}
	
	//ham chinh
	public static void main(String[] args) {
		try {
			Port = 6666;		
			Client = new Socket();
			
			while (!Client.isConnected()) {
				Client = new Socket();
				tryToConnectToServer(Client, Port);
			}
			
			path = "NULL";
			busy = false;
			
			user = new User();
			user.setDin(Client.getInputStream());
			user.setDout(Client.getOutputStream());
			user.setIPAddress(Client.getLocalAddress().getHostAddress());
			
			UserOnline = new LinkedList<String>();
			
			signIn = new SignIn(user);
			
			//khoi tao va chay thread phan tich ms
			AnalysisMessage ThreadAnalysis = new AnalysisMessage();
			Thread threadAnalysis = new Thread(ThreadAnalysis);
			threadAnalysis.start();
			threadAnalysis.join();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
