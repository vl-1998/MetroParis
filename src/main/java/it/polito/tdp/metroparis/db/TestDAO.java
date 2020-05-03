package it.polito.tdp.metroparis.db;

import java.sql.Connection;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.metroparis.model.Fermata;

public class TestDAO {

	public static void main(String[] args) {
		
		try {
			Connection connection = DBConnect.getConnection();
			connection.close();
			System.out.println("Connection Test PASSED");
			
			MetroDAO dao = new MetroDAO() ;
			
			System.out.println(dao.getAllFermate()) ;
			System.out.println(dao.getAllLinee()) ;
			System.out.println(dao.fermateConnesse(new Fermata (358, "Neuville Universite", new LatLng (2.07843, 49.01432)) , new Fermata (124, "Conflans Fin d'Oise",new LatLng (2.07366, 48.99076) )));

		} catch (Exception e) {
			throw new RuntimeException("Test FAILED", e);
		}
	}

}
