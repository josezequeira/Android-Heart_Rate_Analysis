package pfe.savfc;

/* Librerias importadas */
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

/* Inicio Actividad Principal */
public class Act_Principal extends Activity {
	
	/* Inicio Declaración de variables de la actividad */
	
	// Depuración
	private static final String TAG = "Act_Principal";
	
	// Elementos del layout
	private ImageView realizarreg, regguardados, preferencias, salir, ayuda;
	
	// Adaptador local del Bluetooth
    private BluetoothAdapter miAdaptadorBT;
	
    // Códigos de petición de Intento
    private static final int PETICION_ACTIVAR_BT = 1;
    
    // Dialogos
    private static AlertDialog.Builder dlgAyudaBuilder;
    private static AlertDialog dlgAyuda;
    private static AlertDialog.Builder dlgAyudaBuilder2;
    private static AlertDialog dlgAyuda2;
    private static AlertDialog.Builder dlgAyudaBuilder3;
    private static AlertDialog dlgAyuda3;
    private static AlertDialog.Builder dlgAyudaBuilder4;
    private static AlertDialog dlgAyuda4;
    private static AlertDialog.Builder dlgAyudaBuilder5;
    private static AlertDialog dlgAyuda5;
    private static AlertDialog.Builder dlgAyudaBuilder6;
    private static AlertDialog dlgAyuda6;
    private static AlertDialog.Builder dlgSalirBuilder;
    private static AlertDialog dlgSalir;
	/* Fin Declaración de variables de la actividad */
	
	
	/**- Inicio Metodos para el ciclo de vida de la actividad -*/
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
    	setContentView(R.layout.ly_principal);
        
    	// Se crean los dialogos
    	dialogsCreateAndSetup();
    	
    	// Se inicializan los elementos del layout
        realizarreg=(ImageView)findViewById(R.id.ly_actprincipal_Boton_realizarregistro);
        realizarreg.setOnClickListener(new ImageView.OnClickListener(){
        	public void onClick(View v) {
        		Intent intento = new Intent(Act_Principal.this, Act_RealizarRegistro.class);
        		startActivity(intento);
			}
        });
        
        regguardados=(ImageView)findViewById(R.id.ly_actprincipal_Boton_registrosguardados);
        regguardados.setOnClickListener(new ImageView.OnClickListener(){
        	public void onClick(View v) {
        		Intent intento = new Intent(Act_Principal.this, Act_RegGuardados.class);
        		startActivity(intento);
			}
        });
        
        preferencias=(ImageView)findViewById(R.id.ly_actprincipal_Boton_preferencias);
        preferencias.setOnClickListener(new ImageView.OnClickListener(){
			public void onClick(View v) {
				Intent intento = new Intent(Act_Principal.this, Act_Preferencias.class);
				startActivity(intento);
			}
        });
        
        salir=(ImageView)findViewById(R.id.ly_actprincipal_Boton_salir);
        salir.setOnClickListener(new ImageView.OnClickListener(){
        	public void onClick	(View v) {
        		if (dlgSalir != null) {
        			if (dlgSalir.isShowing() == false) {
        				dlgSalir.show();
        			}
       			}
			}
        });
        
        ayuda=(ImageView)findViewById(R.id.ly_actprincipal_Boton_ayuda);
        ayuda.setOnClickListener(new ImageView.OnClickListener(){
			public void onClick(View v) {
				if (dlgAyuda != null) {
					if (dlgAyuda.isShowing() == false) {
						dlgAyuda.show();
					}
				}
				//Toast.makeText(Act_Principal.this, R.string.Toast_En_Construccion, Toast.LENGTH_SHORT).show();
			}
        });
        
        // Se obtiene el adaptador Bluetooth
		miAdaptadorBT = BluetoothAdapter.getDefaultAdapter();
        
        // Verifica si el telefono movil soporta Bluetooth
    	// si el adaptador es nulo el dispositivo no soporta el Bluetooth 
        if (miAdaptadorBT == null) {
        	Toast.makeText(this, R.string.Toast_BT_No_Soporta, Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }else{
        	//El dispisitivo si soporta el Bluetooth
        }
        
    }
    
	@Override
	protected void onStart(){
		// The activity is about to become visible
		super.onStart();
		Log.d(TAG, "+++ ON START +++");
		
		// Si el Bluetooth no esta activado, pedir que se active
		if (!miAdaptadorBT.isEnabled()) {
            // No está activado, y se pide su activacion
			Intent intento = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intento, PETICION_ACTIVAR_BT);
			return;
        } else {
        	// Si está activado
        }
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
	/**- Fin Metodos para el ciclo de vida de la actividad -*/
    
    
    /* Acción al activar el Bluetooth */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "+++ onActivityResult +++: " + resultCode);
    	switch (requestCode) {
    	case PETICION_ACTIVAR_BT:
    		if (resultCode == Activity.RESULT_OK) {
                // El BT está activado
    			Toast.makeText(this, R.string.Toast_BT_Activado, Toast.LENGTH_SHORT).show();
            } else {
                // El usuario no ha activado el BT ó ha sucedido algun error
                Log.d(TAG, "Bluetooth no activado");
                Toast.makeText(this, R.string.Toast_BT_No_Activado, Toast.LENGTH_SHORT).show();
                finish();
            }
    	}
    }
    
    
    /* Menu de Opciones */ 
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	Log.d(TAG, "+++ onCreateOptionsMenu +++");
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu_principal, menu);
		return true;
    }
    
    
    /* Acción al seleccionar item del menu de opciones*/ 
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	Log.d(TAG, "+++ onOptionsItemSelected +++");
    	switch (item.getItemId()){
    	case R.id.menu_pricipal_preferencias:
    		Intent intento = new Intent(Act_Principal.this, Act_Preferencias.class);
    		startActivity(intento);
    		return true;
    	case R.id.menu_pricipal_salir:
    		 if (dlgSalir != null) {
    			 if (dlgSalir.isShowing() == false) {
    				 dlgSalir.show();
    			 }
    		 }
    		 return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    
    /* Acción al presionar una tecla ó el boton de retroceder */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	Log.d(TAG, "+++ onKeyDown +++");
    	if ((keyCode == KeyEvent.KEYCODE_BACK)){
    		if (dlgSalir != null) {
    			if (dlgSalir.isShowing() == false) {
    				dlgSalir.show();
    			}
   			}
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    
    /** Dialogos */
    /* Crear he iniciar dialogod */ 
    private void dialogsCreateAndSetup() {
    	Log.d(TAG, "+++ dialogsCreateAndSetup +++");
    	
    	// Crear dialogos
    	dlgSalirBuilder = new AlertDialog.Builder(this);
        dlgSalir = null;
    	dlgAyudaBuilder = new AlertDialog.Builder(this);
        dlgAyuda = null;
        dlgAyudaBuilder2 = new AlertDialog.Builder(this);
        dlgAyuda2 = null;
        dlgAyudaBuilder3 = new AlertDialog.Builder(this);
        dlgAyuda3 = null;
        dlgAyudaBuilder4 = new AlertDialog.Builder(this);
        dlgAyuda4 = null;
        dlgAyudaBuilder5 = new AlertDialog.Builder(this);
        dlgAyuda5 = null;
        dlgAyudaBuilder6 = new AlertDialog.Builder(this);
        dlgAyuda6 = null;
    	
    	// Configurar dialogo salir
        dlgSalirBuilder.setMessage(R.string.Dialogo_Salir_Texto);
        dlgSalirBuilder.setCancelable(false);
        dlgSalirBuilder.setPositiveButton(R.string.Dialogo_Si, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id) {
				// Finalizar actividad
				Act_Principal.this.finish();
			}
		});
        dlgSalirBuilder.setNegativeButton(R.string.Dialogo_No, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				// Quitar el dialogo
				dialog.dismiss();
			}
		});
        // Crear dialigo salir
        dlgSalir = dlgSalirBuilder.create();
        
        
        // Configurar dialogo ayuda
        dlgAyudaBuilder.setTitle(R.string.Dialogo_Ayuda_Titulo);
        dlgAyudaBuilder.setMessage(R.string.Dialogo_Ayuda_Mensaje_1_3);
        dlgAyudaBuilder.setCancelable(true);
        dlgAyudaBuilder.setNegativeButton(R.string.Dialogo_Ayuda_Boton, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Quitar el dialogo
				dialog.dismiss();
			}
        });
        dlgAyudaBuilder.setPositiveButton(R.string.Dialogo_Ayuda_Siguiente, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Mostrar siguiente mensaje
				if (dlgAyuda2 != null) {
					dlgAyuda2.show();
				}
			}
        });
    	// Crear dialogo ayuda
        dlgAyuda = dlgAyudaBuilder.create();
        
        
        // Configurar dialogo ayuda 2
        dlgAyudaBuilder2.setTitle(R.string.Dialogo_Ayuda_Titulo);
        dlgAyudaBuilder2.setMessage(R.string.Dialogo_Ayuda_Mensaje_4);
        dlgAyudaBuilder2.setCancelable(true);
        dlgAyudaBuilder2.setNegativeButton(R.string.Dialogo_Ayuda_Boton, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Quitar el dialogo
				dialog.dismiss();
			}
        });
        dlgAyudaBuilder2.setPositiveButton(R.string.Dialogo_Ayuda_Siguiente, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Mostrar siguiente mensaje
				if (dlgAyuda3 != null) {
					dlgAyuda3.show();
				}
			}
        });
    	// Crear dialogo ayuda
        dlgAyuda2 = dlgAyudaBuilder2.create();
        
        
        // Configurar dialogo ayuda 3
        dlgAyudaBuilder3.setTitle(R.string.Dialogo_Ayuda_Titulo);
        dlgAyudaBuilder3.setMessage(R.string.Dialogo_Ayuda_Mensaje_5);
        dlgAyudaBuilder3.setCancelable(true);
        dlgAyudaBuilder3.setNegativeButton(R.string.Dialogo_Ayuda_Boton, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Quitar el dialogo
				dialog.dismiss();
			}
        });
        dlgAyudaBuilder3.setPositiveButton(R.string.Dialogo_Ayuda_Siguiente, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Mostrar siguiente mensaje
				if (dlgAyuda4 != null) {
					dlgAyuda4.show();
				}
			}
        });
    	// Crear dialogo ayuda
        dlgAyuda3 = dlgAyudaBuilder3.create();
        
        
        // Configurar dialogo ayuda 4
        dlgAyudaBuilder4.setTitle(R.string.Dialogo_Ayuda_Titulo);
        dlgAyudaBuilder4.setMessage(R.string.Dialogo_Ayuda_Mensaje_6);
        dlgAyudaBuilder4.setCancelable(true);
        dlgAyudaBuilder4.setNegativeButton(R.string.Dialogo_Ayuda_Boton, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Quitar el dialogo
				dialog.dismiss();
			}
        });
        dlgAyudaBuilder4.setPositiveButton(R.string.Dialogo_Ayuda_Siguiente, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Mostrar siguiente mensaje
				if (dlgAyuda5 != null) {
					dlgAyuda5.show();
				}
			}
        });
    	// Crear dialogo ayuda
        dlgAyuda4 = dlgAyudaBuilder4.create();
        
        
        // Configurar dialogo ayuda 5
        dlgAyudaBuilder5.setTitle(R.string.Dialogo_Ayuda_Titulo);
        dlgAyudaBuilder5.setMessage(R.string.Dialogo_Ayuda_Mensaje_7_8);
        dlgAyudaBuilder5.setCancelable(true);
        dlgAyudaBuilder5.setNegativeButton(R.string.Dialogo_Ayuda_Boton, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Quitar el dialogo
				dialog.dismiss();
			}
        });
        dlgAyudaBuilder5.setPositiveButton(R.string.Dialogo_Ayuda_Siguiente, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Mostrar siguiente mensaje
				if (dlgAyuda6 != null) {
					dlgAyuda6.show();
				}
			}
        });
    	// Crear dialogo ayuda
        dlgAyuda5 = dlgAyudaBuilder5.create();
        
        
        // Configurar dialogo ayuda 6
        dlgAyudaBuilder6.setTitle(R.string.Dialogo_Ayuda_Titulo);
        dlgAyudaBuilder6.setMessage(R.string.Dialogo_Ayuda_Mensaje_9);
        dlgAyudaBuilder6.setCancelable(true);
        dlgAyudaBuilder6.setNegativeButton(R.string.Dialogo_Ayuda_Boton, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Quitar el dialogo
				dialog.dismiss();
			}
        });
    	// Crear dialogo ayuda
        dlgAyuda6 = dlgAyudaBuilder6.create();
        
    }
    
}/* Fin Actividad Principal */