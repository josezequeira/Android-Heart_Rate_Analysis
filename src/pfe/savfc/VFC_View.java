package pfe.savfc;

/* Librerias importadas */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/* Inicio Clase Principal */
public class VFC_View extends View {
	
	/** Inicio declaración de variables */
	// Depuración
	//private static final String TAG = "Clase VFC_View";
	
	// Miembros
	private Bitmap  miBitmap;
	private Canvas  miCanvas = new Canvas();
	private Paint miPaint = new Paint();
	private long startX;
	private long startY;
	private long stopX;
	private long stopY;
	private long ultimoX;
	private long delta = 4;
	private long anchoPantalla;
    private long altoPantalla;
    private double fescala;
	
    /** Fin declaración de variables */
    
	/** Inicio constructores */
	public VFC_View(Context context) {
		super(context);
	}
	
	public VFC_View(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public VFC_View(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	/** Fin constructores */
	
	/** Inicio Gets and Setters */
	public long getStartX() {
		return startX;
	}
	
	public void setStartX(long startX) {
		this.startX = startX;
	}
	
	public long getStartY() {
		return startY;
	}
	
	public void setStartY(long startY) {
		this.startY = startY;
	}
	
	public long getStopX() {
		return stopX;
	}
	
	public void setStopX(long stopX) {
		this.stopX = stopX;
	}
	
	public long getStopY() {
		return stopY;
	}
	
	public void setStopY(long stopY) {
		this.stopY = stopY;
	}
	/** Fin Gets and Setters */
	
	// Metodo onSizeChanged para controlar los cambios de tamaño de la pantalla
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        
		// Ancho y alto
		altoPantalla = h;
        anchoPantalla = w;
        // Se calcula el factor escala en el eje Y, el intervalo de tiempo
        // que se quiere graficar entre 0.0ms y 1200.0ms
     	fescala = altoPantalla/1200.0;
     	// Mapa de bits
     	miBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
     	// Se le asigna el mapa de bits al lienzo para dibujar sobre el
		miCanvas.setBitmap(miBitmap);
		// Color del fondo
        miCanvas.drawColor(Color.rgb(15, 15, 15));
        // Color de la linea
     	miPaint.setColor(Color.WHITE);
     	// Grosor de la linea
     	miPaint.setStrokeWidth(2);
     	// Lineas de borde
     	miCanvas.drawLine(0, 0, anchoPantalla, 0, miPaint); // Linea superior
     	miCanvas.drawLine(0, altoPantalla, anchoPantalla, altoPantalla, miPaint); // Linea inferior
     	miCanvas.drawLine(0, 0, 0, altoPantalla, miPaint); // Linea izquierda
     	miCanvas.drawLine(anchoPantalla, 0, anchoPantalla, altoPantalla, miPaint); // Linea derecha
     	// Grosor de la linea
     	miPaint.setStrokeWidth(1);
     	
        super.onSizeChanged(w, h, oldw, oldh);
    }
	
	// Metodo onDraw para dibujar sobre el lienzo(canva)
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		synchronized (this){
			
			// Se apunta al canvas que contiene el mapa de bits
			Canvas canva = miCanvas;
			// Se aumenta el eje X un valor delta cada ves que se recive un valor del eje Y
			ultimoX = ultimoX + delta;
			// Se escala el valor leido respecto al alto del canva que tiene el mapa de bits
			startY = Math.round(startY * fescala);
			stopY = Math.round(stopY * fescala);
			
			if (ultimoX >= anchoPantalla) {
				ultimoX = 1;
				// Color del fondo
				canva.drawColor(Color.rgb(15, 15, 15));
				// Grosor de la linea
		     	miPaint.setStrokeWidth(2);
		     	// Lineas de borde
		     	miCanvas.drawLine(0, 0, anchoPantalla, 0, miPaint); // Linea superior
		     	miCanvas.drawLine(0, altoPantalla, anchoPantalla, altoPantalla, miPaint); // Linea inferior
		     	miCanvas.drawLine(0, 0, 0, altoPantalla, miPaint); // Linea izquierda
		     	miCanvas.drawLine(anchoPantalla, 0, anchoPantalla, altoPantalla, miPaint); // Linea derecha
		     	// Grosor de la linea
		     	miPaint.setStrokeWidth(1);
				
				// Dibujar muestra
				canva.drawLine((ultimoX - delta), (altoPantalla - startY), ultimoX, (altoPantalla - stopY), miPaint);
			} else {
				// Dibujar muestra
				canva.drawLine((ultimoX - delta), (altoPantalla - startY), ultimoX, (altoPantalla - stopY), miPaint);
			}
			canvas.drawBitmap(miBitmap, 0, 0, null);
		}
	}
}/* Fin Clase Principal */