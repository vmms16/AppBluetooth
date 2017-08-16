package com.example.vinic.appbluetooth;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by vinic on 16/08/2017.
 */

public class ListaDispositivos extends ListActivity {

    private BluetoothAdapter mBluetoothAdapter = null;

    static  String ENDERECO_MAC = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> arrayAdapterBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> dispositivosPareados = mBluetoothAdapter.getBondedDevices();

        if(dispositivosPareados.size()>0){
            for(BluetoothDevice dispositivo : dispositivosPareados){
                String nomeBt= dispositivo.getName();
                String macBt= dispositivo.getAddress();
                arrayAdapterBluetooth.add(nomeBt+" \n "+macBt);
            }
        }

        setListAdapter(arrayAdapterBluetooth);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String informacaoGeral = ((TextView) v).getText().toString();

        String enderecoMac = informacaoGeral.substring(informacaoGeral.length()-17);
        //Toast.makeText(getApplicationContext(),"Info: "+ enderecoMac, Toast.LENGTH_LONG).show();

        Intent retornaMac= new Intent();
        retornaMac.putExtra(ENDERECO_MAC, enderecoMac);
        setResult(RESULT_OK, retornaMac);
        finish();
    }
}
