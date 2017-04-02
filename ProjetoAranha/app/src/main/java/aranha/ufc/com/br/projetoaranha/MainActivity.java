package aranha.ufc.com.br.projetoaranha;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button pesquisar, ligar, desligar;
    ListView listaDispositivos;

    ArrayList<String> dispositivos = new ArrayList<String>();
    BluetoothAdapter adaptador;
    BluetoothServerSocket serverSocket;
    BluetoothSocket socket;
    String ip;

    DataInputStream in;
    DataOutputStream out;

    private static final UUID SerialPortService_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listaDispositivos = (ListView) findViewById(R.id.listaDispositivos);
        listaDispositivos.setCacheColorHint(Color.BLACK);
        listaDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String Item = listaDispositivos.getItemAtPosition(position).toString();
                ip = Item.split(" - ")[1];

                Toast.makeText(getApplicationContext(), ip, Toast.LENGTH_LONG).show();

                BluetoothDevice dispositivo = adaptador.getRemoteDevice(ip);

                try {
                    socket = dispositivo.createInsecureRfcommSocketToServiceRecord(SerialPortService_UUID);
                    socket.connect();
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        pesquisar = (Button) findViewById(R.id.btnPesquisar);
        pesquisar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descobrirDispositivos();
                adaptador.startDiscovery();
            }
        });

        ligar = (Button) findViewById(R.id.btnLigar);
        ligar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    out.write("A".getBytes());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        desligar = (Button) findViewById(R.id.btnDesligar);
        desligar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    out.write("B".getBytes());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        adaptador = BluetoothAdapter.getDefaultAdapter();
        if (adaptador == null) {
            Toast.makeText(getApplicationContext(), "ERRO", Toast.LENGTH_LONG).show();
        } else {
            if (adaptador.isEnabled()) {
                listarDispositivos();
            } else {
                Intent solicita = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(solicita, 1);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        listarDispositivos();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void listarDispositivos() {

        Set<BluetoothDevice> opcoesDispositivos = adaptador.getBondedDevices();
        for (BluetoothDevice d : opcoesDispositivos) {
            dispositivos.add(d.getName() + " - " + d.getAddress());
        }

        ArrayAdapter<String> adapterList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, dispositivos);

        listaDispositivos.setAdapter(adapterList);
    }

    private void descobrirDispositivos() {
        final BroadcastReceiver receptorDispositivos = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice disp = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    dispositivos.remove(disp.getName() + " - " + disp.getAddress());
                    dispositivos.add(disp.getName() + " - " + disp.getAddress());

                    ArrayAdapter<String> adapterList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, dispositivos);

                    listaDispositivos.setAdapter(adapterList);
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receptorDispositivos, filter);
    }

}
