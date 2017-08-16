package com.example.vinic.appbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.text.CollationElementIterator;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int SOLICITA_ATIVACAO =1;
    private static final int SOLICITA_CONEXAO =2;

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice mDevice=null;
    BluetoothSocket mSocket=null;

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
                        Toast.makeText(getApplicationContext(),"Você foi connectado com Mac: "+ MAC, Toast.LENGTH_LONG).show();
                        conexao=true;
                        final Button btnConectar = (Button)findViewById(R.id.idConectar);
                        btnConectar.setText("Desconectar");


                    }catch (IOException e){
                        conexao=false;
                        Toast.makeText(getApplicationContext(),"Ocorreu um erro", Toast.LENGTH_LONG).show();
                    }


                }else{
                    Toast.makeText(getApplicationContext(),"Falha ao obter o MAC", Toast.LENGTH_LONG).show();
                }
        }

    }
}
