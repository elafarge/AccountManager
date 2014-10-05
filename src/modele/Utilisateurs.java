/**
 * Classe encapsulant la liste de tous les utilisateurs du logiciel
 * Elle gère entre autres le chargement et la sauvegarde des données
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package modele;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import outils.NotifyOverObserver;
import outils.Observable;
import outils.Observer;

public class Utilisateurs extends Observable implements Observer, NotifyOverObserver{
	private static final long serialVersionUID = -1791063907086236112L;

	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	public static final String FILEPATH = System.getProperty("user.home") + "/.suivi_de_depenses.dat";
	
	private List<Utilisateur> utilisateurs;
	
	///////////////////////////
	//////// METHODES /////////
	///////////////////////////
	public Utilisateurs(){
		
		//Chargement des données depuis le disque dur
		try {
			FileInputStream fis = new FileInputStream(new File(FILEPATH));
			ObjectInputStream ois = new ObjectInputStream(fis);
			Utilisateurs usrs = (Utilisateurs)ois.readObject(); // Procéder comme suit plutot que directement comme suit : "this.utilisateurs = (ArrayList<Utilisateur>) ois.readObject();" permet d'éviter un Unchecked Type Warning lors du cast
			this.utilisateurs = usrs.getList();
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			//Si le fichier n'est pas trouvé, c'est pas grave, c'est juste que c'est la première fois qu'on lance l'application
			utilisateurs = new ArrayList<Utilisateur>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.veillerAuGrain();
		
	}
	
	/**
	 * Met à jour les Observables qu'observe utilisateurs (tous les objets qu'il contient en fait). A appeler lors de la création de cet objet et lors de la modification de tout sous-objet (Utilisateur, Compte et Operation)
	 */
	private void veillerAuGrain(){		
		//Enregistrement de la classe courante comme listener de tous les sous objets (chaque utilisateur, chaque compte, chaque opération) pour pouvoir sauvegrder le tout au moindre changement (il n'y aura donc pas de bouton save dans notre IHM... pas besoin, à l'heure actuelle c'est un peu dépassé)
		Iterator<Utilisateur> i = this.utilisateurs.listIterator();
		
		while(i.hasNext()){
			Utilisateur u = i.next();
			u.addObserver(this);
			
			Iterator<Compte> j = u.getComptes().listIterator();
			while(j.hasNext()){
				Compte c = j.next();
				//c.addObserver(this); //Inutile car quand un compte est modifié, l'utilisateur le possédant met à jour ses observateurs. On aurait donc un double appel si on ne commentait pas cette ligne
				
				Iterator<Operation> k = c.getOperations().listIterator();
				while(k.hasNext()){
					k.next().addObserver(this);
				}
			}
		}
		
		this.addObserver(this); //Et oui il faut aussi sauvegarder en cas d'ajout d'utilisateur
	}
	
	//Sauvegarde à chaque modification de quoi que ce soit (la sauvegarde se fait dans un Thread Séparé car sinon elle est généralement appelée depuis la fonction de callback d'un événement donc depuis l'Event Dispatch Thread donc on fait freezer l'IHM lors des sauvegardes si on les effectue depuis l'EDT. D'où l'intérêt de mon Thread séparé.
	@Override
	public void update() {
		this.save();
		this.waitForNotifyDispatchOverBeforeUpdatingObservedObjects();
	}
	
	/**
	 * (Petite perte de sang froid lors du choix du nom de la méthode) Celle-ci ordonne de mettre à jour la liste des objets observés une fois que ceux-ci ont fini de notifier tous leurs observers de leur modification
	 */
	private void waitForNotifyDispatchOverBeforeUpdatingObservedObjects() {
		Iterator<Utilisateur> i = this.utilisateurs.listIterator();
		
		while(i.hasNext()){
			Utilisateur u = i.next();
			u.addNotifyOverObserver(this);
			
			Iterator<Compte> j = u.getComptes().listIterator();
			while(j.hasNext()){
				Compte c = j.next();
				c.addNotifyOverObserver(this);
				
				Iterator<Operation> k = c.getOperations().listIterator();
				while(k.hasNext()){
					k.next().addNotifyOverObserver(this);
				}
			}
		}
		
		this.addNotifyOverObserver(this); //Et oui il faut aussi sauvegarder en cas d'ajout d'utilisateur
	}

	@Override
	public void onNotifyOver() {
		this.veillerAuGrain();
	}
	
	public void add(Utilisateur u){
		this.utilisateurs.add(u);
		this.notifyObservers();
	}
	
	public void remove(Utilisateur u){
		this.utilisateurs.remove(u);
		this.notifyObservers();
	}
	
	/**
	 * Enregistre toutes les données dans un fichier pour que celles-ci puissent être récupérées à chaque lancement de l'application
	 */
	public synchronized void save(){
		try {
			FileOutputStream fos = new FileOutputStream(new File(FILEPATH));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.flush();
			System.out.println("Données sauvegardées sur " + FILEPATH);
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Getter uniquement pour la sérialisation
	private List<Utilisateur> getList(){
		return this.utilisateurs;
	}
	
	//Autres getters et setters
	public List<Utilisateur> getUtilisateurs(){
		return new ArrayList<Utilisateur>(this.utilisateurs);
	}
}
