/**
 * Classe décrivant une opération bancaire. Celle ci sont comparables par Date
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package modele;

import java.util.Comparator;
import java.util.Date;

import outils.Observable;

public class Operation extends Observable implements Comparable<Operation> {
	private static final long serialVersionUID = 200966735142063945L;
	
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	private Compte compte;
	private Date date;
	private String mdp;
	private double montant, soldeAvant;
	private String poste;
	private String benef_creancier; //Bénéficiaire (pour un débit) ou créancier (pour un crédit)

	///////////////////////////
	//////// METHODES /////////
	///////////////////////////	
	public Operation(Compte c, Date date, String mdp, double montant, String poste, String bencr, double solde_avant){
		this.date = date;
		this.mdp = mdp;
		this.montant = montant;
		this.poste = poste;
		this.benef_creancier = bencr;
		this.compte = c;
		this.soldeAvant = solde_avant;
	}
	
	@Override
	public int compareTo(Operation op){
		return - date.compareTo(op.getDate()); // il y'a un - car on veut trier dans l'ordre décroissant des dates
	}
	
	//Getters et Setters	
	public String getMdp() {
		return mdp;
	}

	public void setMdp(String mdp) {
		this.mdp = mdp;
		this.notifyObservers();
	}

	public double getMontant() {
		return montant;
	}

	public void setMontant(double montant) {
		
		//On pense bien à modifier le solde du compte associé s'il y en a un
		if(this.compte != null){
			this.compte.removeOperation(this, true);
			this.montant = montant;
			this.compte.addOperation(this);
		}else
			this.montant = montant;
		
		this.notifyObservers();
	}

	public String getPoste() {
		return poste;
	}

	public void setPoste(String poste) {
		this.poste = poste;
		this.notifyObservers();
	}

	public Date getDate() {
		return date;
	}
	
	public void setDate(Date d){
		this.date = d;
		this.notifyObservers();
	}
	
	public Compte getCompte(){
		return this.compte;
	}

	public String getBenef_creancier() {
		return benef_creancier;
	}

	public void setBenef_creancier(String benef_creancier) {
		this.benef_creancier = benef_creancier;
		this.notifyObservers();
	}

	public double getSoldeAvant() {
		return soldeAvant;
	}

	public void setSoldeAvant(double solde_avant) {
		this.soldeAvant = solde_avant;
		this.notifyObservers();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///// Les classes de comparateurs permettant d'ordonner une liste d'opérations suivant divers critères  ////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static class CompMdp implements Comparator<Operation>{
		@Override
		public int compare(Operation o1, Operation o2) {
			return o1.mdp.compareTo(o2.mdp);
		}
	}
	
	public static class CompMontant implements Comparator<Operation>{
		@Override
		public int compare(Operation o1, Operation o2) {
			return o1.montant == o2.montant ? 0 : (o1.montant < o2.montant ? -1 : 1);
		}
	}
	
	public static class CompPoste implements Comparator<Operation>{
		@Override
		public int compare(Operation o1, Operation o2) {
			return o1.poste.compareTo(o2.poste);
		}
	}
	
	public static class CompBenef_creancier implements Comparator<Operation>{
		@Override
		public int compare(Operation o1, Operation o2) {
			return o1.benef_creancier.compareTo(o2.benef_creancier);
		}
	}
	
	public static class CompSoldeAvant implements Comparator<Operation>{
		@Override
		public int compare(Operation o1, Operation o2) {
			return o1.soldeAvant == o2.soldeAvant ? 0 : (o1.soldeAvant < o2.soldeAvant ? -1 : 1);
		}
	}
	
	public static class CompSoldeApres implements Comparator<Operation>{
		@Override
		public int compare(Operation o1, Operation o2) {
			double soldeApres1 = o1.soldeAvant + o1.montant, soldeApres2 = o2.soldeAvant + o2.montant;
			
			return soldeApres1 == soldeApres2 ? 0 : (soldeApres1 < soldeApres2 ? -1 : 1);
		}
	}
	
	public static class CompCompte implements Comparator<Operation>{
		@Override
		public int compare(Operation o1, Operation o2) {
			return o1.compte.getNom().compareTo(o2.compte.getNom());
		}
	}
	
}
