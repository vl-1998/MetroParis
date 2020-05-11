package it.polito.tdp.metroparis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.metroparis.model.CoppiaFermata;
import it.polito.tdp.metroparis.model.Fermata;
import it.polito.tdp.metroparis.model.Linea;

public class MetroDAO {

	public List<Fermata> getAllFermate() {

		final String sql = "SELECT id_fermata, nome, coordx, coordy FROM fermata ORDER BY nome ASC";
		List<Fermata> fermate = new ArrayList<Fermata>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Fermata f = new Fermata(rs.getInt("id_Fermata"), rs.getString("nome"),
						new LatLng(rs.getDouble("coordx"), rs.getDouble("coordy"))); //le coordinate sono usate per costruire
																						// l'oggetto latitudine e longitudine
				fermate.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return fermate;
	}

	public List<Linea> getAllLinee() {
		final String sql = "SELECT id_linea, nome, velocita, intervallo FROM linea ORDER BY nome ASC";

		List<Linea> linee = new ArrayList<Linea>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Linea f = new Linea(rs.getInt("id_linea"), rs.getString("nome"), rs.getDouble("velocita"),
						rs.getDouble("intervallo"));
				linee.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return linee;
	}
	
	//Metodo che conta il numero di connessioni tra le fermate
	public boolean fermateConnesse (Fermata fp, Fermata fa) {
		final String sql = "SELECT COUNT(*) AS c from connessione WHERE id_stazP=? AND id_stazA=?";
		
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			
			st.setInt(1, fp.getIdFermata());
			st.setInt(2, fa.getIdFermata());
			
			ResultSet res = st.executeQuery();
			
			res.first();//mi posiziono sulla prima riga per leggere il valore del numero di connessioni
			int linee= res.getInt("c");
			
			st.close();
			conn.close();

			return linee>=1;
				
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}
	}

	public List<Fermata> fermateSuccessive (Fermata fp, Map<Integer, Fermata> fermateIdMap){
		String sql="SELECT DISTINCT id_stazA FROM connessione WHERE id_stazP=?";
		List<Fermata> result = new ArrayList<Fermata>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);

			st.setInt(1, fp.getIdFermata());
			ResultSet res = st.executeQuery();
			
			while (res.next()) {
			int id_fa = res.getInt("id_stazA"); //Id fermata di arrivo
			result.add(fermateIdMap.get(id_fa));
			}
			
			st.close();
			conn.close();
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");			
		}
		
	}

	public List<CoppiaFermata> coppieFermate(Map<Integer, Fermata> fermateIdMap) {
		String sql="SELECT DISTINCT id_stazP, id_stazA FROM connessione";

		List<CoppiaFermata> result = new ArrayList<>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);

			ResultSet res = st.executeQuery();
			
			while (res.next()) {
				CoppiaFermata c = new CoppiaFermata(fermateIdMap.get(res.getInt("id_stazP")), 
						fermateIdMap.get(res.getInt("id_stazA")));
				
				result.add(c);
			}
			
			st.close();
			conn.close();
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");			
		}
	}


}
