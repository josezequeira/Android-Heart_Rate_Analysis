package pfe.savfc;

/* Librerias importadas */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/* Inicio Actividad Principal */
public class Act_ProcesarReg extends Activity {
	
	/* Inicio Declaración de variables de la actividad */
	
	// Depuración
	private static final String TAG = "Act_ProcesarReg";
	
	// Elementos del layout
	private static TextView id;
	private static TextView sexo;
	private static TextView edad;
	private static TextView peso;
	private static TextView fechayhora;
	private static TextView duracion;
	private static TextView actprevia;
	private static TextView respxmin;
	//private static TextView velMuestreo;
	private static TextView numlatidos;
	private static TextView rrmax;
	private static TextView rrmin;
	private static TextView rrprom;
	private static TextView fcprom;
	private static TextView sdrr;
	private static TextView rmssd;
	private static TextView pnn50;
	private static TextView vlf;
	private static TextView lf;
	private static TextView hf;
	private static TextView lfhf;
	
	// Miembros
	private static Intent intento;
	private static String[] registroDir;
	private static int duracionReg;											// Duración del registro
	private static double fsCalculada;										// Frecuendia de RRprom en Hz (Fs)
	//private static final double fsSensor = 1000;							// Frecuendia del sensor HxM en Hz (en tiempo es 1ms)
	private static final double iniLF = 0.04;								// Frecuencia donde inicia la baja frecuencia (LF)
	private static final double iniHF = 0.15;								// Frecuencia donde inicia la alta frecuencia (HF)
	private static final double finHF = 0.4;								// Frecuencia donde finaliza la alta frecuencia (HF)
	private static Vector<Double> RR = new Vector<Double>();				// Vector donde ponen los RR leidos
	private static double[] RRsinArtefacto;									// Vector donde ponen los RR leidos
	private static double[] RRsinDC;										// Vector de la señal RR sin DC
	private static Vector<Double> potFFT = new Vector<Double>();			// Vector de potencia calculado despues de la FFT
	private static double[] complejo;										// Array de valores complejos
	private static int nfft;												// Número de puntos de la FFT
	private static double FCvent;											// Factor de correcion de la ventana
	
	// Miembros para obtener las preferencias
    private static SharedPreferences miPref;
    private static String ARTEFACTO_UMBRAL = null;
    private static String VENTANA = null;
    private static String GUARDAR_ARC_PROCESADOS = null;
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
		setContentView(R.layout.ly_procesarreg);
		
		// Se obtienen el nombre y el directorio del registro a procesar
		intento = getIntent();
		registroDir = intento.getStringArrayExtra("regDir");
		
		// Se inicializan los elementos del layout
		id = (TextView) findViewById(R.id.ly_ProcesarRegistro_DatosUsuario_ID_Valor);
		sexo = (TextView) findViewById(R.id.ly_ProcesarRegistro_DatosUsuario_Sexo_Valor);
		edad = (TextView) findViewById(R.id.ly_ProcesarRegistro_DatosUsuario_Edad_Valor);
		peso = (TextView) findViewById(R.id.ly_ProcesarRegistro_DatosUsuario_Peso_Valor);
		fechayhora = (TextView) findViewById(R.id.ly_ProcesarRegistro_FechaHora_Valor);
		duracion = (TextView) findViewById(R.id.ly_ProcesarRegistro_DuracionReg_Valor);
		actprevia = (TextView) findViewById(R.id.ly_ProcesarRegistro_ActividadPreviaReg_Valor);
		respxmin = (TextView) findViewById(R.id.ly_ProcesarRegistro_RespxMin_Valor);
		//velMuestreo = (TextView) findViewById(R.id.ly_ProcesarRegistro_VelocidadMuestreo_Valor);
		numlatidos = (TextView) findViewById(R.id.ly_ProcesarRegistro_NumeroLatidos_Valor);
		rrmax = (TextView) findViewById(R.id.ly_ProcesarRegistro_RRmaximo_Valor);
		rrmin = (TextView) findViewById(R.id.ly_ProcesarRegistro_RRminimo_Valor);
		rrprom = (TextView) findViewById(R.id.ly_ProcesarRegistro_RRpromedio_Valor);
		fcprom = (TextView) findViewById(R.id.ly_ProcesarRegistro_FCpromedio_Valor);
		sdrr = (TextView) findViewById(R.id.ly_ProcesarRegistro_SDRR_Valor);
		rmssd = (TextView) findViewById(R.id.ly_ProcesarRegistro_RMSSD_Valor);
		pnn50 = (TextView) findViewById(R.id.ly_ProcesarRegistro_pNN50_Valor);
		vlf = (TextView) findViewById(R.id.ly_ProcesarRegistro_VLF_Valor);
		lf = (TextView) findViewById(R.id.ly_ProcesarRegistro_LF_Valor);
		hf = (TextView) findViewById(R.id.ly_ProcesarRegistro_HF_Valor);
		lfhf = (TextView) findViewById(R.id.ly_ProcesarRegistro_LFHF_Valor);
		
		// Obtener preferencias
		obtenerPreferencias();
		
		// Se inicializan las variables
		duracionReg = 0;
		fsCalculada = 0.0;
		nfft = 0;
		FCvent = 0.0;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "+++ ON START +++");
		
		// Se lee el registro segun su procedencia
		if (registroDir[0].equals("memTEL")) {
			RR = leerRegTEL(registroDir[1]);
		} else if (registroDir[0].equals("memTSD")) {
			RR = leerRegTSD(registroDir[1]);
		} else {
			Toast.makeText(this, R.string.Toast_No_Identificado_Registro, Toast.LENGTH_SHORT).show();
		}
		
		// Se eliminan artefactos
		RRsinArtefacto = eliminarArtefactos(RR);
		
		// Se realiza el procesamiento en el tiempo (Simple y Estadístico)
		procTiempo(RRsinArtefacto);
		
		// Se elimina el DC o la linea de tendencia (detrend), de la señal RR
		RRsinDC = eliminarDC(RRsinArtefacto);
		
		// Se elige la ventana a aplicar segun las preferencias
		int vent;
		if (VENTANA != null) {
			if (VENTANA.equals("Bartlett")) {
				vent = 1;
			}
			else if (VENTANA.equals("Blackman")) {
				vent = 2;
			}
			else if (VENTANA.equals("Hamming")) {
				vent = 3;
			}
			else if (VENTANA.equals("Hanning")) {
				vent = 4;
			}
			else {
				// Rectangular
				vent = 0;
			}
		}
		else {
			vent = 4;
		}
		
		// Se acondiciona la señal para que se le pueda aplicar la FFT
		complejo = preDfft(RRsinDC, vent);
		
		// Se realiza la FFT directa de la señal
		complejo = fft(complejo, nfft, 1);
		
		// Se calculan la densidad espectral de potencia (PSD) de un solo lado de la FFT Directa
		potFFT = magfaspotDfft(complejo, nfft, RRsinDC.length, fsCalculada, 5);
		
		// Se realiza el procesamiento en la frecuencia
		procFrec(potFFT, nfft, fsCalculada,iniLF, iniHF, finHF);
		
		//+++++++++++ Se guardan los archivos +++++++++++
		boolean flag;
		if (GUARDAR_ARC_PROCESADOS != null) {
			if (GUARDAR_ARC_PROCESADOS.equals("Activado")) {
				flag = true;
			}
			else {
				flag = false;
			}
		}
		else {
			flag = false;
		}
		if (flag == true) {
			guardarArchivo(RRsinArtefacto, "Señal" + "_SinArtefacto_" + ARTEFACTO_UMBRAL);
			guardarArchivo(RRsinDC, "Señal" + "_SinDC");
			guardarArchivo2(potFFT, "Señal" + "_DEP_" + VENTANA);
		}
	}
	
	@Override
	protected synchronized void onResume() {
		super.onResume();
		Log.d(TAG, "+++ ON RESUME +++");
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
		potFFT.removeAllElements();
		
	}
	/*- Fin Metodos para el ciclo de vida de la actividad -*/
	
	
	/* Metodo para leer el registro de la memoria del telefono */
	private Vector<Double> leerRegTEL(String nombreReg) {
		// Variables
		Vector<Double> vectorRR = new Vector<Double>();
		
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
			
			// Se guarda la duracion en una variable global
			duracionReg = Integer.parseInt(Duracion.substring(0, Duracion.indexOf(" ")));
			// Se busca la posicion donde se ecuentre este símbolo, que es donde inician los RR
			int pos = Str.indexOf("[");
			// Se obtienen la cadena entre "[]"
			String subStr = Str.substring(pos+1, Str.length()-1);
			// Se eliminan todos los espacios y quedan los RR separados por comas
			String strVFC = subStr.replaceAll(" ","").trim();
			// Se crea un arreglo de cadenas que contiene las cadenas separadas por ","
			String[] intervalosRR = strVFC.split(",");
			double tmp;
			// Se convierten las cadenas de caracteres a numeros y se guardan en el vector RR
			for (int i=0; i<intervalosRR.length; i++) {
				tmp = Long.parseLong(intervalosRR[i]);
				vectorRR.addElement(tmp);
			}
			// Se ajunsta el vector al numero de elementos que tiene
			vectorRR.trimToSize();
			
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
		
		// Regresa la señal RR
		return vectorRR;
	}
	
	
	/* Metodo para leer el registro de la tarjeta SD*/
	private Vector<Double> leerRegTSD(String dirReg) {
		// Variables
		Vector<Double> vectorRR = new Vector<Double>();
		
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
				
				// Se guarda la duracion en una variable global
				duracionReg = Integer.parseInt(Duracion.substring(0, Duracion.indexOf(" ")));
				
				int pos = Str.indexOf("[");
				String subStr = Str.substring(pos+1, Str.length()-1);
				String strVFC = subStr.replaceAll(" ","").trim();
				
				String[] intervalosRR = strVFC.split(",");
				
				double tmp;
				for (int i=0; i<intervalosRR.length; i++) {
					tmp = Long.parseLong(intervalosRR[i]);
					vectorRR.addElement(tmp);
				}
				
				vectorRR.trimToSize();
			}
			catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			finally {
				if (fis != null) {
					try {
						fis.close();
					}
					catch (IOException e) {
						Log.e("+++ leerRegTSD: fis.close +++", e.getMessage(), e);
					}
				}
			}
		}
		else {
			id.setText(R.string.No_se_puede_leer_RegistroTSD);
		}
		
		// Regresa la señal RR
		return vectorRR;
	}
	
	
	/* Obtener las preferencias relevantes para el procesado */
    private boolean obtenerPreferencias() {
    	Log.d(TAG, "+++ obtenerPreferencias +++");
    	
    	// Se inicializan las preferencias
    	miPref = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	// Se obtienen los valores de las preferencias
    	ARTEFACTO_UMBRAL = miPref.getString("Artefacto_Umbral", null);
    	VENTANA = miPref.getString("Ventana", null);
    	GUARDAR_ARC_PROCESADOS = miPref.getString("GA_Bandera", null);
    	
    	// Regresar verdadero si se obtuvo la preferencia
    	if ((ARTEFACTO_UMBRAL != null) && (VENTANA != null) && (GUARDAR_ARC_PROCESADOS != null)) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
	
	
	/* Eliminar artefactos de la señal */
	private double[] eliminarArtefactos(Vector<Double> RR) {
		
		// Variables
		double[] RRsinArtefactos = new double[RR.size()];
		double umbral, temp;
		boolean flag;
		
		// Inicializar array RRsinArtefactos
		for (int i=0; i<RRsinArtefactos.length; i++) {
			RRsinArtefactos[i] = RR.elementAt(i); 
		}
		
		// Obtener el umbral de las preferencias
		if (ARTEFACTO_UMBRAL != null) {
			if (ARTEFACTO_UMBRAL.equals("Sin deteccion de artefactos")) {
				umbral = 0;
				flag = false;
			}
			else {
				umbral = Integer.valueOf(ARTEFACTO_UMBRAL);
				flag = true;
			}
		}
		else {
			umbral = 0;
			flag = false;
		}
		
		
		if (flag == true) {
			// Se inicializa el vector
			RRsinArtefactos[0] = RR.elementAt(0);
			
			// Se eliminan las variaciones por encima del umbral
			for (int i=1; i<RRsinArtefactos.length; i++) {
				temp = Math.abs(RRsinArtefactos[i-1] - RR.elementAt(i));
				if (temp > umbral) {
					RRsinArtefactos[i] = RRsinArtefactos[i-1];
				}
				else {
					RRsinArtefactos[i] = RR.elementAt(i);
				}
			}
			
			// Se regresa la señal sin artefactos
			return RRsinArtefactos;
		}
		else {
			// Se regresa la señal sin modificar
			return RRsinArtefactos;
		}
	}
	
	
	/* Eliminar nivel del DC de la señal */
	private double[] eliminarDC(double[] RR) {
		
		// Variables
		double[] sinDC = new double[RR.length];
		double NUMLATIDOS = 0.0;
		double SUMA = 0.0;
		double PROMEDIO = 0.0;
		
		// Se calcula el NUMERO DE LATIDOS
		NUMLATIDOS = RR.length;
		
		// Se calcula la SUMA
		for (int i=0; i < NUMLATIDOS; i++ ) {
			// Suma
			SUMA = SUMA + RR[i];
		}
		
		// Se calcula el PROMEDIO
		if (NUMLATIDOS <= 0) {
			// Error no se puede dividir por 0
		} else {
			PROMEDIO = SUMA/NUMLATIDOS;
		}
		
		// Se elimina el nivel de DC
		if (PROMEDIO != 0) {
			for (int i=0; i < NUMLATIDOS; i++ ) {
				sinDC[i] = RR[i] - PROMEDIO;
			}
		}
		
		// Se regresa la señal sin DC
		return sinDC;
	}
	
	
	/* Metodo para saber si el numero es par o impar */ 
	private boolean esImpar(int Num) {
		if ( (Num % 2) != 0 ) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/* Procesamiento en el Tiempo */
	private void procTiempo(double[] RR) {
		// Variable para mostrar el resultado en pantalla
		String temp;
		
		// Variables Simples
		double NUMLATIDOS = 0.0;
		double MAX = 0.0;
		double MIN = 0.0;
		double SUMA = 0.0;
		double PROMEDIO = 0.0;
		double PROM_EN_SEG = 0.0;
		double FCPROMEDIO = 0.0;
		
		// Variables Estadísticas
		double PREVARIANZA = 0.0;
		double VARIANZA = 0.0;
		double SDRR = 0.0;
		double PRERMSSD = 0.0;
		double NUMDIF = 0.0;
		double PRERMSSD2 = 0.0;
		double RMSSD = 0.0;
		double NN50 = 0.0;
		double PNN50 = 0.0;
		
		// Se calcula el NUMERO DE LATIDOS
		NUMLATIDOS = RR.length;
		
		// Se calcula el MAXIMO, MINIMO y Suma
		MAX=RR[0];
		MIN=RR[0];
		
		for (int i=0; i < NUMLATIDOS; i++ ) {
			// Máximo
			if (RR[i] > MAX) {
				MAX = RR[i];
			}
			
			// Mínimo
			if (RR[i] < MIN) {
				MIN = RR[i];
			}
			
			// Suma
			SUMA = SUMA + RR[i];
		}
		
		// Se calcula el PROMEDIO
		if (NUMLATIDOS <= 0) {
			// Error no se puede dividir por 0
		} else {
			PROMEDIO = SUMA/NUMLATIDOS;
		}
		
		// Se calcula la Frecuencia promedio de los RR
		PROM_EN_SEG = PROMEDIO/1000.0;
		
		if (PROM_EN_SEG <= 0) {
			// Error no se puede dividir por 0
		} else {
			fsCalculada = 1.0/PROM_EN_SEG;
		}
		
		// Se calcula FRECUENCIA CARDIACA PROMEDIO
		if (duracionReg <= 0 ) {
			// Error no se puede dividir por 0
		} else {
			FCPROMEDIO = NUMLATIDOS/duracionReg;
		}
		
		// Se calcula la SDRR
		for (int i=0; i < NUMLATIDOS; i++ ) {
			PREVARIANZA = PREVARIANZA + Math.pow((RR[i] - PROMEDIO), 2);
		}
		if (NUMLATIDOS <= 0) {
			// Error no se puede dividir por 0
		} else {
			VARIANZA = PREVARIANZA/(NUMLATIDOS - 1.0);
		}
		SDRR = Math.sqrt(VARIANZA);
		
		
		// Se calcula el RMSSD y el PNN50
		for (int i=0; i < NUMLATIDOS-1 ; i++ ) {
			PRERMSSD = PRERMSSD + Math.pow((RR[i+1] - RR[i]), 2);
			NUMDIF = NUMDIF + 1.0;
			if ( Math.abs(RR[i+1] - RR[i]) > 50 ) {
				NN50 = NN50 + 1.0;
			}
		}
		if (NUMDIF <= 0) {
			// Error no se puede dividir por 0
		} else {
			PRERMSSD2 = PRERMSSD/NUMDIF;
			PNN50 = (NN50 * 100.0)/NUMDIF;
		}
		RMSSD = Math.sqrt(PRERMSSD2);
		
		// Se muestran los valores en pantalla
		temp = num2string((1.0/fsCalculada)*1000.0);
		//velMuestreo.setText(String.valueOf(temp + " ms"));
		//velMuestreo.setText(String.valueOf((1.0/fsSensor)*1000.0) + " ms");
		
		temp = num2string(NUMLATIDOS);
		numlatidos.setText(temp);
		
		temp = num2string(MAX);
		rrmax.setText(temp);
		
		temp = num2string(MIN);
		rrmin.setText(temp);
		
		temp = num2string(PROMEDIO);
		rrprom.setText(temp);
		
		temp = num2string(FCPROMEDIO);
		fcprom.setText(temp);
		
		temp = num2string(SDRR);
		sdrr.setText(temp);
		
		temp = num2string(RMSSD);
		rmssd.setText(temp);
		
		temp = num2string(PNN50);
		pnn50.setText(temp);
	}
	
	
	/* Se adecua la señal para aplicar la FFT
	 * Se realiza zero paddin si la señal no es potencia de 2,
	 * se enventana la señal con las siguientes opciones:
	 * 1 para Bartlett
	 * 2 para Blackman
	 * 3 para Hamming
	 * 4 para Hanning
	 * Y se obtiene el array de numeros complejos a transformar.
	 * El array queda de la siguiente forma: 
	 * data=[RR,0,RR,0...nn] para aplicar la FFT.
	 */
	private double[] preDfft(double[] RR, int vent) {
		
		// Variables
		int sigpot2, ini, cont;
		double complejo[], RRceropadding[], len, pot, ventana = 0.0, temp = 0.0, temp2 = 0.0;
		double sumaFCvent = 0.0;
		
		// Tamaño del vector de datos
		len = RR.length;
		
		// Se aplica la ventana elegida
		if (vent == 1) {
			// Bartlett(Triangular)
			temp2 = 0.5*(len-1);
			for (int i=0; i<len ; i++) {
				ventana = 1 - Math.abs((i-temp2)/temp2);
				temp = RR[i] * ventana;
				RR[i] = temp;
				sumaFCvent = sumaFCvent + Math.pow(ventana, 2);
			}
			// Calcular factor de correccion de la ventana
			if (sumaFCvent > 0) {
				FCvent = len/sumaFCvent;
			}
		}
		else if (vent == 2) {
			// Blackman
			temp2 = 2*(Math.PI)/(len-1);
			for (int i=0; i<len ; i++) {
				ventana = 0.42 - 0.5*Math.cos(temp2*i) + 0.08*Math.cos(2*temp2*i);
				temp = RR[i] * ventana;
				RR[i] = temp;
				sumaFCvent = sumaFCvent + Math.pow(ventana, 2);
			}
			// Calcular factor de correccion de la ventana
			if (sumaFCvent > 0) {
				FCvent = len/sumaFCvent;
			}
		}
		else if (vent == 3) {
			// Hamming
			temp2 = 2*(Math.PI)/(len-1);
			for (int i=0; i<len ; i++) {
				ventana = 0.54 - 0.46*Math.cos(temp2*i);
				temp = RR[i] * ventana;
				RR[i] = temp;
				sumaFCvent = sumaFCvent + Math.pow(ventana, 2);
			}
			// Calcular factor de correccion de la ventana
			if (sumaFCvent > 0) {
				FCvent = len/sumaFCvent;
			}
		}
		else if (vent == 4) {
			// Hanning
			temp2 = 2*(Math.PI)/(len-1);
			for (int i=0; i<len ; i++) {
				ventana = 0.5 - 0.5*Math.cos(temp2*i);
				temp = RR[i] * ventana;
				RR[i] = temp;
				sumaFCvent = sumaFCvent + Math.pow(ventana, 2);
			}
			// Calcular factor de correccion de la ventana
			if (sumaFCvent > 0) {
				FCvent = len/sumaFCvent;
			}
		}
		else {
			// Rectangular
			FCvent = 1;
		}
		
		
		// Se calcula los puntos de la fft a la siguiente potencia de 2 del tamalo del vector
		pot = Math.log(len)/Math.log(2);
		// Validar si el número es entero
		if ((pot % 1) == 0) {
			nfft = (int) len;
		}
		// el número no es entero
		else  {
			sigpot2 = (int) Math.ceil(pot);
			nfft = (int) Math.pow(2, sigpot2);
		}
		
		// Se rellena el vector con 0s segun el número de puntos de la FFT
		if (nfft > len) {
			RRceropadding = new double[nfft];
			for (int i=0; i<len; i++) {
				RRceropadding[i] = RR[i];
			}
			ini = (int) len;
			for (int i=ini; i<nfft; i++) {
				RRceropadding[i] = 0.0;
			}
		}
		else {
			RRceropadding = new double[(int) len];
			for (int i=0; i<len; i++) {
				RRceropadding[i] = RR[i];
			}
		}
		
		// Tamaño del vector
		len = RRceropadding.length;
		
		
		// Obtener el vector data=[RR,0,RR,0...2N-1] para aplicar la FFT
		// los 0s corresponden a la parte imaginaria de cada RR
		complejo = new double[(int) (2*len)];
		cont = 0;
		for (int k = 0; k < complejo.length ; k++ ) {
			if ( esImpar(k) ) {
				complejo[k] = 0;
			} else {
				complejo[k] = RRceropadding[cont];
				cont = cont + 1;
			}
		}
		
		return complejo;
	}
	
	
	/* Transformada rapida de Fourier (FFT)
	 * (Implementacion de Numerical Recipes in C)
	 * data[] es el vector de complejos a transformar
	 * isign =  1 para aplicar la FFT directa
	 * isign = -1 para aplicar la FFT inversa
	 */
	private double[] fft(double data[], int n, int isign) {
		
		// Variables
		int nn, mmax, m, j, istep, i;
		double wtemp, wr, wpr, wpi, wi, theta, tempr, tempi, temp;
		
		// Inversion de los bits
		nn = n << 1;
		j = 1;
		for (i=1; i<nn; i+=2) {
			if (j > i) {
				temp = data[j-1];
				data[j-1] = data[i-1];
				data[i-1] = temp;
				
				temp = data[j];
				data[j] = data[i];
				data[i] = temp;
			}
			m=n;
			while(m>=2 && j>m) {
				j -= m;
				m >>= 1;
			}
			j += m;
		}
		
		// Aqui empieza la sección de Danielson-Lanczos
		mmax=2;
		// Inicio del bucle externo que se ejecuta log2 nn veces
		while (nn > mmax) {
			istep = mmax << 1;
			theta = isign*((2*Math.PI)/mmax); // Se inicializa la recurrencia trigonometrica
			wtemp =  Math.sin(0.5*theta);
			wpr = -2.0*wtemp*wtemp;
			wpi = Math.sin(theta);
			wr=1.0;
			wi=0.0;
			// Inicio de bucles internos anidados que implementan la formula de Danielson-Lanczos
			for (m=1; m<mmax; m+=2) {
				for (i=m; i<=nn; i+=istep) {
					j = i + mmax;
					tempr = wr*data[j-1]-wi*data[j];
					tempi = wr*data[j]+wi*data[j-1];
					data[j-1] = data[i-1]-tempr;
					data[j] = data[i]-tempi;
					data[i-1] += tempr;
					data[i] += tempi;
				}
				wr = (wtemp=wr)*wpr-wi*wpi+wr; // Recurrencia trigonometrica
				wi = wi*wpr+wtemp*wpi+wi;
			}// Fin de bucles internos anidados
			mmax = istep;
		}// Fin ciclo de externo
		
		return data;
	}
	
	
	/* Calculo de magnitud ó fase ó potencia de la FFT Directa
	 * complejo[] es al arreglo resultante de la FFT Directa
	 * nfft son los puntos de la FFT
	 * N es el tamaño de la señal
	 * fs es la frecuencia de muestreo calculada
	 * Regresa la magnitud si opc es 1
	 * Regresa la fase si opc es 2
	 * Regresa la potencia si opc es 3
	 * Regresa la magnitud de un solo lado si opc es 4
	 * Regresa la potencia de un solo lado si opc es 5
	 */
	private Vector<Double> magfaspotDfft(double complejo[], int nfft, int N, double fs, int opc) {
		
		// Variables
		double rdata, idata, suma, mag, fas, pot, Ts;
		int len = complejo.length, cont = 0;
		
		// Se calcula el periodo de muestreo
		Ts = 1/fs;
		
		if (opc == 1) { // Se calcula magnitud
			Vector<Double> magnitud = new Vector<Double>();
			for (int i=0; i<len; i=i+2) {
				rdata = (complejo[i]);
				idata = (complejo[i+1]);
				suma = Math.pow(rdata, 2) + Math.pow(idata, 2);
				mag = (Math.sqrt(suma))/nfft;
				magnitud.addElement(mag);
			}
			magnitud.trimToSize();
			return magnitud;
			
		}
		else if (opc == 2) { // Se calcula la fase
			Vector<Double> fase = new Vector<Double>();
			for (int i=0; i<len; i=i+2) {
				rdata = complejo[i];
				idata = complejo[i+1];
				
				if (rdata == 0) { // Evita el error de dividir por cero
					if (idata < 0) {
						fas = -(Math.PI/2);
						fase.addElement(fas);
					}
					else {
						fas = Math.PI/2;
						fase.addElement(fas);
					}
				}
				else if (rdata < 0) { // Evita valores incorrectos del arctan
					if (idata < 0) {
						fas = Math.atan(idata/rdata) - Math.PI;
						fase.addElement(fas);
					}
					else {
						fas = Math.atan(idata/rdata) + Math.PI;
						fase.addElement(fas);
					}
				}
				else {
					fas = Math.atan(idata/rdata);
					fase.addElement(fas);
				}
			}
			fase.trimToSize();
			return fase;
			
		}
		else if (opc == 3) { // Se calcula la potencia
			Vector<Double> potencia = new Vector<Double>();
			for (int i=0; i<len; i=i+2) {
				rdata = (complejo[i]);
				idata = (complejo[i+1]);
				suma = Math.pow(rdata, 2) + Math.pow(idata, 2);
				pot = suma/(Math.pow(nfft, 2));
				potencia.addElement(pot);
			}
			potencia.trimToSize();
			return potencia;
			
		}
		else if (opc == 4) { // Se calcula la magnitud en un solo lado del espectro
			Vector<Integer> indice = new Vector<Integer>();
			if (esImpar(len)) {
				for (int i=0; i<((len-1)/2); i++) {
					indice.addElement(cont);
					cont = cont + 1;
				}
			}
			else {
				for (int i=0; i<(len/2); i++) {
					indice.addElement(cont);
					cont = cont + 1;
				}
			}
			indice.trimToSize();
			
			Vector<Double> magnitud = new Vector<Double>();
			for (int i=0; i<indice.size(); i=i+2) {
				rdata = (complejo[i]);
				idata = (complejo[i+1]);
				suma = Math.pow(rdata, 2) + Math.pow(idata, 2);
				mag = 2*(Math.sqrt(suma))/nfft;
				magnitud.addElement(mag);
			}
			indice.removeAllElements();
			magnitud.trimToSize();
			return magnitud;
			
		}
		else if (opc == 5) { // Se calcula la potencia en un solo lado del espectro
			// Se crean los vectores
			Vector<Double> Potencia = new Vector<Double>();
			Vector<Double> PotenciaUnLado = new Vector<Double>();
			// Se calcula la potencia
			for (int i=0; i<len; i=i+2) {
				rdata = (complejo[i]);
				idata = (complejo[i+1]);
				suma = Math.pow(rdata, 2) + Math.pow(idata, 2);
				mag = Math.sqrt(suma);
				pot = 2*(Ts/N)*Math.pow(mag, 2);
				Potencia.addElement(pot);
			}
			Potencia.trimToSize();
			// Se obtiene la potencia de un solo lado
			int len2 = (nfft/2)+1;
			for (int i=0; i<len2 ;i++) {
				PotenciaUnLado.addElement(Potencia.elementAt(i));
			}
			PotenciaUnLado.trimToSize();
			Potencia.removeAllElements();
			// Se regresa la potencia de un solo lado
			return PotenciaUnLado;
		}
		else {
			// opc debe ser 1 para calcular la magnitud
			// ó 2 para calcular la fase
			// ó 3 para calcular la potencia o energía
			// ó 4 para calcular la magnitud en un solo lado del espectro
			// ó 5 para calcular la potencia en un solo lado del espectro
			return null;
		}
	}
	
    
    /* Procesamiento en la frecuencia
     * porFTT es el vector con los valores de potencia de la FFT
     * Fs es la frecuencia de muestreo
     * NFFT es el número de puntos de la FFT
     * iniLF es el inicio de la frecuencia de bajas frecuencias
     * iniHF es el final de las bajs frecuencias y el inicio de las altas
     * finHF es el final de las altas frecuencias
     * Se calcula la energía en las bandas LF, HF y su cociente LF/HF
     */
	private void procFrec(Vector<Double> potFFT, int NFFT, double Fs, double iniLF, double iniHF, double finHF) {
    	
    	// Variables
    	int kinilf, kinihf, kfinhf, len;
    	double suma = 0.0, ebVLF = 0.0, ebLF = 0.0, ebHF = 0.0, cociLFHF = 0.0, factor = 0.0;
    	String temp;
    	
    	// Se calcula el factor para el calculo de la potencia en las bandas
    	factor = (Fs/NFFT);
    	
    	// Se calculan los indices que corresponden a las frecuencias dadas
    	kinilf = (int) Math.ceil(iniLF*NFFT*Fs);
    	kinihf = (int) Math.ceil(iniHF*NFFT*Fs);
    	kfinhf = (int) Math.ceil(finHF*NFFT*Fs);
    	// Se evita que ultimo valor este fuera del rango de las muestras
    	len = potFFT.size();
    	if (kfinhf > len) {
    		kfinhf = len;
    	}
    	
    	// Calculo de la energia para VLF
    	suma = 0;
    	for (int i=0; i<kinilf; i++) {
    		suma = suma + potFFT.elementAt(i);
    	}
    	ebVLF = factor*suma*FCvent;
    	// Se muestra el valor en pantalla
    	temp = num2string(ebVLF);
    	vlf.setText(temp);
    	
    	// Calculo de energia para LF
    	suma = 0;
    	for (int i=kinilf; i<kinihf; i++) {
    		suma = suma + potFFT.elementAt(i);
    	}
    	ebLF = factor*suma*FCvent;
    	// Se muestra el valor en pantalla
    	temp = num2string(ebLF);
    	lf.setText(temp);
    	
    	// Calculo de la energia de HF
    	suma = 0;
    	for (int i=kinihf; i<kfinhf; i++) {
    		suma = suma + potFFT.elementAt(i);
    	}
    	ebHF = factor*suma*FCvent;
    	// Se muestra el valor en pantalla
    	temp = num2string(ebHF);
    	hf.setText(temp);
    	
    	// Se calcula el cociente LF/HF
    	cociLFHF = ebLF/ebHF;
    	// Se muestra el valor en pantalla
    	temp = num2string(cociLFHF);
    	lfhf.setText(temp);
    }
    
	
	/* Mostrar valor correcto en pantalla */
	private String num2string(double number) {
		// Variables
		int numLen = 0;
    	int dotPos = -1;
    	int numAfterDot = 0;
		String temp;
		String text;
		
		temp = String.valueOf(number);
    	numLen = temp.length();
    	dotPos = temp.indexOf(".");
    	if (dotPos > -1) {
    		numAfterDot = numLen - dotPos;
    		if (numAfterDot > 3) {
    			text = temp.substring(0, dotPos+3);
    		}
    		else if (numAfterDot > 1) {
    			text = temp.substring(0, dotPos+2);
    		}
    		else if(numAfterDot > 0) {
    			text = temp.substring(0, dotPos+1);
    		}
    		else {
    			text = temp;
    		}
    	}
    	else {
    		if (numLen > 4) {
    			text = temp.substring(0, 4);
    		}
    		else {
    			text = temp;
    		}
    	}
		
    	// Texto para mostrar en pantalla
		return text;
	}
	
    
    /* Guardar archivo 1 */
	private void guardarArchivo(double datos[], String Nombre) {
    	// Variable de indicacion de que se ha guardado el archivo
		boolean guardaOK = true;
		
		// Se crea el nombre del archivo
		String Nombre_Archivo = Nombre +".txt";
		Log.i(TAG,"+++ Nombre del Archivo = " + Nombre_Archivo + " +++");
		
		// Se genera el archivo a guardar
		Vector<Double> TRF = new Vector<Double>();
		for (int i=0; i<datos.length; i++) {
			TRF.addElement(datos[i]);
		}
		String Archivo = TRF.toString();
		Log.i(TAG,Archivo);
		
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
					Log.e(TAG, "+++ guardarArchivo en tarjeta SD error no se puede escribir en /sdcard/Frecuencia_Cardiaca +++");
					guardaOK = false;
				}
			} else {
				Log.e(TAG, "+++ guardarArchivo en tarjeta SD error, directorio /sdcard no disponible +++");
				guardaOK = false;
			}
			// Fin: Se guarda en la memoria de la tarjeta SD
			
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(estado)) {
			// Unicamente se puede leer de la tarjeta SD
			Toast.makeText(this, "ERROR! La tarjeta SD no se puede escribir", Toast.LENGTH_LONG).show();
			guardaOK = false;
		} else {
			// No se puede leer ni escribir en la tarjeta SD
			Toast.makeText(this, "ERROR! No se puede leer ni escribir en la Tarjeta SD", Toast.LENGTH_LONG).show();
			guardaOK = false;
		}// Fin: Se verifica que el almacenamiento externo se encuentre disponible
		
		// Se avisa al usuario si se ha guardado el archivo
		if (guardaOK == false) {
			Toast.makeText(this, "ERROR! no se ha guardado el archivo", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Se ha guardado el archivo", Toast.LENGTH_LONG).show();
		}
    }
    
    
    /* Guardar archivo 2 */
	private void guardarArchivo2(Vector<Double> datos, String Nombre) {
    	// Variable de indicacion de que se ha guardado el archivo
		boolean guardaOK = true;
		
		// Se crea el nombre del archivo
		String Nombre_Archivo = Nombre +".txt";
		Log.i(TAG,"+++ Nombre del Archivo = " + Nombre_Archivo + " +++");
		
		// Se genera el archivo a guardar
		String Archivo = datos.toString();
		Log.i(TAG,Archivo);
		
		// Inicio: Se verifica que el almacenamiento externo se encuentre disponible
		String estado = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(estado)) {
			// Se puede leer y escribir de la tarjeta SD
			
			// Inicio: Se guarda en la memoria de la tarjeta SD
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
							// Se escribe en el archivo
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
					Log.e(TAG, "+++ guardarArchivo en tarjeta SD error no se puede escribir en /sdcard/Frecuencia_Cardiaca +++");
					guardaOK = false;
				}
			} else {
				Log.e(TAG, "+++ guardarArchivo en tarjeta SD error, directorio /sdcard no disponible +++");
				guardaOK = false;
			}
			// Fin: Se guarda en la memoria de la tarjeta SD
			
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(estado)) {
			// Unicamente se puede leer de la tarjeta SD
			Toast.makeText(this, "ERROR! La tarjeta SD no se puede escribir", Toast.LENGTH_LONG).show();
			guardaOK = false;
		} else {
			// No se puede leer ni escribir en la tarjeta SD
			Toast.makeText(this, "ERROR! No se puede leer ni escribir en la Tarjeta SD", Toast.LENGTH_LONG).show();
			guardaOK = false;
		}// Fin: Se verifica que el almacenamiento externo se encuentre disponible
		
		// Se avisa al usuario si se ha guardado el archivo
		if (guardaOK == false) {
			Toast.makeText(this, "ERROR! no se ha guardado el archivo", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Se ha guardado el archivo", Toast.LENGTH_LONG).show();
		}
		
		// Se vacia el vector
		//datos.removeAllElements();
    }
	
    
}/* Fin Actividad Principal */