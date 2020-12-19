

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class SignIn {
	
	User user;
	static String userName;
	static String pw;
	static JFrame frame;
	static SignUp signUp;
	static JLabel lblMessager;
	
	public SignIn(User user) {
		this.user = user;
		frame = new JFrame();
		frame.setResizable(false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = screenSize.height/2;
		int width = screenSize.width/2;
		frame.setBounds(width - 312/2, height - 287/2, 312, 287);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblLogin = new JLabel("Đăng nhập");
		lblLogin.setFont(new Font("Tahoma", Font.PLAIN, 26));
		lblLogin.setBounds(93, 11, 127, 40);
		frame.getContentPane().add(lblLogin);
		
		JLabel lblNewLabel = new JLabel("Tài khoản:");
		lblNewLabel.setBounds(25, 63, 77, 14);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Mật khẩu:");
		lblNewLabel_1.setBounds(25, 100, 71, 14);
		frame.getContentPane().add(lblNewLabel_1);
		
		JTextField textUser = new JTextField();
		textUser.setBounds(103, 63, 130, 20);
		textUser.setColumns(10);
		frame.getContentPane().add(textUser);
		
		
		JPasswordField txtPass = new JPasswordField();
		txtPass.setBounds(103, 100, 130, 20);
		frame.getContentPane().add(txtPass);
		
		lblMessager = new JLabel("");
		lblMessager.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMessager.setBounds(21, 215, 265, 26);
		frame.getContentPane().add(lblMessager);
		
		JButton btnLogin = new JButton("Đăng nhập");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textUser.getText().equalsIgnoreCase("") && !txtPass.getText().equals("") ) {
					userName = textUser.getText().toUpperCase();
					pw = txtPass.getText();
					lblMessager.setText("");
					String ms = "SIGNIN " + userName + " " + pw;
					try {
						if(userName.length() < 17 && pw.length() < 17) {
							if(checkUser(userName)) {
								user.getDout().writeUTF(ms);
								user.getDout().flush();
								System.out.println(ms);
							}
							else {
								lblMessager.setText("Tên tài khoản gồm các ký tự a-z @ _");
							}
						}
						else {
							lblMessager.setText("Độ dài TK và MK không quá 16 ký tự!");
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}else{
					lblMessager.setText("Đăng nhập thất bại!");
				}
			}
		});
		
		btnLogin.setBounds(62, 155, 95, 23);
	  	frame.getContentPane().add(btnLogin);
	  	
	  	JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textUser.setText("");
				txtPass.setText("");
				lblMessager.setText("");
			}
		}); 
		btnReset.setBounds(170, 155, 71, 23);
		frame.getContentPane().add(btnReset);
		
		JButton btnSingUp = new JButton("Đăng ký?");
		btnSingUp.setBounds(113, 185, 87, 23);
		frame.getContentPane().add(btnSingUp);
		 
		btnSingUp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				frame.setVisible(false);
				signUp =  new SignUp(frame, user);
			}
		});
		 
		// Set default button when press enter
		frame.getRootPane().setDefaultButton(btnLogin);
		frame.setVisible(true);
	}
	
	public void setUser() {
		user.setUserName(userName);
		user.setPassWord(pw.hashCode());
		frame.dispose();
	}
	
	public void setSignInFailed() {
		lblMessager.setText("Đăng nhập thất bại!");
	}
	
	public Boolean checkUser(String userName) {
		String checkString = "abcdefghijklmnopqrstuvxyzwABCDEFGHIJKLMNOPQRSTUVXYZW0123456789_@";
		for (int i = 0; i < userName.length(); i++) {
			if(!checkString.contains(userName.substring(i, i + 1))) {
				return false;
			}
		}
		return true;
	}
}
