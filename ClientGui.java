import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.filechooser.FileFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientGui extends Thread{

  final JTextPane jtextFilDiscu = new JTextPane();
  final JTextPane jtextListUsers = new JTextPane();
  final JTextField jtextInputChat = new JTextField();
  private String oldMsg = "";
  private Thread read;
  private String serverName;
  private int PORT;
  private String name;
  BufferedReader input;
  PrintWriter output;
  Socket server;
  
  private JButton jsbtnSendFile;
  private JFileChooser fileChooser;

  public ClientGui() {
    this.serverName = "localhost";
    this.PORT = 12345;
    this.name = "nickname";

    String fontfamily = "Arial, sans-serif";
    Font font = new Font(fontfamily, Font.PLAIN, 15);

    final JFrame jfr = new JFrame("Chat");
    jfr.getContentPane().setLayout(null);
    jfr.setSize(700, 500);
    jfr.setResizable(true);
    jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Modul rangkaian diskusi
    jtextFilDiscu.setBounds(25, 25, 490, 320);
    jtextFilDiscu.setFont(font);
    jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
    jtextFilDiscu.setEditable(false);
    JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
    jtextFilDiscuSP.setBounds(25, 25, 490, 320);

    jtextFilDiscu.setContentType("text/html");
    jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Modul Daftar Pengguna
    jtextListUsers.setBounds(520, 25, 156, 320);
    jtextListUsers.setEditable(true);
    jtextListUsers.setFont(font);
    jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
    jtextListUsers.setEditable(false);
    JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
    jsplistuser.setBounds(520, 25, 156, 320);

    jtextListUsers.setContentType("text/html");
    jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Field message user input
    jtextInputChat.setBounds(0, 350, 400, 50);
    jtextInputChat.setFont(font);
    jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
    final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
    jtextInputChatSP.setBounds(25, 350, 650, 50);

    // button send
    final JButton jsbtn = new JButton("Send");
    jsbtn.setFont(font);
    jsbtn.setBounds(575, 410, 100, 35);

    // button Disconnect
    final JButton jsbtndeco = new JButton("Disconnect");
    jsbtndeco.setFont(font);
    jsbtndeco.setBounds(25, 410, 130, 35);
    
    // button Send File
    jsbtnSendFile = new JButton("Send File");
    jsbtnSendFile.setFont(font);
    jsbtnSendFile.setBounds(440, 410, 130, 35);

    jsbtnSendFile.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendFile();
        }
    });

    jtextInputChat.addKeyListener(new KeyAdapter() {
      // send message on Enter
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        // Get last message typed
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }
      }
    });

    // Click on send button
    jsbtn.addActionListener(ae -> sendMessage());

    // Connection view
    final JTextField jtfName = new JTextField(this.name);
    final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
    final JTextField jtfAddr = new JTextField(this.serverName);
    final JButton jcbtn = new JButton("Connect");

    // periksa apakah field itu tidak kosong
    jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));

    // posisi module
    jcbtn.setFont(font);
    jtfAddr.setBounds(25, 380, 135, 40);
    jtfName.setBounds(375, 380, 135, 40);
    jtfport.setBounds(200, 380, 135, 40);
    jcbtn.setBounds(575, 380, 100, 40);

    // warna default thread diskusi dan modul daftar pengguna
    jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
    jtextListUsers.setBackground(Color.LIGHT_GRAY);

    // menambahkan elemen
    jfr.add(jcbtn);
    jfr.add(jtextFilDiscuSP);
    jfr.add(jsplistuser);
    jfr.add(jtfName);
    jfr.add(jtfport);
    jfr.add(jtfAddr);
    // Initialize the file chooser
    fileChooser = new JFileChooser();
    jfr.setVisible(true);


    // info di Obrolan
    appendToPane(jtextFilDiscu, "<h4>Perintah yang memungkinkan dalam chat adalah:</h4>"
        +"<ul>"
        +"<li><b>@nickname</b> untuk mengirim Pesan Pribadi ke 'nama panggilan' pengguna</li>"
        +"<li><b>#d3961b</b> untuk mengubah warna nama panggilan Anda menjadi kode heksadesimal yang ditunjukkan</li>"
        +"<li><b>;)</b> beberapa smiley diterapkan</li>"
        +"<li><b>panah atas</b> untuk melanjutkan pesan yang terakhir diketik</li>"
        +"</ul><br/>");

    // On connect
    jcbtn.addActionListener(ae -> {
      try {
        name = jtfName.getText();
        String port = jtfport.getText();
        serverName = jtfAddr.getText();
        PORT = Integer.parseInt(port);

        appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
        server = new Socket(serverName, PORT);

        appendToPane(jtextFilDiscu, "<span>Connected to " +
            server.getRemoteSocketAddress()+"</span>");

        input = new BufferedReader(new InputStreamReader(server.getInputStream()));
        output = new PrintWriter(server.getOutputStream(), true);

        // send nickname to server
        output.println(name);

        // create new Read Thread
        read = new Read();
        read.start();
        jfr.remove(jtfName);
        jfr.remove(jtfport);
        jfr.remove(jtfAddr);
        jfr.remove(jcbtn);
        jfr.add(jsbtnSendFile);
        jfr.add(jsbtn);
        jfr.add(jtextInputChatSP);
        jfr.add(jsbtndeco);
        jfr.revalidate();
        jfr.repaint();
        jtextFilDiscu.setBackground(Color.WHITE);
        jtextListUsers.setBackground(Color.WHITE);
      } catch (Exception ex) {
        appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
        JOptionPane.showMessageDialog(jfr, ex.getMessage());
      }
    });

    // on deco
    jsbtndeco.addActionListener(ae -> {
      jfr.add(jtfName);
      jfr.add(jtfport);
      jfr.add(jtfAddr);
      jfr.add(jcbtn);
      jfr.remove(jsbtn);
