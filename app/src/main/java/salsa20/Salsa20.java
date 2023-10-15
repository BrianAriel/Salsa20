package salsa20;

import java.util.Random;
import java.nio.*;

/*
 * Primero, se proporciona el textoPlano y la clave que debe ser de 32 bytes. Invertir el textoPlano
 * Segundo, inicializar la matriz de estado con la clave, los nonce, la constante y el offset
 * Tercero, Se inicia un bucle que se ejecuta mientras queden suficientes bytes de texto plano para formar un bloque completo (64 bytes).
 * Cuarto, en cada iteracion del bucle se genera una matriz encriptadora con el numero de bloque actual. Se usa para cifrar el bloque de texto plano
 * Quinto, se realiza una operacion XOR con la matriz encriptadora y los bytes del bloque. Aca es donde se cifra
 * Sexto, se almacena el bloque en el array resultado
 * Septimo, una vez terminado con los bucles, se manejan los bytes restantes en un bloque final
 * Octavo, se devuelve el criptograma
 */

public class Salsa20 {

	// Salsa20 tiene implementa 20 rondas donde realiza operaciones de permutacion y difusion
	private final int RONDAS = 20;
	private final int TAM_BLOQUE = 64;

	// "expand 32-byte k" en ASCII, cada numero es la representación en decimal
	private final byte[] CONSTANTE = { 101, 120, 112, 97, 110, 100, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107 };

	/* 
	 * Representa a la matriz de estado. Se inicializa con valores derivados de la clave, el nonce, el offset y una constante. 
	 * Se somete a varias rondas de operaciones (como rotación y de XOR) para generar el flujo de bytes pseudoaleatorios que se combinaran con los datos de entrada
	 */
	private int[][] matrizInicial = new int[4][4];

	/* 
	 * Representa a la matriz de salida. Usada para almacenar los datos de salida del cifrado. Cada vez que se necesita una salida de 64 bytes (o TAM_BLOQUE) 
	 * se toma una copia de estra matriz y se aplica un XOR con la matrizInicial para generar los bytes de salida
	 */
	private int[][] matrizEncriptadora = new int[4][4];

	/* Almacena resultados temporales mientras suceden las rondas de operacion */
	private int[][] matrizAux = new int[4][4];

	 /* Metodo que se ejecuta una sola vez. Inicializa la matriz inicial usando la key (parametro), dos nonce, una constante y el offset.
	  * El offset o contador se incrementa en cada iteración del cifrado para generar secuencias de bytes cifrados distintos y únicos.
	  */
	private void generarMatrizInicial(byte[] key) {
		int nonce1, nonce2;
		Random r = new Random(23);
		nonce1 = r.nextInt(Integer.MAX_VALUE);
		nonce2 = r.nextInt(Integer.MAX_VALUE);

		// Generamos la matriz (agregamos la key, la constante, los nonce y el offset)
		matrizInicial[0][0] = cargarCelda(CONSTANTE, 0);
		matrizInicial[0][1] = cargarCelda(key, 0);
		matrizInicial[0][2] = cargarCelda(key, 4);
		matrizInicial[0][3] = cargarCelda(key, 8);
		matrizInicial[1][0] = cargarCelda(key, 12);
		matrizInicial[1][1] = cargarCelda(CONSTANTE, 4);
		matrizInicial[1][2] = nonce1;
		matrizInicial[1][3] = nonce2;
		// Para una primera vez, el contador inicia en 0. luego, por cada bloque de 64B del texto plano, este contador incrementará en 1
		matrizInicial[2][0] = 0;
		matrizInicial[2][1] = 0;
		matrizInicial[2][2] = cargarCelda(CONSTANTE, 8);
		matrizInicial[2][3] = cargarCelda(key, 16);
		matrizInicial[3][0] = cargarCelda(key, 20);
		matrizInicial[3][1] = cargarCelda(key, 24);
		matrizInicial[3][2] = cargarCelda(key, 28);
		matrizInicial[3][3] = cargarCelda(CONSTANTE, 12);
	}

	/* 
	 * Método responsable de calcular y generar la matriz de estado que se utilizará para encriptar los datos en cada ronda del cifrado. 
	 */
	private void generarMatrizEncriptadora(byte[] numeroBloque) {
		int i, j;
		// Se debe cambiar el valor del contador (offset) que esta embebido en la matriz inicial
		matrizInicial[2][0] = cargarCelda(numeroBloque, 0);
		matrizInicial[2][1] = cargarCelda(numeroBloque, 4);
		matrizAux = matrizInicial.clone();

		// Ejecutamos las 20 rondas donde se realizan operaciones de rotacion y XOR
		for (i = 0; i < RONDAS; i += 2) {
			// Los parametros representan las coordenadas de las cuatro celdas que se estan procesando. Cada par de coordenadas corresponde a una celda
			quarterRound(0, 0, 1, 0, 2, 0, 3, 0);// COLUMNA 1
			quarterRound(1, 1, 2, 1, 3, 1, 0, 1);// COLUMNA 2
			quarterRound(2, 2, 3, 2, 0, 2, 1, 2);// COLUMNA 3
			quarterRound(3, 3, 0, 3, 1, 3, 2, 3);// COLUMNA 4

			quarterRound(0, 0, 0, 1, 0, 2, 0, 3);// FILA 1
			quarterRound(1, 1, 1, 2, 1, 3, 1, 0);// FILA 2
			quarterRound(2, 2, 2, 3, 2, 0, 2, 1);// FILA 3
			quarterRound(3, 3, 3, 0, 3, 1, 3, 2);// FILA 4
		}

		// Almaceno el resultado entre la auxiliar y la inicial en la matrizEncriptadora
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				matrizEncriptadora[i][j] = matrizAux[i][j] + matrizInicial[i][j];
			}
		}
	}

	// Metodo para implementar el proceso de cifrado a partir de un textoPlano y una key
	public byte[] encriptar(byte[] textoPlano, byte[] key) {
		int numeroBloque = 0;
		int cantBytesTextoPlano = textoPlano.length;
		int i, j, k;
		byte[] block = new byte[TAM_BLOQUE];
		byte[] bloqueAux = new byte[4];
		byte[] criptograma;
		
		/* 
		 * Salsa20 asume que los datos de entrada están en formato "little-endian", donde los bytes menos significativos se almacenan en las primeras posiciones de la secuencia de bytes.
		 * Como Java almacena en formato "big-endian" por defecto, donde los bytes más significativos se almacenan en las primeras posiciones invertimos el textoPlano para poder aplicar el cifrado
		 */
		ByteBuffer buffer = ByteBuffer.wrap(textoPlano.clone());
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		textoPlano = buffer.array();

		// Generamos por primera vez la matriz inicial, con el numero de bloque en 0
		generarMatrizInicial(key);
		criptograma = new byte[cantBytesTextoPlano];

		// Aca se realiza el XOR
		while (cantBytesTextoPlano >= TAM_BLOQUE) {

			// Generamos la matriz encriptadora para esta pasada
			generarMatrizEncriptadora(ByteBuffer.allocate(8).putInt(numeroBloque).array());

			// Dividimos el texto plano, cargando un bloque de 64 Bytes
			for (i = 0; i < 64; i++) {
				block[i] = textoPlano[(int) (numeroBloque * 64) + i];
			}

			// Hacemos el XOR con el texto plano
			for (i = 0; i < 4; i++) {
				for (j = 0; j < 4; j++) {
					bloqueAux = ByteBuffer.allocate(4).putInt(matrizEncriptadora[i][j]).array();

					for (k = 0; k < 4; k++) {
						block[k + (i * 16 + j * 4)] ^= bloqueAux[k];
					}
				}
			}

			// Incorporamos el bloque cifrado al criptograma
			for (i = 0; i < TAM_BLOQUE; i++) {
				criptograma[i + (int) (numeroBloque * TAM_BLOQUE)] = block[i];
			}

			numeroBloque++;
			cantBytesTextoPlano -= TAM_BLOQUE;
		}

		
		// Esto se realiza en caso en que que la longitud del texto plano no es múltiplo de 6.
		if (cantBytesTextoPlano != 0) {
			
			int aux = cantBytesTextoPlano;

			// Generamos la matriz encriptadora para la última pasada
			generarMatrizEncriptadora(ByteBuffer.allocate(8).putInt(numeroBloque).array());

			// Cargamos los n úlitmos bytes que faltan cifrar
			for (i = 0; i < cantBytesTextoPlano; i++) {
				block[i] = textoPlano[(int) (numeroBloque * 64) + i];
			}

			// Hacemos el XOR con el texto plano
			for (i = 0; i < 4; i++) {
				for (j = 0; j < 4; j++) {
					bloqueAux = ByteBuffer.allocate(4).putInt(matrizEncriptadora[i][j]).array();

					for (k = 0; k < 4; k++) {

						if (cantBytesTextoPlano > 0) {
							block[k + (i * 16 + j * 4)] ^= bloqueAux[k];
							cantBytesTextoPlano--;
						}
					}
				}
			}

			// Incorporamos el bloque cifrado al criptograma
			for (i = 0; i < aux; i++) {
				criptograma[i + (int) (numeroBloque * TAM_BLOQUE)] = block[i];
			}

		}

		return criptograma;
	}

	public byte[] desencriptar(byte[] criptograma, byte[] key) {
		return encriptar(criptograma, key);
	}

	private int rotateL(int a, int b) {
		return (a << b) | (a >>> (32 - b)); // a<<b --> a= a.2^b
	}

	/*
	 * funcion de rotación que hace operaciones ARX, XOR rotate sobre 4 elementos.
	 */
	private void quarterRound(int xa, int ya, int xb, int yb, int xc, int yc, int xd, int yd) {

		matrizAux[xb][yb] ^= rotateL(matrizAux[xa][ya] + matrizAux[xd][yd], 7);
		matrizAux[xc][yc] ^= rotateL(matrizAux[xb][yb] + matrizAux[xa][ya], 9);
		matrizAux[xd][yd] ^= rotateL(matrizAux[xc][yc] + matrizAux[xb][yb], 13);
		matrizAux[xa][ya] ^= rotateL(matrizAux[xd][yd] + matrizAux[xc][yc], 18);

	}


	private int cargarCelda(byte[] x, int offset) {
		return ((int) (x[offset]) & 0xff) | ((((int) (x[offset + 1]) & 0xff)) << 8)
				| ((((int) (x[offset + 2]) & 0xff)) << 16) | ((((int) (x[offset + 3]) & 0xff)) << 24);
	}
}
