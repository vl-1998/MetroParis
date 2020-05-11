package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

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
		System.out.format("Grafo caricato con %d vertici e %d archi\n", this.graph.vertexSet().size(), this.graph.edgeSet().size());	
	}
	
	//Visita l'intero grafo con una strategia in ampiezza e ritorna l'insieme dei vertici incontrati. Source è il 
	//vertice di partenza. Ritorna l'insieme di vertici incontrati
	//per farlo ha bisogno di un iteratore
	public List<Fermata> visitaInAmpiezza(Fermata source) {
		List <Fermata> visita = new ArrayList <>();
		BreadthFirstIterator <Fermata, DefaultEdge> bfv= new BreadthFirstIterator<>(graph, source);
		//anche GraphIterator <Fermata, DefaultEdge> bfv= new BreadthFirstIterator<>(graph, source);
		//come quando definiamo con un interfaccia e poi creiamo un oggetto
		
		//creiamo l'iteratore che si posizionerà sul primo elemento
		//vado avanti tra i vari elementi ed estraggo l'elemento successivo
		while (bfv.hasNext()) {
			visita.add(bfv.next());
		}
		
		return visita;
	}
	
	//metodo per creare un albero di visita
	public Map<Fermata,Fermata> alberoVisita (Fermata source) {
		Map <Fermata, Fermata> albero = new HashMap<>();
		albero.put(source, null); //l'unica che non ha nessun padre e quindi devo aggiungerla a mano
		GraphIterator<Fermata, DefaultEdge> bfv= new BreadthFirstIterator<>(graph, source);
		
		//prima di fare la visita aggiungo dei traversalListener
		//in questo caso ci interessa l'attracersamento dell'arco
		//aggiunge una specifica al nostro iteratore, vengono generati eventi ad ogni operazione determinata
		bfv.addTraversalListener(new TraversalListener<Fermata,DefaultEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				//la visita sta considerando un arco
				//questo arco ha scoperto un nuovo vertice? se si, provenendo da dove?
				//questo metodo riceve un oggetto composto da un arco ch mi dice quale arco sta considerando
				
				//arco attraversato (a,b): ho scoperto a partendo da b oppure b da a, lo saprò perche uno dei due non lo conoscevo
				DefaultEdge edge = e.getEdge();
				//se è gia stato visitato so gia come arrivarci, quindi sara il vertice di partenza perche è gia stato incontrato
				//l'altro sara l'id della mappa, perche non l'avevo mai incontrato quindi sara il vertice di arrivo 
				Fermata a = graph.getEdgeSource(edge);
				Fermata b = graph.getEdgeTarget(edge);
				if (albero.containsKey(a)) {
					albero.put(b, a); //aggiungo b arrivando da a, perche a lo conoscevo gia
				} else {
					albero.put(a, b);
				}
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {}

			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {}
			
		});
		
		//devo far lavorare l'iteratore
		while(bfv.hasNext()) {
			bfv.next(); // estrai l'elemento e ignoralo, non lo salvo, perchè voglio solo avere l'evento, perchè l'albero lo creo da un'altra parte
		}
		return albero;	

	}
	
	public List<Fermata> visitaInProfondita(Fermata source) {
		List <Fermata> visita = new ArrayList <>();
		DepthFirstIterator <Fermata, DefaultEdge> dfv= new DepthFirstIterator<>(graph, source);
		
		//creiamo l'iteratore che si posizionerà sul primo elemento
		//vado avanti tra i vari elementi ed estraggo l'elemento successivo
		while (dfv.hasNext()) {
			visita.add(dfv.next());
		}
		
		return visita;
	}
	
	//provo a calcolare un cammino minimo usando dijkstra
		public List <Fermata> camminiMinimi(Fermata partenza, Fermata arrivo) {
			DijkstraShortestPath <Fermata, DefaultEdge> dij = new DijkstraShortestPath <> (graph);
			GraphPath<Fermata, DefaultEdge> cammino = dij.getPath(partenza, arrivo);
			
			return cammino.getVertexList(); //traduco il cammino in un insieme di vertici che poi posso stampare
		}

	public static void main (String args[]) {
		Model m = new Model();
		/*List<Fermata> visita1 = m.visitaInAmpiezza(m.feramte.get(0));
		System.out.println(visita1);
		List<Fermata> visita2 = m.visitaInProfondita(m.feramte.get(0));
		System.out.println(visita2);
		
		Map <Fermata, Fermata> albero = m.alberoVisita(m.feramte.get(0));
		for(Fermata f : albero.keySet()) {
			System.out.format("%s <- %s\n", f, albero.get(f));
		}*/
		
		//e un grafo non orientato, quindi mi restituisce un algoritmo di visita in ampiezza
		//NON sono tutti i vertici, perche non sto facendo un algoritmo di visita, ma sto calcolando un cammino minimo
		List <Fermata> cammino = m.camminiMinimi(m.feramte.get(0), m.feramte.get(1));
		System.out.println(cammino);
	}
}
