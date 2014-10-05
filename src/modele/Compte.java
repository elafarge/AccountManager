/**
 * Classe décrivant un compte bancaire (solde et liste des opérations)
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package modele;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import outils.Observable;

public class Compte extends Observable {
	private static final long serialVersionUID = 994642685754460715L;
	
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	private String nom;
	private double solde;
	private List<Operation> operations;
	private List<String> mdps; // Moyens de paiement associés à ce compte (CB, Chèque...)
	
	///////////////////////////
	//////// METHODES /////////
	///////////////////////////
	public Compte(String nom){
		this.setNom(nom);
		this.solde = 0.0;
		this.operations = new ArrayList<Operation>(); //On choisit ici une ArrayList car on doit pouvoir accéder facilement à la ième opération pour la modifier mais on doit aussi pouvoir récupérer facilement la liste dans l'ordre des dates décroissantes
		this.mdps = new ArrayList<String>();
		this.mdps.add("Cash");
		this.mdps.add("CB");
		this.mdps.add("Chèque");
	}
	
	public Compte(String nom, double solde){
		this(nom);
		this.addOperation(new Operation(this, new Date(), "-", solde, "Apport initial", "-", 0.0));
	}
	
	public void addOperation(Operation op){
		this.operations.add(op);
		this.solde += op.getMontant();
		Collections.sort(this.operations); //On garde la liste ordonnée (par Dates décroissantes)
		this.notifyObservers();
	}
	
	public void removeOperation(Operation op, boolean majSolde){
		this.operations.remove(op);
		if(majSolde){
			this.solde -= op.getMontant();
		}
		this.notifyObservers();
	}
	
	public void addMdP(String mdp){
		this.mdps.add(mdp);
		this.notifyObservers();
	}
	
	public void removeMdP(String mdp){
		this.mdps.remove(mdp);
		this.notifyObservers();
	}
	
	public String[] getMdPs(){
		String[] ret = new String[this.mdps.size()];
		for(int i = 0; i < this.mdps.size(); i++)
			ret[i] = this.mdps.get(i);
		return ret;
	}
	
	public List<String> getMdPsAsList(){
		return this.mdps;
	}
	
	public void setMdPs(List<String> mdps){
		this.mdps = new ArrayList<String>(mdps);
		this.notifyObservers();
	}
	
	/**
	 * Renvoie la liste des opérations pour affichage
	 * On renvoie en fait une liste clonée pour éviter la suppression ou l'ajout d'élément à la liste par getOperations.remove()/add()
	 * @return la liste des opérations
	 */
	public List<Operation> getOperations(){ 
		return new ArrayList<Operation>(this.operations);
	}
	
	/**
	 * Renvoie les dernières opérations ayant eu  lieu sur un compte
	 * @param n : nombre limite d'opérations à retourner
	 * @return les n dernières opérations
	 */
	public List<Operation> getLastOperations(int n){
		return new ArrayList<Operation>(this.operations.subList(0, Math.max(0, Math.min(n, operations.size())) ));
	}
	
	/**
	 * Retourne toutes les opérations ayant eu lieu entre les dates d1 (inclue) et d2 (exclue). NB si d1 > d2 la liste retournée sera vide
	 * @param d1 : date de début de la plage sélectionnée
	 * @param d2 : date de fin de la plage sélectionnée (exclue)
	 * @return
	 */
	public List<Operation> getOperationsBetween(Date d1, Date d2){
		List<Operation> ret = new ArrayList<Operation>();
		
		Iterator<Operation> i = this.operations.listIterator();
		
		while(i.hasNext()){
			Operation o = i.next();
			if(o.getDate().compareTo(d1) < 0) // Si la date de l'opération courante est inférieure à d1 on peut quitter la boucle tout de suite, on ne trouvera plus d'opérations dans l'intervalle demandé au dela de celle-ci
				break;
			
			if(o.getDate().compareTo(d1) >= 0 && o.getDate().compareTo(d2) < 0)
				ret.add(o);
		}
		
		return ret;
	}
	
	//Getters et Setters
	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
		this.notifyObservers();
	}

	/**
	 * @return le solde actuel du compte
	 */
	public double getSolde(){
		return this.solde;		
	}
}
