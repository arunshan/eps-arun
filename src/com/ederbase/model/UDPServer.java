//UDPServer.java
package com.ederbase.model;

import java.net.*;

class UDPServer
{
  public void runServer(int iListen) throws Exception
  {
    byte[] receive_data = new byte[1024];
    byte[] send_data = new byte[1024];

    int recv_port;

    DatagramSocket server_socket = new DatagramSocket(iListen);

    System.out.println("UDPServer Waiting for client on port " + iListen);


    while (true)
    {

      DatagramPacket receive_packet = new DatagramPacket(receive_data,
          receive_data.length);

      server_socket.receive(receive_packet);

      String data = new String(receive_packet.getData(), 0, 0, receive_packet.getLength());

      InetAddress IPAddress = receive_packet.getAddress();

      recv_port = receive_packet.getPort();

      if (data.equals("q") || data.equals("Q"))
        break;
      else
        System.out.println("( " + IPAddress + " , " + recv_port + " ) said :" + data);


    }
  }
}

