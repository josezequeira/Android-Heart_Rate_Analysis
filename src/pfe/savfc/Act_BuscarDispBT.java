package pfe.savfc;

/* Librerias importadas */
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Esta actividad aparece como un dialogo. Lista los dipositivos
 * emparejados y tiene la opcion de detectar y emparejar nuevos
 * dispositivos. Cuando un dispositivo es elegido por el usuario
 * la direccion MAC de este es enviada a la actividad padre en 
 * el resultado del intento.
 */

/* Inicio Actividad Principal */
public class Act_BuscarDispBT extends Activity {
	
	/* Inicio Declaración de variables de la actividad */
	
	// Depuración
	private static final String TAG = "Act_configbt";
	
	// Adaptador local del Bluetooth
    private BluetoothAdapter miAdaptadorBT;
	
	// Variables resultado del intento
    public static String EXTRA_MAC_DISPOSITIVO = "mac_dispositivo";
    public static String EXTRA_NOMBRE_DISPOSITIVO = "nombre_dispositivo";
    
    // Adaptadores de los arreglos
    private ArrayAdapter<String> miAdaptadorArrayDispositivosEmparejados;
    private ArrayAdapter<String> miAdaptadorArrayNuevosDispositivos;
    
	/* Fin Declaración de variables de la actividad */
	
	
	/*- Inicio Metodos para el ciclo de vida de la actividad -*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// The activity is being created
    	super.onCreate(savedInstanceState);
    	Log.d(TAG, "+++ ON CREATE +++");
    	
    	// Se inicializa el layout
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	setContentView(R.layout.ly_buscardispbt_lista);
    	
    	// Poner en resultado CANCELED en caso de que el usuario se retire
        setResult(Activity.RESULT_CANCELED);
    	
        // Se inicializa el boton para realizar la busqueda de nuevos dispositivos
        Button scanButton = (Button) findViewById(R.id.ly_BuscarDispBT_Boton_Buscar);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BuscarDisp();
                v.setVisibility(View.GONE);
            }
        });
        
        // Se inicializan los arreglos, uno para los dispositivos
        // emparejados y otro para los que se encuentren
        miAdaptadorArrayDispositivosEmparejados = new ArrayAdapter<String>(this, R.layout.ly_buscardispbt_nombre);
        miAdaptadorArrayNuevosDispositivos = new ArrayAdapter<String>(this, R.layout.ly_buscardispbt_nombre);
        
        // Se configura el ListView para los dispositivos emparejados
        ListView pairedListView = (ListView) findViewById(R.id.ly_BuscarDispBT_disp_emparejados);
        pairedListView.setAdapter(miAdaptadorArrayDispositivosEmparejados);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        
        // Se configura el ListView para los nuevos dispositivos encontrados
        ListView newDevicesListView = (ListView) findViewById(R.id.ly_BuscarDispBT_nuevos_disp);
        newDevicesListView.setAdapter(miAdaptadorArrayNuevosDispositivos);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        
        // Se registra el broadcasts cuando un dispositivo es encontrado
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        
        // Se registra el broadcasts cuando la busqueda haya terminado
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);        
        
        // Obtiene el adaptador local del Bluetooth
        miAdaptadorBT = BluetoothAdapter.getDefaultAdapter();
        
        // Se obtienen los dispositivos emparejados
        Set<BluetoothDevice> pairedDevices = miAdaptadorBT.getBondedDevices();
        
        // Si hay dispositivos emparejados se agregan al ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.ly_BuscarDispBT_titulo_disp_emparejados).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                miAdaptadorArrayDispositivosEmparejados.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.Act_BuscarDispBT_NingunDispEmparejado).toString();
            miAdaptadorArrayDispositivosEmparejados.add(noDevices);
        }
	}
    
    @Override
    protected void onDestroy(){
    	// The activity is about to be destroyed
    	Log.d(TAG, "+++ ON DESTROY +++");
    	super.onDestroy();
    	
    	// Se asegura que no se esta haciendo alguna busqueda
        if (miAdaptadorBT != null) {
            miAdaptadorBT.cancelDiscovery();
        }
        
        // Cancelar los broadcast listeners
        this.unregisterReceiver(mReceiver);        
    }
	/*- Fin Metodos para el ciclo de vida de la actividad -*/
    
    
    /* Acción al presionar una tecla ó el boton de retroceder */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	Log.d(TAG, "+++ onKeyDown +++");
    	if ((keyCode == KeyEvent.KEYCODE_BACK)){
    		
    		// Finalizar actividad
    		Act_BuscarDispBT.this.finish();
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    
    /* Busca dispositivos con el adaptador Bluetooth */
    private void BuscarDisp() {
        Log.d(TAG, "+++ BuscarDisp +++");
        
        // Indica en el titulo que esta escaneando
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.Act_BuscarDispBT_EscaneandoDispositivo);
        
        // Activa subtitulos para nuevos dispositivos
        findViewById(R.id.ly_BuscarDispBT_titulo_nuevos_disp).setVisibility(View.VISIBLE);
        
        // Si se estuviera escaneando se detiene
        if (miAdaptadorBT.isDiscovering()) {
            miAdaptadorBT.cancelDiscovery();
        }
        
        // Se solicita escanear desde el adaptador Bluetooth
        miAdaptadorBT.startDiscovery();
    }
    
    
    /* Accion al clickear algun dispositivo listado en los ListViews */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	Log.d(TAG, "+++ OnItemClickListener +++");
            
        	// Se cancela el escaneo porque se va a iniciar la conexion
            miAdaptadorBT.cancelDiscovery();
            
            // Se obtiene la direccion MAC del dispositivo seleccionado, que son los ultimos 17 caracteres
            String info = ((TextView) v).getText().toString();
            String direccionMac = info.substring(info.length() - 17);
            String nombre = info.substring(0, info.length() - 17);
            
            // Se crea el resultado del intento y se incluye la direccion MAC y el nombre del dispositivo seleccionado
            Intent intent = new Intent();
            intent.putExtra(EXTRA_MAC_DISPOSITIVO, direccionMac);
            intent.putExtra(EXTRA_NOMBRE_DISPOSITIVO, nombre);
            
            // Se pone el resultado ok y se finaliza la actividad
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
    
    
    // El BroadcastReceiver que escuha los dispositivos encontrados y
    // cambia el titulo cuando la busqueda haya terminado
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {    	
        @Override
        public void onReceive(Context context, Intent intent) {
        	Log.d(TAG, "+++ BroadcastReceiver +++");
        	
            String action = intent.getAction();
            
            // Cuando se encuentra un dispositivo
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Se obtiene el objeto del dispositivo BT del intento
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Si esta emparejado se ignora porque ya ha sido listado
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    miAdaptadorArrayNuevosDispositivos.add(device.getName() + "\n" + device.getAddress());
                }
            // Cuando la busqueda ha terminado se cambia el titulo de la actividad
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.Act_BuscarDispBT_SeleccionarDispositivo);
                if (miAdaptadorArrayNuevosDispositivos.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.Act_BuscarDispBT_NingunDispEncontrado).toString();
                    miAdaptadorArrayNuevosDispositivos.add(noDevices);
                }
            }
        }
    };
    
}/* Fin Actividad Principal */