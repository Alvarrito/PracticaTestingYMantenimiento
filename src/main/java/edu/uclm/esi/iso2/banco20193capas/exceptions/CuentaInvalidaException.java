package edu.uclm.esi.iso2.banco20193capas.exceptions;

public class CuentaInvalidaException extends Exception {

	public CuentaInvalidaException(final Long numero) { //se declara como final la variable numero
		super("La cuenta " + numero + " no existe");
	}

}
