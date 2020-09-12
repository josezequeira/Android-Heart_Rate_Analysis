package pfe.savfc;

/* Librerias importadas */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/* Inicio Actividad Principal */
public class Act_VisualizarRegistro extends Activity {
	
	/* Inicio Declaración de variables de la actividad */
	
	// Depuración
	private static final String TAG = "Act_visualizarreg";
	
	// Elementos del layout
	private static VisualizarReg_View graficarReg;
	private static TextView id;
	private static TextView sexo;
	private static TextView edad;
	private static TextView peso;
	private static TextView fechayhora;
	private static TextView duracion;
	private static TextView actprevia;
	private static TextView respxmin;
	
	// Miembros
	private static Intent intento;								// Intento para leer los extras que indican el directorio del reg
	private static String[] registroDir;						// Directorio del registro a leer
	private static Vector<Double> RR = new Vector<Double>();	// Vector donde ponen los RR leidos
	
	/* Fin Declaración de variables de la actividad */
	
	
	/*- Inicio Metodos para el ciclo de vida de la actividad -*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "+++ ON CREATE +++");
		
		// Se activan las banderas para mantener la pantalla encendida
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Se inicializa el layout
		setContentView(R.layout.ly_visualizarregistro);
		
		// Se inicializan los elementos del layout
		graficarReg = (VisualizarReg_View) findViewById(R.id.VisualizarReg);
		id = (TextView) findViewById(R.id.ly_ProcesarRegistro_DatosUsuario_ID_Valor);
		sexo = (TextView) findViewById(R.id.ly_ProcesarRegistro_DatosUsuario_Sexo_Valor);
		edad = (TextView) findViewById(R.id.ly_ProcesarRegistro_DatosUsuario_Edad_Valor);
		peso = (TextView) findViewById(R.id.ly_ProcesarRegistro_DatosUsuario_Peso_Valor);
		fechayhora = (TextView) findViewById(R.id.ly_ProcesarRegistro_FechaHora_Valor);
		duracion = (TextView) findViewById(R.id.ly_ProcesarRegistro_DuracionReg_Valor);
		actprevia = (TextView) findViewById(R.id.ly_ProcesarRegistro_ActividadPreviaReg_Valor);
		respxmin = (TextView) findViewById(R.id.ly_ProcesarRegistro_RespxMin_Valor);
		
		// Se obtienen el nombre y el directorio del registro a visualizar
		intento = getIntent();
		registroDir = intento.getStringArrayExtra("regDir");
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "+++ ON START +++");
		
		// Se lee el registro segun su procedencia
		if (registroDir[0].equals("memTEL")) {
			leerRegTEL(registroDir[1]);
		} else if (registroDir[0].equals("memTSD")) {
			leerRegTSD(registroDir[1]);
		} else {
			Toast.makeText(this, R.string.Toast_No_Identificado_Registro, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	@Override
	protected synchronized void onResume() {
		super.onResume();
		Log.d(TAG, "+++ ON RESUME +++");
		
		// Se grafica el registro
		graficarReg.setRR(RR);
		graficarReg.invalidate();
		
	}
	
	@Override
	protected synchronized void onPause() {
		super.onPause();
		Log.d(TAG, "+++ ON PAUSE +++");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "+++ ON STOP +++");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "+++ ON DESTROY +++");
		
		// Vaciar vectores
		RR.removeAllElements();
	}
	
	/*- Fin Metodos para el ciclo de vida de la actividad -*/
	
	
	/* Metodo para leer el registro de la memoria del telefono */
	public void leerRegTEL(String nombreReg) {
		
		// Se declara en FileInputStram para obtener los bytes del sistema de archivos
		FileInputStream fis = null;
		
		try {
			// Archivo de la memoria del telefono que se quiere leer
			fis = openFileInput(nombreReg);
			
			// Byte array de tamaño estimado del archivo donde se van a guardar los bytes que se lean
			byte[] reader = new byte[fis.available()];
			
			// Se leen los bytes del archivo y se guardan en el byte array reader
			while (fis.read(reader) != -1) {
			}
			
			// Se obtienen solo los strings de tiempo entre latido y latido (RR)
			String Str = new String(reader); // Se convierten los bytes leidos en strings
			
			// Se obtienen los datos del usuario
			int iniID = Str.indexOf("=");
			int finID = Str.indexOf("\n", iniID);
			String ID = Str.substring(iniID + 1, finID);
			id.setText(ID);
			
			int iniFechaHora = Str.indexOf("=", finID);
			int finFechaHora = Str.indexOf("\n", iniFechaHora);
			String FechaHora = Str.substring(iniFechaHora + 1, finFechaHora);
			fechayhora.setText(FechaHora);
			
			int iniNombre = Str.indexOf("=", finFechaHora);
			int finNombre = Str.indexOf("\n", iniNombre);
			
			int iniApellido = Str.indexOf("=", finNombre);
			int finApellido = Str.indexOf("\n", iniApellido);
			
			int iniEdad = Str.indexOf("=", finApellido);
			int finEdad = Str.indexOf("\n", iniEdad);
			String Edad = Str.substring(iniEdad + 1, finEdad);
			edad.setText(Edad);
			
			int iniSexo = Str.indexOf("=", finEdad);
			int finSexo = Str.indexOf("\n", iniSexo);
			String Sexo = Str.substring(iniSexo + 1, finSexo);
			sexo.setText(Sexo);
			
			int iniPeso = Str.indexOf("=", finSexo);
			int finPeso = Str.indexOf("\n", iniPeso);
			String Peso = Str.substring(iniPeso + 1, finPeso);
			peso.setText(Peso);
			
			int iniActividad = Str.indexOf("=", finPeso);
			int finActividad = Str.indexOf("\n", iniActividad);
			String Actividad = Str.substring(iniActividad + 1, finActividad);
			actprevia.setText(Actividad);
			
			int iniDuracion = Str.indexOf("=", finActividad);
			int finDuracion = Str.indexOf("\n", iniDuracion);
			String Duracion = Str.substring(iniDuracion + 1, finDuracion);
			duracion.setText(Duracion);
			
			int iniRespMin = Str.indexOf("=", finDuracion);
			int finRespMin = Str.indexOf("\n", iniRespMin);
			String RespMin = Str.substring(iniRespMin + 1, finRespMin);
			respxmin.setText(RespMin);
			
			// Se busca la posicion donde se ecuentre este símbolo, que es donde inician los RR
			int pos = Str.indexOf("[");
			// Se obtienen los RR entre los []
			String subStr = Str.substring(pos+1, Str.length()-1);
			// Se eliminan todos los espacios y quedan los RR separados por comas
			String strVFC = subStr.replaceAll(" ","").trim();
			
			// Se convierte cada RR en numero y se guardan en un vector
			StringBuffer TRR = new StringBuffer(); // Buffer de strings
			double tmp; // variable temporal
			
			// Ciclo que realiza la conversion de cada RR en numero y lo guarda en una posicion del vector RR
			for (int i=0 ; i < strVFC.length()-1 ; i++) {
				if ( strVFC.substring(i,i+1).equals(",") ) {
					tmp = Long.parseLong(TRR.toString());
					RR.addElement(tmp);
					TRR.setLength(0);
				} else {
					TRR.append(strVFC.substring(i,i+1));
				}
			}
			
			// Se ajunsta el vector al numero de elementos que tiene
			RR.trimToSize();
			
		} catch (IOException e) {
			Log.e("+++ leerRegTEL +++", e.getMessage(), e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					Log.e("+++ leerRegTEL: fis.close +++", e.getMessage(), e);
				}
			}
		}
		
	}
	
	
	/* Metodo para leer el registro de la tarjeta SD*/
	public void leerRegTSD(String dirReg) {
		
		File rFile = new File(dirReg);
		if (rFile.exists() && rFile.canRead()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(rFile);
				byte[] reader = new byte[fis.available()];
				while (fis.read(reader) != -1) {
				}
				
				String Str = new String(reader);
				
				int iniID = Str.indexOf("=");
				int finID = Str.indexOf("\n", iniID);
				String ID = Str.substring(iniID + 1, finID);
				id.setText(ID);
				
				int iniFechaHora = Str.indexOf("=", finID);
				int finFechaHora = Str.indexOf("\n", iniFechaHora);
				String FechaHora = Str.substring(iniFechaHora + 1, finFechaHora);
				fechayhora.setText(FechaHora);
				
				int iniNombre = Str.indexOf("=", finFechaHora);
				int finNombre = Str.indexOf("\n", iniNombre);
				
				int iniApellido = Str.indexOf("=", finNombre);
				int finApellido = Str.indexOf("\n", iniApellido);
				
				int iniEdad = Str.indexOf("=", finApellido);
				int finEdad = Str.indexOf("\n", iniEdad);
				String Edad = Str.substring(iniEdad + 1, finEdad);
				edad.setText(Edad);
				
				int iniSexo = Str.indexOf("=", finEdad);
				int finSexo = Str.indexOf("\n", iniSexo);
				String Sexo = Str.substring(iniSexo + 1, finSexo);
				sexo.setText(Sexo);
				
				int iniPeso = Str.indexOf("=", finSexo);
				int finPeso = Str.indexOf("\n", iniPeso);
				String Peso = Str.substring(iniPeso + 1, finPeso);
				peso.setText(Peso);
				
				int iniActividad = Str.indexOf("=", finPeso);
				int finActividad = Str.indexOf("\n", iniActividad);
				String Actividad = Str.substring(iniActividad + 1, finActividad);
				actprevia.setText(Actividad);
				
				int iniDuracion = Str.indexOf("=", finActividad);
				int finDuracion = Str.indexOf("\n", iniDuracion);
				String Duracion = Str.substring(iniDuracion + 1, finDuracion);
				duracion.setText(Duracion);
				
				int iniRespMin = Str.indexOf("=", finDuracion);
				int finRespMin = Str.indexOf("\n", iniRespMin);
				String RespMin = Str.substring(iniRespMin + 1, finRespMin);
				respxmin.setText(RespMin);
				
				int pos = Str.indexOf("[");
				String subStr = Str.substring(pos+1, Str.length()-1);
				String strVFC = subStr.replaceAll(" ","").trim();
				
				StringBuffer TRR = new StringBuffer();
				double tmp;
				
				for (int i=0 ; i < strVFC.length()-1 ; i++) {
					if ( strVFC.substring(i,i+1).equals(",") ) {
						tmp = Long.parseLong(TRR.toString());
						RR.addElement(tmp);
						TRR.setLength(0);
					} else {
						TRR.append(strVFC.substring(i,i+1));
					}
				}
				RR.trimToSize();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						Log.e("+++ leerRegTSD: fis.close +++", e.getMessage(), e);
					}
				}
			}
		} else {
			id.setText(R.string.No_se_puede_leer_RegistroTSD);
		}
	}
	
}/* Fin Actividad Principal */