

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class SignUp {
	User user;
	static String userName;
	static String pw;
	static JFrame frame;
	static JFrame jFrame;
	static JLabel lblMessager;
	
	public SignUp(JFrame jFrame, User user) {
		this.user = user;
		frame = new JFrame();
		this.jFrame = jFrame;
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = screenSize.height/2;
		int width = screenSize.width/2;
		frame.setBounds(width - 312/2, height - 287/2, 312, 287);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblSignUp = new JLabel("Đăng ký");
		lblSignUp.setFont(new Font("Tahoma", Font.PLAIN, 26));
		lblSignUp.setBounds(93, 11, 127, 40);
		frame.getContentPane().add(lblSignUp);

		JLabel lblNewLabel = new JLabel("Tài khoản:");
		lblNewLabel.setBounds(25, 63, 77, 14);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Mật khẩu:");
		lblNewLabel_1.setBounds(25, 100, 71, 14);
		frame.getContentPane().add(lblNewLabel_1);
		
		JTextField textUser = new JTextField();
		textUser.setBounds(103, 63, 130, 20);
		frame.getContentPane().add(textUser);
		textUser.setColumns(10);
		 
		JPasswordField txtPass = new JPasswordField();
		txtPass.setBounds(103, 100, 130, 20);
		frame.getContentPane().add(txtPass);
		 
		JLabel lblRePW = new JLabel("Nhập lại:");
		lblRePW.setBounds(25, 137, 71, 14);
		frame.getContentPane().add(lblRePW);
		 
		JPasswordField reTxtPass = new JPasswordField();
		reTxtPass.setBounds(103, 137, 130, 20);
		frame.getContentPane().add(reTxtPass);
		 
		JCheckBox cbmi = new JCheckBox("Visible");
		cbmi.setBounds(240, 102, 71, 14);
		frame.getContentPane().add(cbmi);
		 
		JTextField text_1 = new JTextField();
		text_1.setBounds(103, 100, 130, 20);
		text_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		frame.getContentPane().add(text_1);
		
		JTextField text_2 = new JTextField();
		text_2.setBounds(103, 137, 130, 20);
		text_2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		frame.getContentPane().add(text_2);
		 
		cbmi.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if(cbmi.isSelected()) {
					text_1.setText(txtPass.getText());
					text_2.setText(reTxtPass.getText());
					text_1.show();
					txtPass.show(false);
					text_2.show();
					reTxtPass.show(false);
				}
				else {
					txtPass.setText(text_1.getText());
					reTxtPass.setText(text_2.getText());
					text_1.show(false);
					txtPass.show();
					text_2.show(false);
					reTxtPass.show();
				}
			}
		});
		
		lblMessager = new JLabel("");
		lblMessager.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMessager.setBounds(21, 230, 265, 26);
		frame.getContentPane().add(lblMessager);

		JButton btnSignUp = new JButton("Đăng ký");
		btnSignUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textUser.getText().equalsIgnoreCase("") && !txtPass.getText().equals("") && !reTxtPass.getText().equals("")) {
					if(textUser.getText().length() < 17 && txtPass.getText().length() < 17) {
						if(checkUser(textUser.getText())) {
							if(txtPass.getText().equals(reTxtPass.getText())) {
								userName = textUser.getText().toUpperCase();
								pw = txtPass.getText();
								String ms = "SIGNUP " + userName + " " + pw;
								try {
									user.getDout().writeUTF(ms);
									user.getDout().flush();
									System.out.println(ms);
								} catch (Exception e2) {
									e2.printStackTrace();
								}
							}
							else {
								lblMessager.setText("Mật khẩu không trùng nhau!");
							}
						}
						else {
							lblMessager.setText("Tên tài khoản gồm các ký tự a-z @ _");
						}
					}
					else {
						lblMessager.setText("Độ dài TK và MK không quá 16 ký tự!");
					}
				}else{
					lblMessager.setText("Tài khoản và mật khẩu không được để trống!");
				}
			}
		});
		 
		btnSignUp.setBounds(62, 175, 95, 23);
	  	frame.getContentPane().add(btnSignUp);

		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textUser.setText("");
				txtPass.setText("");
				reTxtPass.setText("");
				lblMessager.setText("");
				text_1.setText("");
				text_2.setText("");
			}
		});
		 
		btnReset.setBounds(170, 175, 71, 23);
		frame.getContentPane().add(btnReset);
		
		JButton btnLogin = new JButton("Đăng nhập");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				jFrame.setVisible(true);
			}
		});
		
		btnLogin.setBounds(113, 210, 95, 23);
	  	frame.getContentPane().add(btnLogin);

		// Set default button when press enter
		frame.getRootPane().setDefaultButton(btnSignUp);
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	public void setSignUp() {
		jFrame.setVisible(true);
		frame.setVisible(false);
		frame.disable();
		JOptionPane.showMessageDialog(null, "Đăng ký thành công!");
	}
	
	public void setSignUpFailed() {
		lblMessager.setText("Tài khoản đã tồn tại!");
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
