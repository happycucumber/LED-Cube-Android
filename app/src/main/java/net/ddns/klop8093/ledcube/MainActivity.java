package net.ddns.klop8093.ledcube;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.SocketException;
import java.util.Arrays;


public class MainActivity extends Activity {

    LinearLayout main, sub, button,layer[], line[], pixel[];
    HorizontalScrollView horizontalScrollView;
    Button set,set2,reset;
    boolean bin[] = new boolean[512];
    byte lineBin[] = new byte[64];
    int on = Color.rgb(00, 00, 255);
    int off = Color.rgb(122, 123, 129);
    int pow[] = {1, 2, 4, 8, 16, 32, 64, 128};
    //int pixelSize = 80; //태블릿
    //int pixelMargin = 7; //태블릿
    //int btnSize = 640;
    int pixelSize = 145; //갤7
    int pixelMargin = 15; //갤7
    int btnSize = 480;
    static String FtpAddress = "xxx.xxx.xxx.xxx"; //rpi ip
    String dirPath;
    File file;
    File savefile,binFile,realBinFile;
    TcpClient b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        horizontalScrollView = new HorizontalScrollView(this);
        //scrollView = new ScrollView(this);
        main = new LinearLayout(this);
        sub = new LinearLayout(this);
        button = new LinearLayout(this);
        layer = new LinearLayout[8];
        line = new LinearLayout[64];
        pixel = new LinearLayout[512];
        set = new Button(this);
        set2 = new Button(this);
        reset = new Button(this);
        set.setId(512);
        set2.setId(513);
        reset.setId(514);

        main.setOrientation(LinearLayout.VERTICAL);
        sub.setOrientation(LinearLayout.HORIZONTAL);
        button.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < layer.length; i++) {
            layer[i] = new LinearLayout(this);
            layer[i].setOrientation(LinearLayout.VERTICAL);
        }
        for (int i = 0; i < line.length; i++) {
            line[i] = new LinearLayout(this);
            line[i].setOrientation(LinearLayout.HORIZONTAL);
        }
        for (int i = 0; i < pixel.length; i++) {
            pixel[i] = new LinearLayout(this);
            pixel[i].setId(i);
            //pixel[i].setOrientation(LinearLayout.VERTICAL);
        }


        for (int i = 0; i < line.length; i++)
            for (int j = i * 8; j < (i + 1) * 8; j++) {
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(pixelSize, pixelSize);
                param.setMargins(pixelMargin, pixelMargin, pixelMargin, pixelMargin);

                pixel[j].setLayoutParams(param);
                //pixel[j].setLayoutParams(new LinearLayout.LayoutParams(100,100));
                pixel[j].setBackgroundColor(off);
                line[i].addView(pixel[j]);
            }

        for (int i = 0; i < layer.length; i++)
            for (int j = i * 8; j < (i + 1) * 8; j++) {
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                param.setMargins(25, 25, 25, 25);
                layer[i].setLayoutParams(param);
                layer[i].addView(line[j]);
            }

        for (int i = 0; i < layer.length; i++) {
            sub.addView(layer[i]);
        }
        horizontalScrollView.addView(sub);
        main.addView(horizontalScrollView);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(btnSize,LinearLayout.LayoutParams.FILL_PARENT);
        set.setLayoutParams(param2);
        set2.setLayoutParams(param2);
        reset.setLayoutParams(param2);
        button.setLayoutParams(param);
        button.addView(set);
        button.addView(set2);
        button.addView(reset);
        main.addView(button);
        setContentView(main);

        Arrays.fill(bin, false);

        for (int i = 0; i < pixel.length; i++) {
            pixel[i].setOnClickListener(ledClickListener);
        }
        set.setText("레이어 모두 적용");
        set.setOnClickListener(buttonClickListener);
        set2.setText("레이어 복제");
        set2.setOnClickListener(buttonClickListener);
        reset.setText("리셋");
        reset.setOnClickListener(buttonClickListener);

        //checkPermission();

        dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LEDCUBE";
        file = new File(dirPath);
        savefile = new File(dirPath + "/LedData.txt");
        binFile = new File(dirPath + "/binData.txt");
        realBinFile = new File(dirPath + "/b");

        if(binFile.exists()) {
            String TempBin[] =new String[512];
            try {
                BufferedReader in = new BufferedReader(new FileReader(dirPath + "/binData.txt"));
                TempBin = in.readLine().split(",");
                for(int i =0;i<bin.length;i++) {
                    bin[i] = getBool(TempBin[i]);
                    if(bin[i]==true)
                        pixel[i].setBackgroundColor(on);
                    else
                        pixel[i].setBackgroundColor(off);
                }
            }catch (Exception e){}
        }
    }

    LinearLayout.OnClickListener ledClickListener = new View.OnClickListener() {
        public void onClick(View v) {
                Log.i("ID", "" + v.getId());
                bin[v.getId()] = !bin[v.getId()];
                if (bin[v.getId()])
                    findViewById(v.getId()).setBackgroundColor(on);
                else
                    findViewById(v.getId()).setBackgroundColor(off);

        }
    };
    Button.OnClickListener buttonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            int dec[][] = new int[8][8]; //한층에 8줄의값,고것이 8층있음 앞에8이 층이고 뒤에8이 한증에 8줄들
            Log.i("BTNid", ""+v.getId());
            switch (v.getId()){
                case 512:
                    for (int i = 0; i < 64; i++) {
                        int level = i % 8;


                        for (int j = 0; j < 8; j++) {
                            int row = (i * 8 + j) / 64;
                            dec[7 - level][row] += getInt(bin[i * 8 + j]) * pow[j];
                        }

                    }
                    break;
                case 513:
                    for (int i = 0; i < 8; i++) {


                        for (int j = 0; j < 8; j++) {
                            int row = (+ j) ;
                            dec[7 - i][0] += getInt(bin[i * 8+j]) * pow[j];
                            dec[7 - i][1] += getInt(bin[i * 8+j]) * pow[j];
                            dec[7 - i][2] += getInt(bin[i * 8+j]) * pow[j];
                            dec[7 - i][3] += getInt(bin[i * 8+j]) * pow[j];
                            dec[7 - i][4] += getInt(bin[i * 8+j]) * pow[j];
                            dec[7 - i][5] += getInt(bin[i * 8+j]) * pow[j];
                            dec[7 - i][6] += getInt(bin[i * 8+j]) * pow[j];
                            dec[7 - i][7] += getInt(bin[i * 8+j]) * pow[j];


                        }

                    }
                    break;
                case 514:
                    Arrays.fill(bin,false);
                    for(int i =0;i<pixel.length;i++)
                        pixel[i].setBackgroundColor(off);
                    break;
            }

            makeTempFile(dec);
            //makeBin(bin);
            Log.i("make", "ok");
            Upload a = new Upload();
            a.execute();
            SystemClock.sleep(1000);
            TcpClient b = new TcpClient();
            b.start();
        }
    };

    int getInt(boolean a) {
        if (a == true)
            return 1;
        else
            return 0;
    }
    int getByte(boolean a) {
        if (a == true)
            return 1;
        else
            return 0;
    }
    boolean getBool(String a) {
        if (a.equals("1"))
            return true;
        else
            return false;
    }

    void makeTempFile(int data[][]) { //dec[층][줄]

        Log.i("path", dirPath);
// 일치하는 폴더가 없으면 생성
        if (!file.exists()) {
            file.mkdirs();
            //Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }


// txt 파일 생성

        try {
            if (savefile.exists())
                savefile.delete();
            if (binFile.exists())
                binFile.delete();
            if(realBinFile.exists())
                realBinFile.delete();
            //savefile
            FileOutputStream fos = new FileOutputStream(savefile);
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    String str = "" + data[i][j];
                    if (j == data[i].length - 1) {
                        fos.write(str.getBytes());
                        fos.write("\n".getBytes());
                    } else {
                        fos.write(str.getBytes());
                        fos.write(",".getBytes());
                    }
                }
            }
            fos.close();
            //binFile
            fos = new FileOutputStream(binFile);
            for (int i = 0; i < bin.length; i++) {
                fos.write((""+getInt(bin[i])).getBytes());
                if(i!=bin.length-1)
                    fos.write(",".getBytes());
            }
            fos.close();
            //realBinFile
            fos = new FileOutputStream(realBinFile);
            DataOutputStream out = new DataOutputStream(fos);
            for (int i = data.length; i > 0; i--) {
                String tempLog = new String("");
                for (int j = 0; j < data[0].length; j++) {

                    tempLog = tempLog + data[i][j] + ",";
                    out.write(data[i][j]);
                }
                Log.i("data",tempLog);
                tempLog = "";
            }
            out.close();
            fos.close();
            //Toast.makeText(this, "Save Success", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
        }

    }

    public void makeBin(boolean binData[]){
        Arrays.fill(lineBin,(byte)0);
        String binIndex = new String();
        for(int i = 0; i < binData.length; i++) {
            byte tempsa =(byte) (binData[i]?1<< 7-i%8:0) ;
                lineBin[i/8] += tempsa;
                binIndex += binData[i]?1:0;
            }
            Log.i("bin",binIndex);
    }


/*
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);

            // MY_PERMISSION_REQUEST_STORAGE is an
            // app-defined int constant

        } else {
            // 다음 부분은 항상 허용일 경우에 해당이 됩니다.
        }
    }
    */

    public class Upload extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                FTPClient mFTP = new FTPClient();

                mFTP.connect(FtpAddress, 21);  // ftp로 접속
                mFTP.login("pi", "a"); // ftp 로그인 계정/비번
                mFTP.setFileType(FTP.BINARY_FILE_TYPE); // 바이너리 파일
                mFTP.setBufferSize(1024 * 1024); // 버퍼 사이즈
                mFTP.enterLocalPassiveMode(); // 패시브 모드로 접속


                // 업로드 경로 수정 (선택 사항 )

                mFTP.cwd("LedCube/pattern/temp"); // ftp 상의 업로드 디렉토리
                //mFTP.mkd("files"); // public아래로 files 디렉토리를 만든다
                //mFTP.cwd("files"); // public/files 로 이동 (이 디렉토리로 업로드가 진행)

                if (savefile.isFile()) {
                    FileInputStream ifile = new FileInputStream(savefile);

                    //mFTP.rest(savefile.getName());  // ftp에 해당 파일이있다면 이어쓰기
                    mFTP.deleteFile("LedData.txt");   // 이전 파일 제거
                    mFTP.appendFile(savefile.getName(), ifile); // ftp 해당 파일이 없다면 새로쓰기
                }

                mFTP.disconnect(); // ftp disconnect

            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

}
