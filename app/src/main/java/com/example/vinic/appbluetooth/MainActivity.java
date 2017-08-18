package com.example.vinic.appbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompatApi23;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.CollationElementIterator;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int SOLICITA_ATIVACAO =1;
    private static final int SOLICITA_CONEXAO =2;
    private static final int MESSAGE_READ=3;

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice mDevice=null;
    BluetoothSocket mSocket=null;

    Handler mHandler;
    StringBuilder dadosBluetooth = new StringBuilder();

    ConnectedThread connectedThread;

    UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    boolean conexao = false;

    private static String MAC=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final Button btnConectar = (Button)findViewById(R.id.idConectar);
        Button btnLed1 = (Button)findViewById(R.id.btn1);
        Button btnLed2 = (Button)findViewById(R.id.btn2);
        Button btnLed3 = (Button)findViewById(R.id.btn3);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter==null){
            Toast.makeText(getApplicationContext(),"Bluetooth não disponivel", Toast.LENGTH_LONG).show();

        }else if(!mBluetoothAdapter.isEnabled()){
            Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBluetooth, SOLICITA_ATIVACAO);
        }

        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conexao){
                    try{
                        mSocket.close();
                        conexao=false;
                        btnConectar.setText("Conectar");
                        Toast.makeText(getApplicationContext(),"Bluetooth foi desconectado ", Toast.LENGTH_LONG).show();
                    }catch (IOException e){
                        Toast.makeText(getApplicationContext(),"Erro: " + e, Toast.LENGTH_LONG).show();
                    }
                }else{
                    Intent abreLista= new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista,SOLICITA_CONEXAO);
                }

            }
        });

        btnLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conexao){
                    connectedThread.enviar("Led1");
                }else{
                    Toast.makeText(getApplication(),"Bluetooth não ativado", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnLed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conexao){
                    connectedThread.enviar("Led2");
                }else{
                    Toast.makeText(getApplication(),"Bluetooth não ativado", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnLed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conexao){
                    connectedThread.enviar("Led3");
                }else{
                    Toast.makeText(getApplication(),"Bluetooth não ativado", Toast.LENGTH_LONG).show();
                }
            }
        });

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                if(msg.what == MESSAGE_READ){
                    //Toast.makeText(getApplicationContext(),msg.toString(),Toast.LENGTH_LONG).show();
                    String recebidos = (String) msg.obj;

                    dadosBluetooth.append(recebidos);

                    int fimInformacao = dadosBluetooth.indexOf("}");

                    if(fimInformacao>0){

                        String dadosCompletos = dadosBluetooth.substring(0, fimInformacao);

                        int tamanhoInformacao = dadosCompletos.length();

                        if(dadosBluetooth.charAt(0)== '{'){

                            String dadosFinais= dadosBluetooth.substring(1, tamanhoInformacao);
                            Log.d("TAG", dadosFinais);
                            Toast.makeText(getApplicationContext(),"Dados recebidos: "+dadosFinais, Toast.LENGTH_LONG).show();

                        }
                        dadosBluetooth.delete(0,dadosBluetooth.length());
                        dadosCompletos=" ";
                    }
                }

            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch ( requestCode){
            case SOLICITA_ATIVACAO:
                if(resultCode == Activity.RESULT_OK){
                    Toast.makeText(getApplicationContext(),"Bluetooth foi ativado", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Bluetooth não foi ativado", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case SOLICITA_CONEXAO:
                if(resultCode == Activity.RESULT_OK){
                    MAC= data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);
                   // Toast.makeText(getApplicationContext(),"MAC FINAL: "+MAC, Toast.LENGTH_LONG).show();

                    mDevice = mBluetoothAdapter.getRemoteDevice(MAC);

                    try{

                        mSocket = mDevice.createRfcommSocketToServiceRecord(mUUID);
                        mSocket.connect();

                        connectedThread = new ConnectedThread(mSocket);
                        connectedThread.start();

                        Toast.makeText(getApplicationContext(),"Você foi connectado com Mac: "+ MAC, Toast.LENGTH_LONG).show();
                        conexao=true;
                        final Button btnConectar = (Button)findViewById(R.id.idConectar);
                        btnConectar.setText("Desconectar");


                    }catch (Exception e){
                        conexao=false;
                        Toast.makeText(getApplicationContext(),"Ocorreu um erro", Toast.LENGTH_LONG).show();
                    }


                }else{
                    Toast.makeText(getApplicationContext(),"Falha ao obter o MAC", Toast.LENGTH_LONG).show();
                }
        }

    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[30720];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    String dadosBt= new String(buffer, 0, bytes);

                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, dadosBt)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void enviar(String dadosEnviar) {
            byte[] msgBuffer = dadosEnviar.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */

    }
}
