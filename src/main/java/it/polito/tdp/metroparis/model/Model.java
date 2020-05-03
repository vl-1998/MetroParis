package it.polito.tdp.metroparis.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	private Graph <Fermata,DefaultEdge> graph;
	private List <Fermata> feramte;
	private Map <Integer, Fermata> fermateIdMap; //identity map legata alle fermate
	
	public Model() {
		this.graph = new SimpleDirectedGraph <> (DefaultEdge.class);//classe della libreria graph che implementa un grafo orientato non pesato e semplice
	
		MetroDAO dao = new MetroDAO();
		this.feramte=dao.getAllFermate(); //ciascuna fermata sara un vertice del grafo
		this.fermateIdMap= new HashMap <>();
		for (Fermata f : this.feramte) { //mappa che "mappa" l'id della fermata sull'oggetto fermata
			fermateIdMap.put(f.getIdFermata(), f);
		}
		
		//CREIAMO i vertici 
		Graphs.addAllVertices(this.graph, this.feramte); //classe statica a cui passo il grafo e i vertici che devo aggiungere
		
		//CREIAMO gli archi
		//1. Un grafo ha un certo numero di vertici e per ogni coppia di vertici può esserci o meno un arco
		//(è la modalità più usata, ma spesso la meno efficiente)
		//Posso considerare tutte le possibile coppie di vertici e per ogni coppia mi chiedo se devo aggiungere l'arco o no
		//Questo metodo va bene per grafi piccoli, altrimenti deve fare moltissime query
		
		//con questo metodo consideriamo i potenziali archi 1 a 1, uso il database per una informazione di basso livello
		/*for (Fermata fp: this.feramte) { //itero sulle fermate di partenza
			for (Fermata fa: this.feramte) { //itero sulle fermate di arrivo
				if (dao.fermateConnesse(fp, fa)) { //esiste una connessione che vada fa a fp
					this.graph.addEdge(fp, fa);
				}
			}
		}
		*/
		
		
		//2. data una stazione di partenza, dammi tutte le stazioni di arrivo (fatto tramite il database) 
		//le query sono più complicate, ma riesco a ridurre drasticamente le query che faccio
		//da un vertice trova tutti i connessi
		//in questo metodo la densita del grafico è molto bassa, quindi conviene 
		//Query più piccole ripetute da java
		/*for (Fermata fp : this.feramte) {
			List <Fermata> connesse = dao.fermateSuccessive(fp, fermateIdMap);//mi faccio restituire la lista di fermate connesse a questa
			//aggiungo un arco per ciascun elemento di questa lista
			for (Fermata fa: connesse) {
				this.graph.addEdge(fp, fa);
			}
		}*/
		
		//3. Farci dare dal database direttamente gli archi che ci servono
		//Complessita lineare nel numero di archi, la più efficiente dal punto di vista del codice java
		//dipende la sua lunghezza dall'efficienza in sql
		//query più lunga, operazione immediata in java
		List <CoppiaFermata> coppie = dao.coppieFermate(fermateIdMap);
		for (CoppiaFermata c : coppie) {
			graph.addEdge(c.getFp(), c.getFa()); //mi serve perchè in java non posso restituire una collection con due oggetti
		}
		
		
		
		//System.out.println(this.graph);
		System.out.format("Grafo caricato con %d vertici e %d archi", this.graph.vertexSet().size(), this.graph.edgeSet().size());	
	}
	
	public static void main (String args[]) {
		Model m = new Model();
	}
}
