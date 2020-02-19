package chatservera;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
import java.sql.*;
import javax.xml.bind.DatatypeConverter;

public class ChatServerA
{
   ArrayList<ClientHandler> clients;
   static int sPort = 5000;
   Connection dbCon = null;
   
   public class ClientHandler implements Runnable 
   {
      BufferedReader reader;
      Socket sock;
      PrintWriter writer;
      String user = null;
              
      public ClientHandler(Socket clientSocket, PrintWriter writer) 
      {
         try {
            sock = clientSocket;
            InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(isReader);
            this.writer = writer;
            
         } catch (Exception ex) 
         { 
             clients.remove(this);
             ex.printStackTrace(); 
         }
      }
      
      public void run() {
         String message;
         try {
            while ((message = reader.readLine()) != null) 
            {
                if(user == null)
                {
                    String[] loginDat = message.split(" ");
                         
                    //Validate input before proceeding
                    if(loginDat.length != 3 || !loginDat[1].matches("^(\\w){1,16}$") || !loginDat[2].matches("^[\\w!@#$%^&*]{12,32}$"))
                    {
                        System.out.println("connection " + sock + " attempted to make an invalid login or registration");  
                        writer.println("The request could not be handled");
                        writer.flush();
                        continue;
                    }
                            
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(loginDat[2].getBytes());
                    String hash = DatatypeConverter.printHexBinary(md.digest());
                    loginDat[2] = hash;                   
                    
                    if(loginDat[0].equals("REG"))
                    {
                        Statement register = dbCon.createStatement();  
                        
                        String loginCheck = "SELECT PASS FROM USERS WHERE NAME == '" + loginDat[1].toLowerCase() + "'";
                        ResultSet rs = register.executeQuery(loginCheck);
                        
                        if(rs.next())
                        {
                            System.out.println("connection " + sock + " attemtped to register a username in use");
                            writer.println("that username is in use");
                            writer.flush();
                            rs.close();
                            register.close();
                            continue;
                        }
                        
                        String regString = "INSERT INTO USERS (NAME,PASS) VALUES ('" + loginDat[1].toLowerCase() + "', '" + loginDat[2] + "');";
                        System.out.println(regString);
                        register.execute(regString);
                        writer.println("account registered");
                        writer.flush();
                        register.close();
                        rs.close();                   
                    }else if(loginDat[0].equals("LOG"))
                    {                        
                        Statement login = dbCon.createStatement();
                        String loginCheck = "SELECT PASS FROM USERS WHERE NAME == '" + loginDat[1].toLowerCase() + "'";
                        ResultSet rs = login.executeQuery(loginCheck);
                        
                        if(rs.next())
                        {
                            if(rs.getString("PASS").equals(loginDat[2]))
                            {
                                user = loginDat[1];
                                System.out.println("connection " + sock + " logged in as " + user);
                                writer.println("accepted");
                                writer.flush();
                                login.close();
                                continue;
                            }else
                            {
                                System.out.println("connection " + sock + " attempted to log in with an incorrect password " + loginDat[1] + " " + loginDat[2]);
                            }
                        }else
                        {
                            System.out.println("connection " + sock + " attempted to log in with an unknown username " + loginDat[1] + " " + loginDat[2]);
                        }
                        
                        writer.println("incorrect username or password");
                        writer.flush();
                        login.close();
                        rs.close();
                    }
                }else
                {
                    tellEveryone(user + ": " + message);
                }               
            }
         } catch (Exception ex) 
         { 
             clients.remove(this);
             ex.printStackTrace(); 
         }
      }
      
      public void printToUsers(String message)
      {
          if(user == null)
              return;
          
          writer.println(message);
          writer.flush();
      }     
   }
   
   public static void main(String[] args) throws SQLException 
   {
      if (args.length>0){
         System.out.println("Server Port Selected: " + args[0]);
         sPort = Integer.parseInt(args[0]);
      } 
      new ChatServerA().go("accounts.db");
   }
   
   
   
   public void go(String dbName) throws SQLException 
   {
      clients = new ArrayList<>();
      try {
         ServerSocket serverSock = new ServerSocket(sPort);
         Class.forName("org.sqlite.JDBC");
         dbCon = DriverManager.getConnection("jdbc:sqlite:" + dbName);
         DatabaseMetaData md = dbCon.getMetaData();
         ResultSet rs = md.getTables(null, null, "%", null);
         
         if(!rs.next())
         {
             String tableString = "CREATE TABLE USERS (NAME TEXT NOT NULL, PASS TEXT NOT NULL)";
             Statement makeTable = dbCon.createStatement();
             makeTable.execute(tableString);
             makeTable.close();
             System.out.println("Table created");
         }
         
         rs.close();
                  
         while(true) 
         {
            Socket clientSocket = serverSock.accept();
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
            
            clients.add(new ClientHandler(clientSocket, writer));
            Thread t = new Thread(clients.get(clients.size() - 1));
            t.start();
            System.out.println("got a connection");
         }
      } catch (Exception ex) 
      { 
          ex.printStackTrace(); 
      }finally
      {
          dbCon.close();
      }
   }
   
   public void tellEveryone(String message) {
      Iterator it = clients.iterator();
      System.out.println(clients.size());
      while (it.hasNext()) 
      {
         try 
         {
             ClientHandler client = (ClientHandler) it.next();
             client.printToUsers(message);
         } catch (Exception ex) 
         { 
             ex.printStackTrace(); 
         }
      }
   }
}
