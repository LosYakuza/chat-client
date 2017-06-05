package gui;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.ScrollPaneConstants;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class VentanaChat extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JButton btnEnviar;
	private JTextArea textArea;
	private String usuarioDestinoChat;
	private VentanaCliente parent;

	/**
	 * Create the frame.
	 * 
	 * Modo:
	 * 		"Sala":	Sala de Chat
	 * 		"Chat": Sesión privada
	 *  
	 * Usuario Destino: Usuario en sesión privada
	 */
	public VentanaChat(String usuarioDestinoChat, String modoChat, VentanaCliente parent) {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.parent = parent;
		this.usuarioDestinoChat = usuarioDestinoChat;
		
		if(modoChat.equals("Sala"))
			setTitle("Sala de Chat");
		else
			setTitle(usuarioDestinoChat);
				
		setResizable(false);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				mostrarVentanaConfirmacion();
			}
		});
		
		setBounds(100, 100, 450, 353);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(0, 0, 444, 300);
		contentPane.add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
		scrollPane.setViewportView(textArea);
		
		textField = new JTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					enviar();
					hacerFocoTextField(textField);
				}
			}
		});
		textField.setBounds(0, 300, 336, 27);
		contentPane.add(textField);
		textField.setColumns(10);
		
		btnEnviar = new JButton("Enviar");
		btnEnviar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				enviar();
			}
		});
		btnEnviar.setBounds(346, 302, 91, 23);
		contentPane.add(btnEnviar);

		setVisible(true);
		textField.requestFocus();

	}
	
	public void enviar() {
		this.parent.enviar(this.usuarioDestinoChat, textField.getText());
		textArea.setCaretPosition(textArea.getText().length());
		textArea.append(textField.getText() + "\n");
		textField.setText("");
	}
	
	public void recibido(String str){
		textArea.setCaretPosition(textArea.getText().length());
		textArea.append(str + "\n");
	}
	
	private void mostrarVentanaConfirmacion() {
		int res = JOptionPane.showConfirmDialog(this, "Desea salir de la sesión de chat", "Confirmar cerrar", JOptionPane.YES_NO_OPTION);
		if(res == JOptionPane.YES_OPTION)
			this.parent.closeChat(this.usuarioDestinoChat);
	}
	
	private void hacerFocoTextField(JTextField textField) {
		textField.requestFocus();
		textField.selectAll();
	}
	
	
}
