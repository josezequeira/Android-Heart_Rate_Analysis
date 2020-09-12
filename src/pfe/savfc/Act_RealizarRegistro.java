package pfe.savfc;

/* Librerias importadas */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.TextView;
import android.widget.Toast;

/* Inicio Actividad */
public class Act_RealizarRegistro extends Activity {
	
	/* Inicio Declaración de variables de la actividad */
	
	// Depuración
	private static final String TAG = "Act_inireg";
	
	// Bluetooth
    private static BluetoothAdapter miAdaptadorBT; //Adaptador local del Bluetooth
    private static final UUID miUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static BluetoothSocket miSocket = null;
    
    // Elementos del layout
    private static TextView miEstado;
    private static Chronometer miCronometro;
    private static TextView miActividad;
    protected static VFC_View VFCgrafica;
    private static TextView miAnimacion;
    private static Button iniciarRegistro;
    
    // Variables Globales
    private static boolean BANDERA_PREFERENCIAS;
    private static String ID_USUARIO = null;			// Identificacion del Usuario
    private static String NOMBRE = null;				// Nombre del usuario
    private static String APELLIDO = null;				// Apellido del usuario
    private static String EDAD = null;					// Edad del usuario
    private static String SEXO = null;					// Sexo del usuario
    private static String PESO = null;					// Peso del usuario
    private static String ACTIVIDAD_REGISTRO = null;	// Actividad previa al registro
    private static String TIEMPO_REGISTRO = null;		// Tiempo de duración del registro
    private static String RESP_MIN = null;				// Respiracion por minuto
    private static String NOMBRE_DISPOSITIVO = null;	// Nombre del dispositivo BT a conectar
    private static String MAC_DISPOSITIVO = null;		// Direccion del dispositivo BT a conectar
    private static String MEMORIA_GUAR_ARCH = null;		// Memoria done se van a guardar los archivos 
    private static boolean BANDERA_DISPCONECTADO;
    private static boolean BANDERA_CONEXIONCANCELADA;
    private static boolean BANDERA_DURREG;				// Bandera que indica si se habilita o no el boton
    private static boolean BANDERA_PRIMERPAQUETERECIBIDO;
    private int latidosviejos = 0;
    private int latidosnuevos = 0;
    private long numerolatido = 0;
    private long TRR = 0;								// Tiempo entre cada ladito
    private static long minutosTranscurridos;			// Tiempo transcurrido de registro
    private static long minutos;						// Tiempo de duración del registro
    private static String Nombre_Archivo;				// Nombre del archivo a guardar
    private int cont;									// Contador para cambiar la animación
    
    // Vector de tiempos RR
    private Vector<Long> RR = new Vector<Long>();
    
    // Instancia de las preferencias
    private static SharedPreferences miPref, miSharedPreference;
    
    // Objetos de diferentes clases
    private conectarDispositivo miConectarDispositivo = new conectarDispositivo();
    private leerDispositivo miLeerDispositivo = new leerDispositivo();
    private SimpleDateFormat miFormatoFechaHora = new SimpleDateFormat("yyyyMMdd-kkmm", Locale.getDefault());
    private MediaPlayer mediaPlayer;
	/* Fin Declaración de variables de la actividad */
	
	
	/*- Inicio Metodos para el ciclo de vida de la actividad -*/
	@Override
	protected void onCreate(Bundle savedInstanceState){
		// The activity is being created
		super.onCreate(savedInstanceState);
		Log.d(TAG, "+++ ON CREATE +++");
		
		// Se icicializan variables
		BANDERA_PREFERENCIAS = false;
		BANDERA_DISPCONECTADO = false;
		BANDERA_CONEXIONCANCELADA = false;
		BANDERA_DURREG = false;
		BANDERA_PRIMERPAQUETERECIBIDO = true;
		
		// Se activan las banderas para mantener la pantalla encendida
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Se inicializa el layout
		setContentView(R.layout.ly_realizarregistro);
		
		// Se indica que las teclas de volumen del telefono ajusten el volumen de la aplicacion
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		// Se Obtienen las preferencias
		BANDERA_PREFERENCIAS = obtenerPreferencias();
		
		// Se verifican las preferencias
		verificarPreferencias();
		
		// Se inicializan los elementos del layout
		miEstado = (TextView) findViewById(R.id.ly_RealizarRegistro_Estado_valor);
		miEstado.setText(R.string.Act_RealizarRegistro_Inicializando);
		miCronometro = (Chronometer) findViewById(R.id.ly_RealizarRegistro_Cronometro_Valor);
		miActividad = (TextView) findViewById(R.id.ly_RealizarRegistro_Actividad_Valor);
		miAnimacion = (TextView) findViewById(R.id.ly_RealizarRegistro_Animacion);
		VFCgrafica = (VFC_View) findViewById(R.id.VFC_Grafica);
		iniciarRegistro = (Button) findViewById(R.id.ly_RealizarRegistro_BotonInireg_Texto);
		iniciarRegistro.setEnabled(false); // boton deshabilitado al inicar
		
		// Se muestra la actividad en pantalla
		if (ACTIVIDAD_REGISTRO != null) {
			miActividad.setText(ACTIVIDAD_REGISTRO);
		}
		
		// Accion al pulsar el boton de iniciar registro
		iniciarRegistro.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View arg0) {
				// Se inicia el cronometro
				miCronometro.setBase(SystemClock.elapsedRealtime());
				miCronometro.start();
				// Se deshabilita el boton
				iniciarRegistro.setEnabled(false);
				// Se inicializa la variable y el vector
				numerolatido = 0;
				RR.clear();
				// Se cambia el estado
		    	miEstado.setText("Registrando...");
			}
		});
		
		// Accion cada vez que pasa un segundo en el cronometo
		miCronometro.setOnChronometerTickListener(new OnChronometerTickListener(){
			public void onChronometerTick(Chronometer arg0) {
				minutosTranscurridos = ((SystemClock.elapsedRealtime() - miCronometro.getBase())/1000)/60;
				
				if (minutosTranscurridos >= minutos) {
					// Detener cronometro
					miCronometro.stop();
			    	// Detener procesos ó hilos
			    	if (BANDERA_CONEXIONCANCELADA == false) {
			    		BANDERA_CONEXIONCANCELADA = true;
			    		miLeerDispositivo.cancel(true);
			        	miConectarDispositivo.onCancelled();
			    	}
			    	// Se cambia el estado
			    	miEstado.setText("Registro finalizado");
			    	// Se acota el tamaño del vector RR
			    	RR.trimToSize();
			    	// Se guarda el registro en un archivo
			    	guardarArchivo();
				}
			}
		});
		
		// Se obtiene el adaptador Bluetooth
		miAdaptadorBT = BluetoothAdapter.getDefaultAdapter();
	}
	
	@Override
	protected void onStart(){
		// The activity is about to become visible
		super.onStart();
		Log.d(TAG, "+++ ON START +++");
		
		// Se muestra en pantalla la preferencia de duración de registro
		if (TIEMPO_REGISTRO != null) {
			if (TIEMPO_REGISTRO.equals("5 minutos")) {
				miCronometro.setText("05:00");
				minutos = 5;
				BANDERA_DURREG = true;
			} else if (TIEMPO_REGISTRO.equals("10 minutos")) {
				miCronometro.setText("10:00");
				minutos = 10;
				BANDERA_DURREG = true;
			} else if (TIEMPO_REGISTRO.equals("15 minutos")) {
				miCronometro.setText("15:00");
				minutos = 15;
				BANDERA_DURREG = true;
			} else {
				miCronometro.setText("Seleccionar en Preferencias");
				iniciarRegistro.setEnabled(false);
				iniciarRegistro.setText("Introducir duración en las preferencias");
			}
		}
		
		if (RESP_MIN != null) {
			if (RESP_MIN.equals("00 rpm-libre")) {
				// No se anima la imagen porque la sincronización
				// cardiorespiratoria es libre
			} else {
				// Se inicia la animacion de la imagen
				animarImagen();
			}
		}
		
	}
	
	@Override
	protected synchronized void onResume(){
		// The activity has become visible (it is now "resumed")
		super.onResume();
		Log.d(TAG, "+++ ON RESUME +++");
		
		// Se verifica que no exista una conexión y las preferencias se hayan cargado
		if ((BANDERA_DISPCONECTADO == false) && (obtenerPreferencias() == true)){
			// Iniciar conexion con el dispositivo BT
			miConectarDispositivo.execute();
		}
		
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
    	
    	// Detener procesos ó hilos
    	if (BANDERA_CONEXIONCANCELADA == false) {
    		miLeerDispositivo.cancel(true);
        	miConectarDispositivo.onCancelled();
    	}
		
    	// Se libera el mediaplayer y la animación
    	if (RESP_MIN != null) {
			if (RESP_MIN.equals("00 rpm-libre")) {
				// No se liberan porque no fueron iniciadas
			} else {
				// Liberar el MediaPlayer
		    	mediaPlayer.release();
		    	mediaPlayer = null;
		    	// Se detiene la animacion
		    	miAnimacion.clearAnimation();
			}
		}
    	
    	// Detener el cronometro
    	miCronometro.stop();
    	
    	// Vaciar vector
    	RR.removeAllElements();
    	
    	// Se pone la bandera en falso
    	BANDERA_DISPCONECTADO = false;
    }
	/*- Fin Metodos para el ciclo de vida de la actividad -*/
    
    
    /* Acción al presionar una tecla ó el boton de retroceder */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	Log.d(TAG, "+++ onKeyDown +++");
    	if ((keyCode == KeyEvent.KEYCODE_BACK)){
    		
    		// Finalizar actividad
            Act_RealizarRegistro.this.finish();
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    
	/* Dialogo para verificar las preferencias del registro */
    private static final int DIALOGO_PONERPREFERENCIAS = 1;
    @Override
    protected Dialog onCreateDialog(int id){
    	Log.d(TAG, "+++ onCreateDialog +++");
	    switch(id){
    	case DIALOGO_PONERPREFERENCIAS:
    		// Se crea un dialogo de alerta
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(R.string.Dialogo_Introducir_preferencias_Titulo);
    		builder.setMessage(R.string.Dialogo_Introducir_preferencias_Texto);
    		builder.setCancelable(false);
    		builder.setPositiveButton(R.string.Dialogo_Aceptar, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id) {
					Intent preferencias = new Intent(Act_RealizarRegistro.this, Act_Preferencias.class);
					startActivity(preferencias);
					// Quitar dialogo
					dialog.cancel();
					// Finalizar actividad
					Act_RealizarRegistro.this.finish();
				}
			});
    		builder.setNegativeButton(R.string.Dialogo_Cancelar, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					// Quitar dialogo
					dialog.cancel();
					// Finalizar actividad
					Act_RealizarRegistro.this.finish();
				}
			});
    		AlertDialog salir = builder.create();
    		salir.show();
    	}
    	return super.onCreateDialog(id);
    }
    
    
    /* Obtener las preferencias relevantes para el registro */
    public boolean obtenerPreferencias() {
    	Log.d(TAG, "+++ obtenerPreferencias +++");
    	
    	// Se inicializan las preferencias
    	miPref = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	// Se obtienen los valores de las preferencias
    	ID_USUARIO = miPref.getString("Id_usuario", null);
    	NOMBRE = miPref.getString("Nombres", null);
    	APELLIDO = miPref.getString("Apellidos", null);
    	EDAD = miPref.getString("Edad", null);
    	SEXO = miPref.getString("Sexo", null);
    	PESO = miPref.getString("Peso", null);
    	ACTIVIDAD_REGISTRO = miPref.getString("Actividad", null);
    	TIEMPO_REGISTRO = miPref.getString("Duracion_Registro", null);
    	RESP_MIN = miPref.getString("Resp_Min", null);
    	MEMORIA_GUAR_ARCH = miPref.getString("Memoria", null);
    	
    	// Se obtiene los valores de SharedPreferences
    	miSharedPreference = getSharedPreferences("miBluetooth", Activity.MODE_PRIVATE);
    	MAC_DISPOSITIVO = miSharedPreference.getString("miBluetooth", null);
    	
    	// Si se obtienen todas se envia verdadero
    	if ((ID_USUARIO != null) &&
    		(TIEMPO_REGISTRO != null) &&
    		(ACTIVIDAD_REGISTRO != null) &&
    		(RESP_MIN != null) &&
    		(MEMORIA_GUAR_ARCH != null) &&
    		(MAC_DISPOSITIVO != null)) {
    		return true;
    	}else{
    		return false;
    	}
    }
    
    
    /* Verifica las preferencias para el registro */
    public void verificarPreferencias(){
    	Log.d(TAG, "+++ verificarPreferencias +++");
    	
    	if (BANDERA_PREFERENCIAS == false) {
    		// No se han cargado los datos de las preferencias, mostrar dialogo
    		showDialog(DIALOGO_PONERPREFERENCIAS);
    	}
    	else {
    		// Continuar con la actividad
    	}
    }
    
    
    /* Metodo para animar la imagen de sincronizacion cardio respiratoria (SCR) */
    public void animarImagen() {
    	
    	// Instancia de la clase que anima los objetos
    	AnimarView Animacion = new AnimarView();
    	
    	// Se monitorea la animacion
    	Animacion.setAnimationListener(new AnimarViewListener());
    	
    	// Se aplica la animacion a la vista
    	miAnimacion.startAnimation(Animacion);
    }
    
    
    /* Inicio clase para monitorear los estados de la animación */
	public class AnimarViewListener implements Animation.AnimationListener {
    	private AnimarViewListener(){}
    	
    	public void onAnimationStart(Animation animation) {
    		Log.i(TAG, "+++ onAnimationStart +++");
    		
    		// Se calcula la velocidad de la animacion en base a las respiraciones por minuto
    		long duracion_ms = 0;
    		long resp_min = 0;
    		if (RESP_MIN == null) {
    			resp_min = 15;
    		} else {
    			resp_min = Long.valueOf(RESP_MIN.substring(0, 2));
    		}
    		
    		if (resp_min == 0) {
    			// No se anima el view, la sincronización
    			// cardioresporatoria es libre
    		} else {
    			duracion_ms = (60/resp_min)*(1000/2);
        		animation.setDuration(duracion_ms);
        		cont = 0;
        		miAnimacion.setText(R.string.Animacion_Inhalar);
        		mediaPlayer = MediaPlayer.create(Act_RealizarRegistro.this, R.raw.beep2);
        		mediaPlayer.start();
    		}
    	}
    	
    	public void onAnimationRepeat(Animation animation) {
    		Log.i(TAG, "+++ onAnimationRepeat +++");
    		
    		cont++;
    		if ( (cont % 2) != 0) {
    			miAnimacion.setText(R.string.Animacion_Exhalar);
    		} else {
    			miAnimacion.setText(R.string.Animacion_Inhalar);
    		}
    		if (mediaPlayer != null) {
    			mediaPlayer.start();
    		}
    	}
    	
    	public void onAnimationEnd(Animation animation) {
    		Log.i(TAG, "+++ onAnimationEnd +++");
    	}
    }/* Fin clase para monitorear los estados de la animación */
    
    
    /* Guardar archivo */
    public void guardarArchivo() {
    	if (MEMORIA_GUAR_ARCH != null) {
    		
    		// Variable de indicacion de que se ha guardado el archivo
    		boolean guardaOK = true;
    		
    		// Se obtiene la fecha y la hora con el formato definido
    		String FechaHora = miFormatoFechaHora.format(new Date());
    		
    		// Se crea el nombre del archivo
    		Nombre_Archivo = ID_USUARIO+"-"+FechaHora+".txt";
    		Log.i(TAG,"+++ Nombre del Archivo = " + Nombre_Archivo + " +++");
    		
    		// Se genera la cabecera del archivo
    		String Cabecera =	"ID=" +ID_USUARIO +"\n"+
    							"Fecha y Hora=" +FechaHora +"\n"+
    							"Nombres=" +NOMBRE +"\n"+
    							"Apellidos=" +APELLIDO +"\n"+
    							"Edad=" +EDAD +"\n"+
    							"Sexo=" +SEXO +"\n"+
    							"Peso=" +PESO +"\n"+
    							"Actividad=" +ACTIVIDAD_REGISTRO +"\n"+
    							"Duración=" +TIEMPO_REGISTRO +"\n"+
    							"Resp_min=" +RESP_MIN +"\n";
    		
    		// Se genera el archivo a guardar
    		String Archivo = Cabecera + RR.toString();
    		Log.i(TAG,Archivo);
    		
    		if (MEMORIA_GUAR_ARCH.equals("En la memoria del Telefono")) {
    			FileOutputStream fos = null;
    			try {
    				fos = openFileOutput(Nombre_Archivo, Act_RealizarRegistro.MODE_PRIVATE);
    				fos.write(Archivo.getBytes());
    			} catch (FileNotFoundException e) {
    				Log.e(TAG, "guardarArchivo en memoria del telefono error creando el archivo", e);
    				guardaOK = false;
    			} catch (IOException e) {
    				Log.e(TAG, "guardarArchivo en memoria del telefono error escribiendo el archivo", e);
    				guardaOK = false;
    			} finally {
    				if (fos != null) {
    					try {
    						fos.flush();
    						fos.close();
    					} catch (IOException e) {
    						Log.e(TAG, "guardarArchivo en memoria del telefono error cerrando el archivo", e);
    						guardaOK = false;
    					}
    				}
    			}
    		} else {
    			// Inicio: Se verifica que el almacenamiento externo se encuentre disponible
        		String estado = Environment.getExternalStorageState();
        		
        		if (Environment.MEDIA_MOUNTED.equals(estado)) {
        			// Se puede leer y escribir de la tarjeta SD
        			
        			// Inicio: Se guarda en la memoria de la tarjeta SD
        			//File sdDir = new File("/sdcard/");
        			File sdDir = new File(Environment.getExternalStorageDirectory().getPath());
        			
        			if (sdDir.exists() && sdDir.canWrite()) {
        				File appDir = new File(sdDir.getAbsolutePath() + "/Análisis_VFC");
        				appDir.mkdir();
        				
        				if (appDir.exists() && appDir.canWrite()) {
        					File file = new File(appDir.getAbsolutePath() + "/" + Nombre_Archivo);
        					
        					try {
        						file.createNewFile();
        					} catch (IOException e) {
        						Log.e(TAG,"guardarArchivo en tarjeta SD error creando el archivo", e);
        					}
        					
        					if (file.exists() && file.canWrite()) {
        						FileOutputStream fos = null;
        						
        						try {
        							fos = new FileOutputStream(file);
        							fos.write(Archivo.getBytes());
        						} catch (FileNotFoundException e) {
        							Log.e(TAG, "guardarArchivo en tarjeta SD error", e);
        						} catch (IOException e) {
        							Log.e(TAG, "guardarArchivo en tarjeta SD error", e);
        						} finally {
        							if (fos != null) {
        								try {
        									fos.flush();
        									fos.close();
        								} catch (IOException e) {
        									Log.e(TAG, "guardarArchivo en tarjeta SD error", e);
        								}
        							}
        						}
        					} else {
        						Log.e(TAG, "+++ guardarArchivo en tarjeta SD error escribiendo el archivo +++");
        						guardaOK = false;
        					}
        				} else {
        					Log.e(TAG, "+++ guardarArchivo en tarjeta SD error no se puede escribir en /sdcard/Análisis_VFC +++");
        					guardaOK = false;
        				}
        			} else {
        				Log.e(TAG, "+++ guardarArchivo en tarjeta SD error, directorio /sdcard no disponible +++");
        				guardaOK = false;
        			}
        			// Fin: Se guarda en la memoria de la tarjeta SD
        			
        		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(estado)) {
        			// Unicamente se puede leer de la tarjeta SD
        			Toast.makeText(this, R.string.Toast_TSD_No_se_puede_escribir, Toast.LENGTH_LONG).show();
        			guardaOK = false;
        		} else {
        			// No se puede leer ni escribir en la tarjeta SD
        			Toast.makeText(this, R.string.Toast_TSD_No_se_puede_leer_ni_escribir, Toast.LENGTH_LONG).show();
        			guardaOK = false;
        		}
        		// Fin: Se verifica que el almacenamiento externo se encuentre disponible
    		}
    		
    		// Se avisa al usuario si se ha guardado el archivo
			if (guardaOK == false) {
				Toast.makeText(this, "ERROR! no se ha guardado el registro", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Se ha guardado el registro", Toast.LENGTH_LONG).show();
			}
			
			// Se detiene la animacion
			miAnimacion.clearAnimation();
    	}
    }
    
    
    /* Hilo o proceso en segundo plano para conectar con el dispositivo BT */
    private class conectarDispositivo extends AsyncTask<Void, String, String> {
    	
    	// Variables del proceso
        private BluetoothSocket SKtemporal;
        private String conexion;
        
		@Override
		protected void onPreExecute() {
			Log.d(TAG, "+++ conectarDispositivo.onPreExecute +++");
			
			// Se cambia el estado de la UI a conectando
	        miEstado.setText(R.string.Act_RealizarRegistro_Conectando);
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			Log.d(TAG, "+++ conectarDispositivo.doInBackground +++");
			// Se realiza el proceso en segundo plano
			
			// Se verifica si el proceso ó el hilo no haya sido cancelado
			if ( isCancelled() ){
				Log.d(TAG, "+++ conectarDispositivo.doInBackground: isCancelled +++");
				// Regresa el valor que se pasara a onPostExecute
				return conexion = "finalizada";
			} else {
				// Obtener el objeto del dispositivo BT
				BluetoothDevice dispositivo = miAdaptadorBT.getRemoteDevice(MAC_DISPOSITIVO);
		        
				// Se le pone el nombre al dispositivo
				NOMBRE_DISPOSITIVO = dispositivo.getName();
				
				// Socket temporal
				SKtemporal = null;
				
				// Obtener un Socket BT para conectar con el dispositivo BT dado
				try {
		            SKtemporal = dispositivo.createRfcommSocketToServiceRecord(miUUID);
		        } catch (IOException e) {
		            Log.e(TAG, "+++ conectarDispositivo: Creacion del Socket fallido +++", e);
		        }
		        miSocket = SKtemporal;
		        
		        if (miSocket != null) {
		        	// Cancelar el descubrimiento porque relentiza la conexión
			        miAdaptadorBT.cancelDiscovery();
			        
			        // Establecer una conexión con el socket BT
			        try {
			            // Llamada de blokeo, sólo devolverá una conexión correcta o una excepción
			            miSocket.connect();
			        }
			        catch (IOException e) {
			        	Log.e(TAG, "+++ conectarDispositivo: Conexión con el Socket fallido +++", e);
			            
			        	// Se restablece el socket temporal
			            SKtemporal = null;
			        	
			        	// Se cierra el socket
			            if (miSocket != null) {
				        	try {
				                miSocket.close();
				                miSocket = null;
				            }
				        	catch (IOException e2) {
				                Log.e(TAG, "+++ conectarDispositivo.doInBackground: cerrar miSocket ha fallado +++", e2);
				            }
						}
			        	// Regresa el valor que se pasara a onPostExecute
			            return conexion  = "fallida";
			        }
					// Regresa el valor que se pasara a onPostExecute
					return conexion = null;
		        }
		        else {
		        	// Regresa el valor que se pasara a onPostExecute
		            return conexion  = "fallida";
		        }
			}
			
		}
    	
		@Override
		protected void onProgressUpdate(String... progress) {
			Log.d(TAG, "+++ conectarDispositivo.onProgressUpdate +++");
			// Se actualiza el progreso con notificaciones o algun elemento del UI
		}
		
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "+++ conectarDispositivo.onPostExecute +++");
			// Se reporta el resultado a traves de UI update, dialogos o notificaciones etc...
			
			if (conexion == "finalizada") {
				// Se cambia el estado del UI
				miEstado.setText(R.string.Act_RealizarRegistro_NoConectado);
			} else if (conexion == "fallida" ) {
				// Se avisa que se ha fallado la conexión y se cambia el estado del UI
				Toast.makeText(Act_RealizarRegistro.this, "Conexión fallida, no se puede conectar al dispositivo", Toast.LENGTH_SHORT).show();
				miEstado.setText(R.string.Act_RealizarRegistro_NoConectado);
				
				// Se pone la bandera en verdadero
		    	BANDERA_DISPCONECTADO = true;
			} else {
				// Se avisa que se ha conectado con el dispositivo BT
				Toast.makeText(Act_RealizarRegistro.this, "Conectado a " + NOMBRE_DISPOSITIVO, Toast.LENGTH_SHORT).show();
				
				// Se cambia el estado en el UI a conectado al nombre del dispositivo
		        miEstado.setText(R.string.Act_RealizarRegistro_Conectado_A);
		    	miEstado.append(" " + NOMBRE_DISPOSITIVO);
		        
		    	// Se pone la bandera en verdadero
		    	BANDERA_DISPCONECTADO = true;
		    	
		    	// Se iniciar la lectura de datos desde el sensor
				miLeerDispositivo.execute();
				
				// Se activa el boton para realizar el registro si la duracion de registro a sido elegida
				if (BANDERA_DURREG == true) {
					iniciarRegistro.setEnabled(true);
				}
			}
			Log.d(TAG, "+++ conectarDispositivo.onPostExecute: ha terminado +++");
		}
		
		@Override
		protected void onCancelled() {
			Log.d(TAG, "+++ conectarDispositivo.onCancelled +++");
			super.onCancelled();
        	
			// Se restablece el socket temporal
            SKtemporal = null;
			
			// Se cierra el socket
			if (miSocket != null){
	        	try {
	                miSocket.close();
	                miSocket = null;
	            } catch (IOException e) {
	                Log.e(TAG, "+++ conectarDispositivo.onCancelled: cerrar miSocket ha fallado +++", e);
	            }
			}
			
			// Se restablece el objeto
            miConectarDispositivo = null;
			
			// Se avisa que se ha finalizado la conexión
			Toast.makeText(Act_RealizarRegistro.this, "Conexión finalizada", Toast.LENGTH_SHORT).show();
		}
    }
    
    
    /* Hilo o proceso en segundo plano para leer del dispositivo BT conectado */
    private class leerDispositivo extends AsyncTask<Void, byte[], String> {
    	
    	// Variables del proceso
    	private InputStream miInStream;
    	private InputStream temporalIn = null;
    	
    	private boolean ejecutandose = true;
    	private String conexion;
    	
    	private int STX = 0x02;
    	private int MSGID = 0x26;
    	private int DLC = 55;
        //private final int CRC = 0x8C;
    	private int ETX = 0x03;
        
    	private byte[] buffer = new byte[1024];
    	private int b = 0;
    	private int bufferIndex = 0;
    	private int payloadBytesRemaining;
    	
    	
		@Override
		protected void onPreExecute() {
			Log.d(TAG, "+++ leerDispositivo.onPreExecute +++");
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			Log.d(TAG, "+++ leerDispositivo.doInBackground +++");
			
			// Obtener los flujos de entrada del Socket Bluetooth
	        try {
	        	temporalIn = miSocket.getInputStream();
	        } catch (IOException e) {
	            Log.e(TAG, "+++ leerDispositivo.onPreExecute: socket temporal de entrada no creado +++", e);
	        }
	        miInStream = temporalIn;
			
	        /*
	         * El siguiente codigo es una imlementación básica de un lector específico
	         * para el dispositivo HxM. El objetivo es ilustrar la estructura de los 
	         * paquetes y la extracción de cada campo, como se indica en el documento
	         * del sensor HxM de Zephyr.
	         * Considere si su implementación debe incluir una lógica mas robusta de
	         * detección de errores para evitar que cosas como el tamaño causen 
	         * exceso de lectura, ó recalcular el CRC y lo compara con el contenido del
	         * mensaja para detectar errores de transmisión.
	         */
	        
	        // Seguir escuchando el InputStream mientras está conectado
	        while (ejecutandose) {
	        	if (isCancelled()){
	        		ejecutandose = false;
	        		return conexion = "finalizada";
	        	} else {
	        		try {
		            	bufferIndex = 0;
		            	// Leer bytes de la trama hasta que nos encontremos con el carácter de inicio de mensaje
		            	while (( b = miInStream.read()) != STX );
		            	buffer[bufferIndex++] = (byte) b;
		            	
		            	// El siguiente byte debe ser el ID de mensaje 
		            	if ((b = miInStream.read()) != MSGID )
		            		continue;
		            	buffer[bufferIndex++] = (byte) b;
		            	
		            	// El siguiente byte debe ser el tamaño esperado de la longitud del mensaje
		            	if ((b = miInStream.read()) != DLC )
		            		continue;
		            	buffer[bufferIndex++] = (byte) b;
		            	
		            	// Los siguientes bytes deben ser el data payload
		            	payloadBytesRemaining = b;
		            	
		            	while ( (payloadBytesRemaining--) > 0 ) {
		            		buffer[bufferIndex++] = (byte) (b = miInStream.read());                		                		
		            	}
		            	
		            	// El siguiente byte debe ser el CRC
		            	buffer[bufferIndex++] = (byte) (b = miInStream.read());
		            		
		            	// El siguiente byte debe ser el indicador de finalizacion del mensaje 
		            	if ((b = miInStream.read()) != ETX )
		            		continue;
		               	buffer[bufferIndex++] = (byte) b;
		               	
		                Log.d(TAG, "+++ leerDispositivos.doInBackground: lee "+Integer.toString(bufferIndex)+" bytes +++");
		                
		                // Enviar los bytes obtenidos a la actividad de la interfaz de usuario
		                //miControlador.obtainMessage(Act_inireg.MENSAJE_LEER, bufferIndex, 0, buffer).sendToTarget();
		                publishProgress(buffer);
		                
		            } catch (IOException e) {
		                Log.e(TAG, "+++ leerDispositivo.doInBackground: Desconectado +++", e);
		                // Se evita que el ciclo while continue
		                ejecutandose = false;
		                // Regresa el valor que se pasara a onPostExecute
		                return conexion = "Perdida";
		            }
	        	}
	        } // Fin del while
	        
	        Log.d(TAG, "+++ leerDispositivos: Ha terminado +++");
			return null;
		}
    	
		@Override
		protected void onProgressUpdate(byte[]... buff) {
			Log.d(TAG, "+++ leerDispositivo.onProgressUpdate +++");
			super.onProgressUpdate(buff);
			
			//byte[] readBuf = buff;
            HxM_visualizacion hrm = new HxM_visualizacion(buffer);
            hrm.calculoRR(); // Se calcular el RR y se guarda en un vector
            hrm.displayUI(); // Se visualiza el mensaje recivido
            hrm.graficarVFC(); // Se grafica la VFC en la UI
		}
		
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "+++ leerDispositivo.onPostExecute +++");
			super.onPostExecute(result);
			
			if (conexion == "finalizada"){
				// Se cambia el estado del UI
				miEstado.setText(R.string.Act_RealizarRegistro_NoConectado);
			} else if (conexion == "Perdida") {
				// Se avisa que se ha perdido la conexión
                Toast.makeText(Act_RealizarRegistro.this, "La conexión con el dispositivo de ha perdido", Toast.LENGTH_SHORT).show();
                // Se cambia el estado del UI
                miEstado.setText(R.string.Act_RealizarRegistro_NoConectado);
			}
		}
		
		@Override
		protected void onCancelled() {
			Log.d(TAG, "+++ leerDispositivo.onCancelled +++");
			super.onCancelled();
			
			// Se restablecen los objetos
			ejecutandose = false;
			miInStream = null;
	    	temporalIn = null;
			
			// Se cierra el socket
			if (miSocket != null){
	        	try {
	                miSocket.close();
	                miSocket = null;
	            } catch (IOException e2) {
	                Log.e(TAG, "+++ leerDispositivo.onCancelled: cerrar miSocket ha fallado +++", e2);
	            }
			}
			
			// Se restablece el objeto
            miLeerDispositivo = null;
			
			// Se avisa que se ha finalizado la conexión
			Toast.makeText(Act_RealizarRegistro.this, "Lectura finalizada", Toast.LENGTH_SHORT).show();
		}
    }
    
    
    /*
     * HxM_visualización
     * Esta clase contiene la informacion correspondiente a un solo mensaje
     * del monitor de frecuencia cardiaca HxM de Zephyr.
     * El constructor HxM_visualizacion llenara los campos de la trama que se leen
     * del monitor de frecuencia cardica Hxm de Zephyr, como Java no soporta
     * las variantes de numeros signed/unsigned, en algunos casos se ponen
     * los campos extraidos del mensaje del monitor en campos mas grandes
     * de lo necesario.
     */
    public class HxM_visualizacion {
    	
    	private static final String TAG = "HxM_visualización";
    	
    	public final int STX = 0x02;
        public final int MSGID = 0x26;
        public final int DLC = 55;
        public final int ETX = 0x03;
    	
    	private int serial;
        private byte stx, msgId, dlc;
        private int firmwareId, firmwareVersion, hardWareId, hardwareVersion, batteryIndicator, heartRate, heartBeatNumber;
        private long[] hbTimes = new long[15];
        private long reserved1, reserved2, reserved3, distance, speed;
        private byte strides, reserved4;
        private long reserved5;
        private byte crc, etx;
        
        
        public HxM_visualizacion (byte[] buffer) {
        	int bufferIndex = 0;
        	
        	Log.d ( TAG, "+++ Construcion a partir del búfer de bytes +++");
        	
            try {
    			stx 				= buffer[bufferIndex++];
    			msgId 				= buffer[bufferIndex++];
    			dlc 				= buffer[bufferIndex++];
    			firmwareId 			= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			firmwareVersion 	= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hardWareId 			= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hardwareVersion		= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			batteryIndicator  	= (int)(0x000000FF & (int)(buffer[bufferIndex++]));
    			heartRate  			= (int)(0x000000FF & (int)(buffer[bufferIndex++]));
    			heartBeatNumber  	= (int)(0x000000FF & (int)(buffer[bufferIndex++]));
    			hbTimes[0]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[1]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[2]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[3]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[4]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[5]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[6]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[7]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[8]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[9]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[10]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[11]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[12]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[13]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTimes[14]			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			reserved1			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			reserved2			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			reserved3			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			distance			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			speed				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			strides 			= buffer[bufferIndex++];    
    			reserved4 			= buffer[bufferIndex++];
    			reserved5 			= (long)(int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			crc 				= buffer[bufferIndex++];
    			etx 				= buffer[bufferIndex];
    		} catch (Exception e) {
    			/*
    			 * Una excepción solo deberia ocurrir si el búfer es demasiado corto y caminamos fuera del final de los bytes
    			 * por la forma en que se leen los bytes del dispositivo esto no deberia ocurrir nunca, pero por si a caso
    			 * se captura la excepción.
    			 */
    	        Log.e(TAG, "+++ Falla construyendo el búfer, probablemente se trate de un búfer incompatible o corrupto +++", e);
    		}
            Log.d(TAG, "+++ Construcción completada, consume " + bufferIndex + " bytes en el proceso +++");
            
            /*
             * Una simple comprobación para ver si podemos analizar correcatamente los bytes
             * es comprobar si el caracter ETX es encontrado donde se espera, una implementación
             * más robusta seria calcular el CRC de los contenidos del mensaje y compararlo con
             * el CRC del mensaje.  
             */
            if ( etx != ETX ){
            	Log.e(TAG,"+++ ETX no es el esperado! El mensaje HxM no se ha analizado correctamente +++");
            }
            // Enviar el contenido del mensaje al LogCat para visualizarlo
            //enviarLogCat();
        }
        
        /*
         * Calculo del RR: Inicio
         */
        private void calculoRR() {
        	// Calcular el numero de latidos nuevos en el paquete de datos
        	if (BANDERA_PRIMERPAQUETERECIBIDO == true) {
        		latidosnuevos = 1;
            	latidosviejos = heartBeatNumber;
            	BANDERA_PRIMERPAQUETERECIBIDO = false;
        	}
        	else{
        		latidosnuevos = heartBeatNumber - latidosviejos;
            	latidosviejos = heartBeatNumber;
            	if (latidosnuevos < 0) {
            		latidosnuevos = latidosnuevos + 256;
            	}
        	}
        	// Calcular el tiempo entre los nuevos latidos detectados
        	int i = latidosnuevos;
        	if (i >= hbTimes.length) {
        		i = hbTimes.length - 1;
        	}
        	while (i > 0) {
        		// Calcular tiempo entre latidos
        		TRR = hbTimes[i-1] - hbTimes[i];
            	Log.i(TAG, "+++ TiempoR1: " + hbTimes[i-1] + " - TiempoR2: " + hbTimes[i] + " +++");
            	// Poner tiempo detectado en en el vector RR
            	if (TRR < 0) {
            		TRR = TRR + 65535;
            		RR.addElement(TRR);
            	} else {
            		RR.addElement(TRR);
            	}
        		// Incrementar variable de latidos detectados
        		numerolatido = numerolatido + 1;
        		// Decrementar variable
        		i--;
        	}
        }// Calculo del RR: Fin
        
        /*
         * Grafica del RR: Inicio
         */
        private void graficarVFC() {
        	if (RR.isEmpty()) {
        		// No se grafica nada
        	} else {
        		if (RR.size() < 2) {
                    VFCgrafica.setStartY(RR.firstElement());
            	} else {
            		int repeticiones = latidosnuevos;
                	if (repeticiones >= hbTimes.length) {
                		repeticiones = hbTimes.length - 1;
                	}
                	while (repeticiones > 0) {
                		VFCgrafica.setStartY(RR.elementAt(RR.lastIndexOf(RR.lastElement()) - repeticiones));
                        VFCgrafica.setStopY(RR.elementAt(RR.lastIndexOf(RR.lastElement()) - (repeticiones-1)));
                        // Decrementar variable
                        repeticiones--;
                	}
            	}
                VFCgrafica.invalidate();
        	}
        	
        }// Grafica del RR: Fin
        
        /*
         * Muestra la lectura del monitor en la UI de la actividad
         */
        private void displayUI() {
        	Log.d(TAG, "+++ Ha entrado al metodo displayUI +++");
        	
        	display (R.id.ly_RealizarRegistro_Bateria_Valor, (int)batteryIndicator);
        	display (R.id.ly_RealizarRegistro_FC_Valor, (int)heartRate);
        	display (R.id.ly_RealizarRegistro_Latidos_Valor, (int)numerolatido);
        	display (R.id.ly_RealizarRegistro_TRR_Valor, (long) TRR);
        }
        
        /*
         * Funciones para el control del formato de los campos
         * HxM en la UI de la actividad.
         */
        // Muestra un valor de byte
    	@SuppressWarnings("unused")
		private void display  ( int nField, byte d ) {
    		
    		String INT_FORMAT = "%x";
    		
    		String s = String.format(INT_FORMAT, d);
    		
    		display( nField, s  );
    	}
    	
    	// Muestra un valor entero
    	private void display ( int nField, int d ) {
    		
    		String INT_FORMAT = "%d";
    		
    		String s = String.format(INT_FORMAT, d);
    		
    		display( nField, s  );
    	}
    	
    	// Muestra un valor long
    	private void display ( int nField, long d ) {   
    		
    		String INT_FORMAT = "%d";
    		
    		String s = String.format(INT_FORMAT, d);
    		
    		display( nField, s  );
    	}
    	
    	// Muestra un string de caracteres
    	private void display ( int nField, CharSequence  str  ) {
    		
        	TextView tvw = (TextView) findViewById(nField);
        	if ( tvw != null ){
        		tvw.setText(str);
        	}
        }
    	
    	/* 
    	 * Envia el mensaje al logcat para visualizarlo
    	 */
        @SuppressWarnings("unused")
		private void enviarLogCat(){
        	Log.d(TAG,"+++ EnviarLogCat +++");
    		Log.d(TAG,"...serial "+ ( serial ));
    		Log.d(TAG,"...stx "+ ( stx ));
    		Log.d(TAG,"...msgId "+( msgId ));
    		Log.d(TAG,"...dlc "+ ( dlc ));
    		Log.d(TAG,"...firmwareId "+ ( firmwareId ));
    		Log.d(TAG,"...sfirmwareVersiontx "+ (  firmwareVersion ));
    		Log.d(TAG,"...hardWareId "+ (  hardWareId ));
    		Log.d(TAG,"...hardwareVersion "+ (  hardwareVersion ));
    		Log.d(TAG,"...batteryIndicator "+ ( batteryIndicator ));
    		Log.d(TAG,"...heartRate "+ ( heartRate ));
    		Log.d(TAG,"...heartBeatNumber "+ ( heartBeatNumber ));
    		Log.d(TAG,"...hbTime1 "+ (  hbTimes[0] ));
    		Log.d(TAG,"...hbTime2 "+ (  hbTimes[1] ));
    		Log.d(TAG,"...hbTime3 "+ (  hbTimes[2] ));
    		Log.d(TAG,"...hbTime4 "+ (  hbTimes[3] ));
    		Log.d(TAG,"...hbTime4 "+ (  hbTimes[4] ));
    		Log.d(TAG,"...hbTime6 "+ (  hbTimes[5] ));
    		Log.d(TAG,"...hbTime7 "+ (  hbTimes[6] ));
    		Log.d(TAG,"...hbTime8 "+ (  hbTimes[7] ));
    		Log.d(TAG,"...hbTime9 "+ (  hbTimes[8] ));
    		Log.d(TAG,"...hbTime10 "+ (  hbTimes[9] ));
    		Log.d(TAG,"...hbTime11 "+ (  hbTimes[10] ));
    		Log.d(TAG,"...hbTime12 "+ (  hbTimes[11] ));
    		Log.d(TAG,"...hbTime13 "+ (  hbTimes[12] ));
    		Log.d(TAG,"...hbTime14 "+ (  hbTimes[13] ));
    		Log.d(TAG,"...hbTime15 "+ (  hbTimes[14] ));
    		Log.d(TAG,"...reserved1 "+ (  reserved1 ));
    		Log.d(TAG,"...reserved2 "+ (  reserved2 ));
    		Log.d(TAG,"...reserved3 "+ (  reserved3 ));
    		Log.d(TAG,"...distance "+ (  distance ));
    		Log.d(TAG,"...speed "+ (  speed ));
    		Log.d(TAG,"...strides "+ ( strides ));
    		Log.d(TAG,"...reserved4 "+ ( reserved4 ));
    		Log.d(TAG,"...reserved5 "+ ( reserved5 ));
    		Log.d(TAG,"...crc "+ ( crc ));
    		Log.d(TAG,"...etx "+ ( etx ));
        }
    }
    
    
}/* Fin Actividad */