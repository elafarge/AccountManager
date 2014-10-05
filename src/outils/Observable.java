/**
 * Classe abstraite décrivant tout objet observable. Ici les modèles correspondent aux classes "Compte", "Operation" et "Utilisateur". Chaque modèle sera relié à 
 * une vue qui - dans le programme final - se matérialisera par un Widget SWING.
 * J'ai appris à utiliser cette technique sur cette page : http://www.siteduzero.com/informatique/tutoriels/apprenez-a-programmer-en-java/mieux-structurer-son-code-le-pattern-mvc
 *
 * Cette classe implémente l'interface Serializable car ses enfants seront des données que l'on souhaite stocker sur le disque dur. On implémente donc Serializable
 * dans leur classe mère une bonne fois pour toutes.
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */

package outils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Observable implements Serializable{
	private static final long serialVersionUID = -3182799589276475569L;
	
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	private transient List<Observer> observers; //Ne surtout pas sérialiser les observers, j'en ai fais les frais. Ca cause une sacrée pagaille ! En effet, a chaque démarrage du programme on ajoute un observer à la liste actuelle ce qui finit par faire beaucoup
	
	private transient List<NotifyOverObserver> noObservers;
	
	///////////////////////////
	//////// METHODES /////////
	///////////////////////////
	
	/**
	 * Construit un objet Observable
	 */
	public Observable(){
		//J'ai choisi l'implémentation LinkedList car on appelle toujours tous les observers à la fois (on ne fais jamais de "observers.get(i)" et on en ajoute
		//fréquemment. Cette implémentation convient donc mieux que ArrayList ou Vector
		this.observers = new LinkedList<Observer>();
		this.noObservers = new LinkedList<NotifyOverObserver>();
	}
	
	/**
	 * Appelle tous les observateurs de la donnée courante pour leur dire de se mettre à jour car cette donnée a été modifiée
	 */
	public void notifyObservers(){
		//Si jamais il reste du monde qui écoute la fin du dispatch des notifications on les enlève car ce n'est pas la fin de cette notification qu'ils attendent (les listener de fin de notification sont faits pour être ajoutés en cours de dispatch pour savoir à partir de quand on peut de nouveau ajouter/supprimer des observers et seulement cela!)
		this.resetNotifyOverObservers();
		
		Iterator<Observer> i = this.observers.listIterator();
		
		while(i.hasNext()){
			i.next().update();
		}
		
		notifyObserversEnd();
	}
	
	private void notifyObserversEnd() {
		Iterator<NotifyOverObserver> i = this.noObservers.listIterator();
		
		while(i.hasNext()){
			i.next().onNotifyOver();
		}
		
		this.resetNotifyOverObservers();
	}

	/**
	 * Enregistre un nouvel Observer
	 * @param o : l'Observer à ajouter
	 * @return
	 */
	public boolean addObserver(Observer o){
		//Dans le cas ou notre constructeur n'aurait pas été appelé (vous avez dit sérialisation?), on instancie gentiment notre liste d'Observers
		if(this.observers == null)
			observers = new LinkedList<Observer>();
		
		if(!this.observers.contains(o))
			return this.observers.add(o);
		
		return false;
	}
	
	/**
	 * Supprime tous les Observers enregistrés
	 */
	public void resetObservers(){
		this.observers = new LinkedList<Observer>();
	}
	
	/**
	 * Supprime un Observer en particulier.
	 * @param o : l'Observer à supprimer.
	 */
	public void removeObserver(Observer o){
		this.observers.remove(o);
	}
	
	/**
	 * Enregistre un nouvel Observer de fin de notification
	 * @param o : l'Observer à ajouter
	 * @return
	 */
	public boolean addNotifyOverObserver(NotifyOverObserver o){
		//Dans le cas ou notre constructeur n'aurait pas été appelé (vous avez dit sérialisation?), on instancie gentiment notre liste d'Observers
		if(this.noObservers == null)
			noObservers = new LinkedList<NotifyOverObserver>();
		
		if(!this.noObservers.contains(o))
			return this.noObservers.add(o);
		
		return false;
	}
	
	/**
	 * Supprime tous les Observers enregistrés
	 */
	public void resetNotifyOverObservers(){
		this.noObservers = new LinkedList<NotifyOverObserver>();
	}
	
	/**
	 * Supprime un Observer en particulier.
	 * @param o : l'Observer à supprimer.
	 */
	public void removeNotifyOverObserver(NotifyOverObserver o){
		this.noObservers.remove(o);
	}
	
	
}
