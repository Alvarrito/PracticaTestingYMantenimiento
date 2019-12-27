package edu.uclm.esi.iso2.banco20193capas.exceptions;

public class ClienteNoAutorizadoException extends Exception {
	public ClienteNoAutorizadoException(final String nif, final Long identificador) { //renombrar variable id a identificador y declarar como final la variable nif y la variable identificador
		super("El cliente con NIF " + nif + " no está autorizado para operar en la cuenta " + identificador);
	}
}
