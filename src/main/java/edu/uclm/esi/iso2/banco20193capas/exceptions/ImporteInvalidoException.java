package edu.uclm.esi.iso2.banco20193capas.exceptions;

public class ImporteInvalidoException extends Exception {
	public ImporteInvalidoException(final double importe) { //se declara como final la variable importe
		super("El importe " + importe + " no es válido para esta operación");
	}
}
