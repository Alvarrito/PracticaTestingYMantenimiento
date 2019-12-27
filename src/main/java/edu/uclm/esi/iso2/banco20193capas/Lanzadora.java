package edu.uclm.esi.iso2.banco20193capas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.uclm.esi.iso2.banco20193capas.model.Cliente;
import edu.uclm.esi.iso2.banco20193capas.model.Cuenta;

@SpringBootApplication
public class Lanzadora {

public static void main(String[] args) throws Exception { //se cuidan los espacios y las tabulaciones para tener el estilo correcto
	SpringApplication.run(Lanzadora.class, args);	
	try {
		final Cliente pepe = new Cliente("12345X", "Pepe", "Pérez"); //ponemos final
		pepe.insert();
		
		final Cuenta cuenta = new Cuenta(); //ponemos final
		cuenta.addTitular(pepe);
		cuenta.insert();
			
		cuenta.ingresar(1000);
	} catch (Exception e) {
		e.printStackTrace();
	}		
}

}