/**
 * Classe décrivant un utilisateur
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package modele;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import outils.Observable;
import outils.Observer;

public class Utilisateur extends Observable implements Observer {
	private static final long serialVersionUID = -3666536618599866359L;
	
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	private String nom;
	private List<Compte> comptes;
	private List<String> postes; //Postes pour les débits
	private List<String> postesCredit; //Postes pour les crédits
	
	///////////////////////////
	//////// METHODES /////////
	///////////////////////////
	public Utilisateur(){
		this.setNom("John Doe");
		this.comptes = new Vector<Compte>(); //J'ai choisi un Vector car une fois les comptes créés, on ne vas pas en ajouter/supprimer souvent. En revanche accéder au compte i pour lire ses données sera chose fréquente
		this.postes = new ArrayList<String>();
		this.postes.add("Alimentation");
		this.postes.add("Loyer/Prêt");
		this.postes.add("Transports");
		this.postes.add("Shopping");
		this.postesCredit = new ArrayList<String>();
		this.postesCredit.add("Salaires");
		this.postesCredit.add("Indemnités");
	}
	
	public Utilisateur(String nom){
		this(); // Appel du constructeur ci-dessus
		this.setNom(nom);
	}
	
	public Utilisateur(String nom, List<Compte> comptes){
		this(nom);
		this.comptes = comptes;
	}
	
	/**
	 * Retourne le solde total de l'utilisateur (la somme d'argent qu'il a sur tout ses comptes)
	 * @return
	 */
	public double soldeTotal(){
		Iterator<Compte> i = this.comptes.listIterator();
		
		double soldeTotal = 0.0;
		while(i.hasNext()){
			Compte c = i.next();
			c.addObserver(this); // Si soldeTotal est appelée ça veut dire qu'on affiche le solde quelque part donc qu'il faut mettre a jour utilisateur (car ce total change) quand le solde d'un compte change, on surveille donc nos comptes de près depuis la classe Utilisateur
			soldeTotal += c.getSolde();
		}
		return soldeTotal;
	}
	
	/**
	 * Renvoie toutes les opérations effectuées sur tous les comptes de l'utlisateur
	 * @return les opérations effectuées par l'utilisateur
	 */
	public List<Operation> getOperations(){
		List <Operation> listOp = new ArrayList<Operation>();
		
		Iterator<Compte> i = this.comptes.listIterator();
		
		while(i.hasNext()){
			listOp.addAll(i.next().getOperations());
		}
		
		Collections.sort(listOp);
		
		return listOp;
	}
	
	/**
	 * Renvoie les n dernières opérations effectuées sur tous les comptes de l'utlisateur
	 * @return les n dernières opérations effectuées par l'utilisateur
	 */
	public List<Operation> getLastOperations(int n){
		List <Operation> listOp = new ArrayList<Operation>();
		
		Iterator<Compte> i = this.comptes.listIterator();
		
		while(i.hasNext()){
			listOp.addAll(i.next().getLastOperations(n));
		}
		
		Collections.sort(listOp);
		
		return listOp.subList(0, Math.max(n, listOp.size()) );
	}
	
	public Compte getCompteGlobal(){
		Compte cg = new Compte("Global");
		
		Iterator<Compte> i = this.comptes.listIterator();
		while(i.hasNext()){
			Iterator<Operation> j = i.next().getOperations().listIterator();
			while(j.hasNext())
				cg.addOperation(j.next());
		}
		
		return cg;
	}
	
	// Getters et Setters (généré par Eclipse) (les Setters préviennent les Observers)
	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
		this.notifyObservers();
	}
	
	public Vector<Compte> getComptes(){
		return new Vector<Compte>(this.comptes);
	}
	
	public void ajouterCompte(Compte c){
		this.comptes.add(c);
		this.notifyObservers();
	}
	
	public void supprimerCompte(Compte c){
		this.comptes.remove(c);
		this.notifyObservers();
	}
	
	public void addPosteCredit(String poste){
		this.postesCredit.add(poste);
		this.notifyObservers();
	}
	
	public void removePosteCredit(String poste){
		this.postesCredit.remove(poste);
		this.notifyObservers();
	}
	
	public String[] getPostesCredit(){
		String[] ret = new String[this.postesCredit.size()];
		for(int i = 0; i < this.postesCredit.size(); i++)
			ret[i] = this.postesCredit.get(i);
		return ret;
	}
	
	public List<String> getPostesCreditAsList(){
		return this.postesCredit;
	}
	
	public void setPostesCredit(List<String> postes){
		this.postes = new ArrayList<String>(postes);
		this.notifyObservers();
	}
	
	public void addPoste(String poste){
		this.postes.add(poste);
		this.notifyObservers();
	}
	
	public void removePoste(String poste){
		this.postes.remove(poste);
		this.notifyObservers();
	}
	
	public String[] getPostes(){
		String[] ret = new String[this.postes.size()];
		for(int i = 0; i < this.postes.size(); i++)
			ret[i] = this.postes.get(i);
		return ret;
	}
	
	public List<String> getPostesAsList(){
		return this.postes;
	}
	
	public void setPostes(List<String> postes){
		this.postes = new ArrayList<String>(postes);
		this.notifyObservers();
	}
	
	//Lorsque l'un des comptes est mis à jour, on appelle les observers d'Utilisateurs pour leur dire que le solde total à changé
	@Override
	public void update() {
		this.notifyObservers();
	}

}
