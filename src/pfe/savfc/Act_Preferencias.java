package pfe.savfc;

/* Librerias importadas */
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

/* Inicio Actividad */
public class Act_Preferencias extends PreferenceActivity implements OnPreferenceChangeListener {
	
	/* Inicio Declaración de variables de la actividad */
	
	// Depuración
	private static final String TAG = "Act_preferencias";
	
	// Códigos de petición de Intento
    private static final int PONER_MAC_NOMBRE_DISPOSITIVO = 1;
	
    // Variables resultado del intento
    private static String MAC_DISPOSITIVO;
    private static String NOMBRE_DISPOSITIVO;
    
	// Preferencias
    private Preference
    					Conf_Id,
    					Conf_nombre,
    					Conf_apellido,
    					Conf_edad,
    					Conf_sexo,
    					Conf_peso,
    					Conf_actividad,
    					Conf_DurReg,
    					Conf_RespMin,
    					Conf_Bluetooth,
    					Selec_Memoria,
    					Selec_Artefacto_Umbral,
    					Selec_Ventana,
    					Selec_GuardarArchivosProcesados;
    
    // SharedPreferences
    private SharedPreferences miSharedPreference;
	
	/* Fin Declaración de variables de la actividad */
	
	
	/*- Inicio Metodos para el ciclo de vida de la actividad -*/
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// The activity is being created
		super.onCreate(savedInstanceState);
		Log.d(TAG, "+++ ON CREATE +++");
		
		// Se activan las banderas para mantener la pantalla encendida
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Se inicializa el layout
		addPreferencesFromResource(R.xml.ly_preferencias);
		
		// Se inicializan las preferencias
		Conf_Id=(Preference)findPreference("Id_usuario");
		Conf_Id.setSummary(Conf_Id.getSharedPreferences().getString("Id_usuario", null));
		Conf_Id.setOnPreferenceChangeListener(this);
		
		Conf_nombre=(Preference)findPreference("Nombres");
		Conf_nombre.setSummary(Conf_nombre.getSharedPreferences().getString("Nombres", null));
		Conf_nombre.setOnPreferenceChangeListener(this);
		
		Conf_apellido=(Preference)findPreference("Apellidos");
		Conf_apellido.setSummary(Conf_apellido.getSharedPreferences().getString("Apellidos", null));
		Conf_apellido.setOnPreferenceChangeListener(this);
		
		Conf_edad=(Preference)findPreference("Edad");
		Conf_edad.setSummary(Conf_edad.getSharedPreferences().getString("Edad", null));
		Conf_edad.setOnPreferenceChangeListener(this);
		
		Conf_sexo=(Preference)findPreference("Sexo");
		Conf_sexo.setSummary(Conf_sexo.getSharedPreferences().getString("Sexo", null));
		Conf_sexo.setOnPreferenceChangeListener(this);
		
		Conf_peso=(Preference)findPreference("Peso");
		Conf_peso.setSummary(Conf_peso.getSharedPreferences().getString("Peso", null));
		Conf_peso.setOnPreferenceChangeListener(this);
		
		Conf_actividad=(Preference)findPreference("Actividad");
		Conf_actividad.setSummary(Conf_actividad.getSharedPreferences().getString("Actividad", null));
		Conf_actividad.setOnPreferenceChangeListener(this);
		
		Conf_DurReg=(Preference)findPreference("Duracion_Registro");
		Conf_DurReg.setSummary(Conf_DurReg.getSharedPreferences().getString("Duracion_Registro", null));
		Conf_DurReg.setOnPreferenceChangeListener(this);
		
		Conf_RespMin=(Preference)findPreference("Resp_Min");
		Conf_RespMin.setSummary(Conf_RespMin.getSharedPreferences().getString("Resp_Min", null));
		Conf_RespMin.setOnPreferenceChangeListener(this);
		
		Conf_Bluetooth=(Preference)findPreference("Bluetooth");
		Conf_Bluetooth.setOnPreferenceChangeListener(this);
		Conf_Bluetooth.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference preference) {
				Intent configbt = new Intent(Act_Preferencias.this, Act_BuscarDispBT.class);
				startActivityForResult(configbt, PONER_MAC_NOMBRE_DISPOSITIVO);
				return true;
			}
		});
		
		Selec_Memoria=(Preference)findPreference("Memoria");
		Selec_Memoria.setSummary(Selec_Memoria.getSharedPreferences().getString("Memoria", null));
		Selec_Memoria.setOnPreferenceChangeListener(this);
		
		Selec_Artefacto_Umbral=(Preference)findPreference("Artefacto_Umbral");
		Selec_Artefacto_Umbral.setSummary(Selec_Artefacto_Umbral.getSharedPreferences().getString("Artefacto_Umbral", null));
		Selec_Artefacto_Umbral.setOnPreferenceChangeListener(this);
		
		Selec_Ventana=(Preference)findPreference("Ventana");
		Selec_Ventana.setSummary(Selec_Ventana.getSharedPreferences().getString("Ventana", null));
		Selec_Ventana.setOnPreferenceChangeListener(this);
		
		Selec_GuardarArchivosProcesados=(Preference)findPreference("GA_Bandera");
		Selec_GuardarArchivosProcesados.setSummary(Selec_GuardarArchivosProcesados.getSharedPreferences().getString("GA_Bandera", null));
		Selec_GuardarArchivosProcesados.setOnPreferenceChangeListener(this);
	}
	
	@Override
	protected void onStart(){
		// The activity is about to become visible
		super.onStart();
		Log.d(TAG, "+++ ON START +++");
	}
	
	@Override
	protected synchronized void onResume(){
		// The activity has become visible (it is now "resumed")
		super.onResume();
		Log.d(TAG, "+++ ON RESUME +++");
    }
    
    @Override
    protected synchronized void onPause(){
    	// Another activity is taking focus (this activity is about to be "paused")
    	super.onPause();
    	Log.d(TAG, "+++ ON PAUSE +++");
    }
    
    @Override
    protected void onStop(){
    	// The activity is no longer visible (it is now "stopped")
    	Log.d(TAG, "+++ ON STOP +++");
    	super.onStop();    	
    }
    
    @Override
    protected void onDestroy(){
    	// The activity is about to be destroyed
    	Log.d(TAG, "+++ ON DESTROY +++");
    	super.onDestroy();
    }
	/*- Fin Metodos para el ciclo de vida de la actividad -*/
	
    
    /* Acción al presionar una tecla ó el boton de retroceder */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	Log.d(TAG, "+++ onKeyDown +++");
    	if ((keyCode == KeyEvent.KEYCODE_BACK)){
    		
    		// Finalizar actividad
    		Act_Preferencias.this.finish();
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    
    /* Acción al seleccionar el dispositivo Bluetooth */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "+++ onActivityResult +++: " + resultCode);
    	switch (requestCode) {
    	case PONER_MAC_NOMBRE_DISPOSITIVO:
    		if (resultCode == Activity.RESULT_OK) {
    			// Se obtiene la dirección mac y el nombre del dispositivo
    			MAC_DISPOSITIVO = data.getExtras().getString(Act_BuscarDispBT.EXTRA_MAC_DISPOSITIVO);
    			NOMBRE_DISPOSITIVO = data.getExtras().getString(Act_BuscarDispBT.EXTRA_NOMBRE_DISPOSITIVO);
    			
    			// Se guarda en SharedPreferences miBluetooth
				miSharedPreference = getSharedPreferences("miBluetooth",Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = miSharedPreference.edit();
				editor.putString("miBluetooth",MAC_DISPOSITIVO);
				editor.commit();
				
				// Visualizar el nombre y la mac en las preferencias
				Conf_Bluetooth.setTitle(NOMBRE_DISPOSITIVO);
				Conf_Bluetooth.setSummary(MAC_DISPOSITIVO);
            }
    	}
    }
    
    
    /* Actualiza los cambios en las preferencias de forma inmediata */ 
    public boolean onPreferenceChange(Preference preference,Object newValue){
    	Log.d(TAG, "+++ onPreferenceChange +++");
    	if (preference.getKey().equals(Conf_Id.getKey())){
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Conf_nombre.getKey())){
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Conf_apellido.getKey())){
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Conf_edad.getKey())){
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Conf_sexo.getKey())){
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Conf_peso.getKey())){
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Conf_actividad.getKey())){
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Conf_DurReg.getKey())){
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Conf_RespMin.getKey())){
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Conf_Bluetooth.getKey())){
    		preference.setTitle( (String) newValue );
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Selec_Memoria.getKey())){
    		
    		if (newValue.equals(R.string.En_la_memoria_del_TEL)) {
    			// Se actualiza el resumen de la preferencia
    			preference.setSummary(R.string.En_la_memoria_del_TEL);
    		}
    		else {
    			// Se verifica que el almacenamiento externo se encuentre disponible
        		String estado = Environment.getExternalStorageState();
        		
        		if (Environment.MEDIA_MOUNTED.equals(estado)) {
        			// Se puede leer y escribir de la tarjeta SD
        			// Se actualiza el resumen de la preferencia
        			preference.setSummary( (String) newValue);
        		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(estado)) {
        			// Unicamente se puede leer de la tarjeta SD, no se puede escribir
        			Toast.makeText(this, R.string.Toast_TSD_No_se_puede_escribir, Toast.LENGTH_LONG).show();
        			// Se actualiza el resumen de la preferencia
        			preference.setSummary(R.string.Toast_TSD_No_se_puede_escribir);
        		} else {
        			// No se puede leer ni escribir en la tarjeta SD
        			Toast.makeText(this, R.string.Toast_TSD_No_se_puede_leer_ni_escribir, Toast.LENGTH_LONG).show();
        			// Se actualiza el resumen de la preferencia
        			preference.setSummary(R.string.Toast_TSD_No_se_puede_leer_ni_escribir);
        		}
    		}
    		return true;
    	}
    	else if (preference.getKey().equals(Selec_Artefacto_Umbral.getKey())) {
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Selec_Ventana.getKey())) {
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	else if (preference.getKey().equals(Selec_GuardarArchivosProcesados.getKey())) {
    		preference.setSummary( (String) newValue);
    		return true;
    	}
    	
    	return false;
    }
    
	
}/* Fin Actividad */
