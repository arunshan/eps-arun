//UDPClient.java
//http://www.prasannatech.net/2008/07/socket-programming-tutorial.html
package com.ederbase.model;

import java.net.*;

class UDPClient
{
  public void sendUdp(String stSend, int iPort) throws Exception
  {
    byte[] send_data = new byte[1024];

    DatagramSocket client_socket = new DatagramSocket();

    InetAddress IPAddress = InetAddress.getByName("127.0.0.1");

    send_data = stSend.getBytes();

    DatagramPacket send_packet = new DatagramPacket(send_data,
        send_data.length,
        IPAddress, iPort);

    client_socket.send(send_packet);

    client_socket.close();
  }
}

