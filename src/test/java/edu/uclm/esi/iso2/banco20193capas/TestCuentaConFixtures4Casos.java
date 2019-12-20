package edu.uclm.esi.iso2.banco20193capas;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uclm.esi.iso2.banco20193capas.exceptions.ClienteNoAutorizadoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ClienteNoEncontradoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaInvalidaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaYaCreadaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ImporteInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.PinInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.SaldoInsuficienteException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.TarjetaBloqueadaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.TokenInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.model.Cliente;
import edu.uclm.esi.iso2.banco20193capas.model.Cuenta;
import edu.uclm.esi.iso2.banco20193capas.model.Manager;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaCredito;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaDebito;
import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestCuentaConFixtures4Casos extends TestCase {
	private Cuenta cuentaPepe, cuentaAna;
	private Cliente pepe, ana;
	private TarjetaDebito tdPepe, tdAna;
	private TarjetaCredito tcPepe, tcAna;
	
	@Before
	public void setUp() {
		Manager.getMovimientoDAO().deleteAll();
		Manager.getMovimientoTarjetaCreditoDAO().deleteAll();
		Manager.getTarjetaCreditoDAO().deleteAll();
		Manager.getTarjetaDebitoDAO().deleteAll();
		Manager.getCuentaDAO().deleteAll();
		Manager.getClienteDAO().deleteAll();
		
		this.pepe = new Cliente("12345X", "Pepe", "Pérez"); this.pepe.insert();
		this.ana = new Cliente("98765F", "Ana", "López"); this.ana.insert();
		this.cuentaPepe = new Cuenta(1); this.cuentaAna = new Cuenta(2);
		try {
			this.cuentaPepe.addTitular(pepe); this.cuentaPepe.insert(); this.cuentaPepe.ingresar(1000);
			this.cuentaAna.addTitular(ana); this.cuentaAna.insert(); this.cuentaAna.ingresar(5000);
			this.tcPepe = this.cuentaPepe.emitirTarjetaCredito(pepe.getNif(), 2000);
			this.tcAna = this.cuentaAna.emitirTarjetaCredito(ana.getNif(), 10000);
			this.tdPepe = this.cuentaPepe.emitirTarjetaDebito(pepe.getNif());
			this.tdAna = this.cuentaAna.emitirTarjetaDebito(ana.getNif());
			
			this.tcPepe.cambiarPin(tcPepe.getPin(), 1234);
			this.tcAna.cambiarPin(tcAna.getPin(), 1234);
			this.tdPepe.cambiarPin(tdPepe.getPin(), 1234);
			this.tdAna.cambiarPin(tdAna.getPin(), 1234);
		}
		catch (Exception e) {
			fail("Excepción inesperada en setUp(): " + e);
		}
	}
	
	@Test
	public void testRetiradaSinSaldo() {
		try {
			this.cuentaPepe.retirar(2000);
			fail("Esperaba SaldoInsuficienteException");
		} catch (ImporteInvalidoException e) {
			fail("Se ha producido ImporteInvalidoException");
		} catch (SaldoInsuficienteException e) {
		}
	}
	
	@Test
	public void testTarjetaBloqueada() {
		try {
			this.tdPepe.sacarDinero(1234,2300);
			fail("Esperaba TarjetaBloqueadaException");
		}catch(TarjetaBloqueadaException e) {
		} catch (Exception e) {
			fail("Esperaba TarjetaBloqueadaException");
		}
	}

	@Test
	public void testTransferencia() {
		try {
			this.cuentaPepe.transferir(this.cuentaAna.getId(), 500, "Alquiler");
			assertTrue(this.cuentaPepe.getSaldo() == 495);
			assertTrue(this.cuentaAna.getSaldo() == 5500);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCompraConTC() {
		try {
			cuentaPepe.retirar(200);
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("12345X", 1000);
			tc.comprar(tc.getPin(), 300);
			assertTrue(tc.getCreditoDisponible()==700);
			tc.liquidar();
			assertTrue(tc.getCreditoDisponible()==1000);
			assertTrue(cuentaPepe.getSaldo()==500);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCompraPorInternetConTC() {
		try {
			this.cuentaPepe.retirar(200);
			assertTrue(this.cuentaPepe.getSaldo()==800);
			
			int token = this.tcPepe.comprarPorInternet(tcPepe.getPin(), 300);
			assertTrue(this.tcPepe.getCreditoDisponible()==2000);
			this.tcPepe.confirmarCompraPorInternet(token);
			assertTrue(this.tcPepe.getCreditoDisponible()==1700);
			this.tcPepe.liquidar();
			assertTrue(this.tcPepe.getCreditoDisponible()==2000);
			assertTrue(cuentaPepe.getSaldo()==500);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testBloqueoDeTarjeta() {
			try {
				this.tcPepe.comprarPorInternet(5678, 100);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			} 
			try {
				this.tcPepe.comprarPorInternet(5678, 100);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			}
			try {
				this.tcPepe.comprarPorInternet(5678, 100);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			}
			try {
				this.tcPepe.comprarPorInternet(1234, 100);
			} catch (TarjetaBloqueadaException e) {
			} catch (Exception e) {
				fail("Esperaba TarjetaBloqueadaException");
			}
	}
	
	@Test
	public void testCambiarPinMal() {
		try {
			this.tdAna.cambiarPin(5412, 4521);
			fail("Esperaba PinInvalidoException");
		} catch (PinInvalidoException e) {
		} catch (Exception e) {
			fail("Esperaba PinInvalidoException" + e);
		}
	}

	@Test
	public void testTransferenciaSinSaldo() {
		try {
			this.cuentaAna.transferir(this.cuentaPepe.getId(), 10000, "Pago coche");
			fail("Esperaba SaldoInsuficienteException");
		} catch (ImporteInvalidoException e) {
			fail("Se ha producido ImporteInvalidoException");
		} catch (SaldoInsuficienteException e) {
		} catch (CuentaInvalidaException e) {
		}
	}
	@Test
	public void testIngresarNegativo(){
	    double cantidad = -250.0;
	    double esperado = 0.0;

	    try {
	    	cuentaAna.ingresar(cantidad);
	        fail("Esperaba ImporteInvalidoException");
	    } catch (ImporteInvalidoException e) {
	        assertEquals(0.0, 0.0);
	    }

	}
	@Test
	public void testTransferenciaCuentaInexistente() {		
		try {
			this.cuentaAna.transferir(656346L, 0, "Prueba cuenta");
		} catch (ImporteInvalidoException e) {
		} catch (SaldoInsuficienteException e) {
		} catch (CuentaInvalidaException e) {
			fail("Se ha producido CuentaInvalidaException");
		}
	}
	@Test
	public void testCambioMismoPin(){
		try{
			this.tcPepe.cambiarPin(tcPepe.getPin(), 1234);
			this.tdPepe.cambiarPin(tdPepe.getPin(), 1234);
			assertTrue(tcPepe.getPin() == 1234);
			assertTrue(tdPepe.getPin() == 1234);
			
		}catch(PinInvalidoException e){
			fail("Producido ExcepcionPinInvalido");
		}
	}
	@Test
	public void testTransferenciaEnNegativo(){
		try{
			this.cuentaPepe.transferir(this.cuentaAna.getId(), -1000, "Blanqueo");
			fail("Esperaba ImporteInvalidoException");
		}catch (ImporteInvalidoException e) {
			
		} catch (CuentaInvalidaException e) {
			e.printStackTrace();
		} catch (SaldoInsuficienteException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void noAutorizadoFallo(){
		
		try{
			this.tcAna = this.cuentaAna.emitirTarjetaCredito(pepe.getNif(), 10000);
		}catch (ClienteNoAutorizadoException  e){
			
		}catch(ClienteNoEncontradoException e) {
			
		}
		
		
	}


		
		@Test
		public void testCuentaYaCreada(){
						
			try {
				this.cuentaPepe.addTitular(pepe);
			} catch (CuentaYaCreadaException e) {

			}
			
	}

}
