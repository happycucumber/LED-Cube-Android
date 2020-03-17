package net.ddns.klop8093.ledcube;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class TcpClient extends Thread {
    String username = "pi";
    String host = "192.168.0.35";
    String password = "a";
    int port = 22;
    @Override
    public void run()  {


        // TODO Auto-generated method stub
        try {
            Socket socket = new Socket("192.168.0.35", 5001);

            // 입력 스트림
            // 서버에서 보낸 데이터를 받음
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            // 출력 스트림
            // 서버에 데이터를 송신
            OutputStream out = socket.getOutputStream();

            // 서버에 데이터 송신
            out.write("Hellow Java Tcp Client!!!! \n".getBytes());
            out.flush();
            System.out.println("데이터를 송신 하였습니다.");

            String line = in.readLine();
            System.out.println("서버로 부터의 응답 : " + line);

            // 서버 접속 끊기
            in.close();
            out.close();
            socket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
