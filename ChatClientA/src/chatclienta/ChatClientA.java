package chatclienta;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClientA
{
   JTextArea incoming;
   JTextField outgoing;
   JTextField uid;
   JPasswordField pwd;
   JLabel elbl = new JLabel(" ");


   BufferedReader reader;
   PrintWriter writer;
   Socket sock;
   String username;
   String password;
   boolean authed=false;
   
   //Moved set up networking to the constructor so logins can be checked
   public void go() {
      JFrame frame = new JFrame("Chat Client");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JLabel ulbl = new JLabel(uid.getText());
   
      JPanel mainPanel = new JPanel();
      incoming = new JTextArea(15, 50);
      incoming.setLineWrap(true);
      incoming.setWrapStyleWord(true);
      incoming.setEditable(false);
      JScrollPane qScroller = new JScrollPane(incoming);
      qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      outgoing = new JTextField(20);
      JButton sendButton = new JButton("Send");
      sendButton.addActionListener(new SendButtonListener());
      mainPanel.add(qScroller);
      mainPanel.add(outgoing);
      mainPanel.add(sendButton);
      frame.add(ulbl, BorderLayout.NORTH );
      frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
      
      Thread readerThread = new Thread(new IncomingReader());
      readerThread.start();
      
      frame.setSize(650, 500);
      frame.setVisible(true);
      
   }
   
   //Constructor to establish networking
   public ChatClientA() {
    setUpNetworking();
}
      
   public void auth() {
      JFrame lframe = new JFrame("Chat Client Login");
      // Specify an action for the close button.
      lframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      lframe.setLayout(new GridLayout(5, 2));
      
      // Create 10 panels.
      JPanel panel1 = new JPanel();
      JPanel panel2 = new JPanel();
      JPanel panel3 = new JPanel();
      JPanel panel4 = new JPanel();
      JPanel panel5 = new JPanel();
      JPanel panel6 = new JPanel();
      JPanel panel7 = new JPanel();
      JPanel panel8 = new JPanel();
      JPanel panel9 = new JPanel();
      JPanel panela = new JPanel();
      
      uid = new JTextField(20);
      pwd = new JPasswordField(20);
      //JLabel hlbl = new JLabel("Chat Application Login");
      JLabel ulbl = new JLabel("Username: ");
      JLabel plbl = new JLabel("Password: ");
      JButton loginButton = new JButton("Login");
      JButton registerButton = new JButton("Register");
      loginButton.addActionListener(new LoginButtonListener());
      registerButton.addActionListener(new RegisterButtonListener());
   
      //panel1.add(hlbl);
      panel2.add(elbl);
      panel3.add(ulbl);
      panel4.add(uid);
      panel5.add(plbl);
      panel6.add(pwd);
      panel8.add(loginButton);
      panela.add(registerButton);
      
      lframe.add(panel1);
      lframe.add(panel2); 
      lframe.add(panel3); 
      lframe.add(panel4); 
      lframe.add(panel5); 
      lframe.add(panel6); 
      lframe.add(panel7); 
      lframe.add(panel8);
      lframe.add(panel9); 
      lframe.add(panela);   
             
      lframe.setSize(500, 300);
      lframe.setVisible(true);
      while (!authed)
      {
         try{
            Thread.sleep(1000);
         }
         catch (Exception e)
         {
            System.out.println(e);
         }
      }
      lframe.setVisible(false);
      go();
   }

   
   private void setUpNetworking() {
      try {
         sock = new Socket("127.0.0.1", 5000);
         InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
         reader = new BufferedReader(streamReader);
         writer = new PrintWriter(sock.getOutputStream());
         System.out.println("networking established");
      }
      catch(IOException ex)
      {
         ex.printStackTrace();
      }
   }
   
   public class LoginButtonListener implements ActionListener 
   {
       public String response;
      public void actionPerformed(ActionEvent ev) 
      {
          response = null;
         username=uid.getText();
         password=pwd.getText();
           writer.println("LOG " + username + " " + password);
           writer.flush();
           
           try {
               //Wait for a server response to the login attempt
               response = reader.readLine();
               
               if(response.equals("accepted"))
               {
                   authed = true;
               }else
               {
                   elbl.setText(response);
               }
               
           } catch (IOException ex) {
               ex.printStackTrace();
           }                      
      }    
   }
   
   public class RegisterButtonListener implements ActionListener 
   {
       public String response;
      public void actionPerformed(ActionEvent ev) 
      {
          response = null;
         username=uid.getText();
         password=pwd.getText();
           writer.println("REG " + username + " " + password);
           writer.flush();
           
           try {
               //Wait for a server response to the login attempt
               response = reader.readLine();                                    
               elbl.setText(response);
               
           } catch (IOException ex) {
               ex.printStackTrace();
           }
           
           
      }
   }
   
   public class SendButtonListener implements ActionListener 
   {       
      public void actionPerformed(ActionEvent ev) 
      {          
         try 
         {
            writer.println(outgoing.getText());
            writer.flush();                    
         }
         catch (Exception ex) {
            ex.printStackTrace();
         }
         outgoing.setText("");
         outgoing.requestFocus();
      }
   }

   
   public static void main(String[] args) throws InterruptedException 
   {
      new ChatClientA().auth();
     // new ChatClientA().go();
   
   }
   
   class IncomingReader implements Runnable 
   {       
      public void run() {
         String message;
         try {
            while ((message = reader.readLine()) != null) {
               System.out.println("client read " + message);
               incoming.append(message + "\n");
            }
         } catch (IOException ex)
         {
            ex.printStackTrace();
         }
      }
   }
}

