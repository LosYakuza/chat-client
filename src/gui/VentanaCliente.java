package gui;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.Connection;
import client.MessageHandler;
import server.Message;

import javax.swing.DefaultListModel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class VentanaCliente extends JFrame implements MessageHandler {

	private JPanel contentPane;
	private JList<String> listUsuarios;
	private JLabel lblUsuarios;
	private Connection cn;
	private HashMap<String, VentanaChat> chats;
	private String usr;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VentanaCliente frame = new VentanaCliente();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public VentanaCliente() {
		this.chats = new HashMap<>();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				abrirVentanaConfirmaSalir();
			}
		});
		
		try
		{
		   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		   e.printStackTrace();
		}
		
		setTitle("Chat");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 379, 526);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnArchivo = new JMenu("Archivo");
		menuBar.add(mnArchivo);
		
		JMenuItem mntmConectar = new JMenuItem("Conectar");
		mntmConectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				abrirVentanaConectar();
			}
		});
		mnArchivo.add(mntmConectar);
		
		JMenuItem mntmSalir = new JMenuItem("Salir");
		mntmSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				abrirVentanaConfirmaSalir();
			}
		});
		mnArchivo.add(mntmSalir);
		
		JMenu mnChat = new JMenu("Chat");
		menuBar.add(mnChat);
		JMenuItem mntmSalaDeChat = new JMenuItem("Sala de Chat");
		mntmSalaDeChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				abrirChat("all");
			}
		});
		mnChat.add(mntmSalaDeChat);
		
		JMenuItem mntmSesionPrivada = new JMenuItem("Sesión privada");
		mntmSesionPrivada.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				seleccionarElementoLista();
			}
		});
		mnChat.add(mntmSesionPrivada);
		
		JMenu mnAyuda = new JMenu("Ayuda");
		menuBar.add(mnAyuda);
		
		JMenuItem mntmConfigIpPuerto = new JMenuItem("Configurar IP-Puerto");
		mntmConfigIpPuerto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abrirVentanaConfiguracion();
			}
		});
		mnAyuda.add(mntmConfigIpPuerto);
		
		JMenuItem mntmAcerca = new JMenuItem("Acerca");
		mnAyuda.add(mntmAcerca);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(0, 0, 373, 462);
		contentPane.add(scrollPane);
		
		listUsuarios = new JList<String>();
		listUsuarios.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				seleccionaDobleClickChat(arg0);
			}
		});
		scrollPane.setViewportView(listUsuarios);

		lblUsuarios = new JLabel("Cantidad de Usuarios conectados: ");
		lblUsuarios.setBounds(0, 464, 373, 14);
		contentPane.add(lblUsuarios);

		setVisible(true);
	}
	
	private void abrirVentanaConfirmaSalir() {
		int opcion = JOptionPane.showConfirmDialog(this, "Desea salir del Chat", "Confirmación", JOptionPane.YES_NO_OPTION);
		if(opcion == JOptionPane.YES_OPTION)
			System.exit(0);
	}
	
	private void abrirVentanaConectar() {
		this.usr = "";
		if(cn != null){
			cn.stopRequest();
			try {
				cn.join(400);
			} catch (InterruptedException e) {
			}
			cn=null;
		}
		String usr = JOptionPane.showInputDialog(this, "Ingrese usuario:");
		if(usr==null)
			return;
		ArchivoDePropiedades adp = new ArchivoDePropiedades("config.properties");
		adp.lectura();
		try {
			cn = new Connection(adp.getIP(), adp.getPuerto(), usr,this);
			this.setTitle("Chat :: "+usr);
			cn.start();
			this.usr = usr;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al conectar, verificar configuracion", "Error", JOptionPane.ERROR_MESSAGE);
			new VentanaConfiguracion(this);
		}
	}
	
	
	public void agregaUsuariosEnLista(String str[]) {
		DefaultListModel<String> modeloLista = new DefaultListModel<String>();
		for(String item : str){
			if(!item.equals(this.usr))
				modeloLista.addElement(item);
		}
		Iterator<String> ventanas = this.chats.keySet().iterator();
		String item;
		while(ventanas.hasNext()){
			item = ventanas.next();
			if(!modeloLista.contains(item)){
				this.chats.get(item).recibido("## El usuario se ha desconectado.");
			}
		}

		listUsuarios.setModel(modeloLista);
		lblUsuarios.setText("Cantidad de Usuarios Conectados: " + modeloLista.getSize());
	}
	
	private void seleccionarElementoLista() {
		if(!listUsuarios.isSelectionEmpty()){
			abrirChat(listUsuarios.getSelectedValue());
		}else{
			JOptionPane.showMessageDialog(this, "Seleccione un elemento de la lista", "Seleccionar Usuario", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private void abrirVentanaConfiguracion() {
		new VentanaConfiguracion(this);
	}
	
	private void seleccionaDobleClickChat(MouseEvent me) {
		if(me.getClickCount() == 2){
			abrirChat(listUsuarios.getSelectedValue());
		}
	}

	private void abrirChat(String usr){
		if(this.usr == null || this.usr.equals("")){
			abrirVentanaConectar();
			return;
		}
		if(this.chats.containsKey(usr)){
			focus(usr);
		}else{
			if(usr.equals("all")){
				this.chats.put(usr, new VentanaChat(usr, "Sala",this));
			}else{
				this.chats.put(usr, new VentanaChat(usr, "Chat",this));
			}
		}
	}
	
	private void focus(String user){
		this.chats.get(user).requestFocus();
		this.chats.get(user).toFront();
		this.chats.get(user).repaint();
	}
	
	@Override
	public void messageReceived(Message m) {
		if(m.getSource().equals("server")){
			if(m.getType() == Message.STATUS_INFO){
				if(m.getDestination().equals("clientlist")){
					agregaUsuariosEnLista(m.getText().split(","));
				}
				return;
			}
			if(m.getType() == Message.SERVER_FATAL){
				JOptionPane.showMessageDialog(this, m.getText(), "Error", JOptionPane.ERROR_MESSAGE);
				if(m.getDestination().equals("login")){
					this.usr="";
					this.setTitle("Chat");
				}
				return;
			}
		}
		
		if(m.getDestination().equals("all")){
			if(this.chats.containsKey("all")){
				if(m.getType() == Message.USR_MSJ && !m.getSource().equals(this.usr)){
					this.chats.get("all").recibido(m.getSource()+ ": "+m.getText());
				}
			}
		}else{
			abrirChat(m.getSource());
			if(m.getType() == Message.USR_MSJ){
				this.chats.get(m.getSource()).recibido(m.getSource()+ ": "+m.getText());
			}
			if(m.getType() == Message.STATUS_INFO){
				this.chats.get(m.getSource()).recibido("## Info: "+m.getText());
			}
		}
	}
	
	public void enviar(String usr, String text){
		try {
			this.cn.sendChat(usr, text);
		} catch (IOException e) {
			e.printStackTrace();
			abrirChat(usr);
			this.chats.get(usr).recibido(" :: Error al enviar mensaje ::");
		}
	}
	
	public void closeChat(String user){
		this.chats.get(user).dispose();
		this.chats.remove(user);
	}
	
}
