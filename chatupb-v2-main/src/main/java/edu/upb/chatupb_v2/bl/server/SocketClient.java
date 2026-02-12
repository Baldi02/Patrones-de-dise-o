/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.message.Invitacion;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author rlaredo
 */
public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public SocketClient(String ip) throws IOException {
        this.socket = new Socket(ip, 1900);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

        //Aqui hice el cambio inge
        String idAuto = UUID.randomUUID().toString().substring(0, 8);
        //Valor unico de id
        String nombreAuto = System.getProperty("user.name");
        //Que me dé el nombre
        Invitacion inv = new Invitacion(idAuto, nombreAuto);
        //Objeto invitacion
        String trama = inv.generarTrama().replace(System.lineSeparator(), "");
        //Hace la trama del objeto invitacion y saca el separador de lineas porque eso lo hace send
        send(trama);
        //La envía
    }


    @Override
    public void run() {
        try {
            String message;
            //Escucha infinitamente
            while ((message = br.readLine()) != null) {
            //debugging
                System.out.println("[" + ip + "] << " + message);

                String[] split = message.split(Pattern.quote("|"));
                //separar el mensaje en partes split[]
                if (split.length == 0) {
                    //Pa ver que no esté vacio
                    continue;
                }

                switch (split[0]) {
                    case "001": {
                        Invitacion inv = Invitacion.parse(message);
                        //convierte a objeto invitacion

                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                null,
                                "Invitación de conexión\n\nNombre: " + inv.getNombre()
                                        + "\nID: " + inv.getIdUsuario()
                                        + "\nIP: " + ip,
                                "Invitación",
                                JOptionPane.INFORMATION_MESSAGE
                        ));
                        //Que nos muestre la informacion de quien se conecta con el popup
                        break;
                    }

                    case "002": {

                        break;
                    }

                    default: {

                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void send(String message) throws IOException {
        message = message + System.lineSeparator();
        try {
            dout.write(message.getBytes("UTF-8"));
            dout.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.socket.close();
            this.br.close();
            this.dout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
