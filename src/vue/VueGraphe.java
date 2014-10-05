/**
 * JPanel affichant le graphe relatif à un compte et permettant de régler la période d'affichage du Graphe.
 * Elle identifie les 3 pôles de dépenses majeurs sur la période indiquée, puis fait la somme des dépenses sur chaque intervalle d'affichage du Graphe.
 * En choisissant des intervalles d'une longueur égale à la période d'affichage.
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package vue;

import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.MaskFormatter;

import modele.Compte;
import modele.Operation;
import modele.Utilisateur;

public class VueGraphe extends JPanel{
	private static final long serialVersionUID = -5179754612385519029L;
	
	///////////////////////////
	//////// CONSTANTES ///////
	///////////////////////////
	static final int PERIOD_MONTH = Calendar.MONTH;
	static final int PERIOD_DAY = Calendar.DAY_OF_MONTH;
	static final int PERIOD_YEAR = Calendar.YEAR;
	static final int PERIOD_WEEK = Calendar.WEEK_OF_YEAR;
	
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	
	private Compte compte;
	
	private List<String> poles;
	private TreeMap<Date, TreeMap<String, Double> > donnees; //NB : on utilise une TreeMap car n a besoin de trier nos données par date croissante pour les afficher dans le graphe
	private Date debut, fin;
	private int period;
	
	//Widgets
	private JFrame conteneur;
	private JPanel graphe;
	private JButton bPeriode, bPoles, bTableau;
	private JLabel lTitre;
	private CardLayout pSwapper;
	private JPanel parent;
	private Utilisateur utilisateur;
	
	///////////////////////////
	//////// METHODES /////////
	///////////////////////////
	
	public VueGraphe(Compte c, CardLayout swapper, JPanel parent, JFrame conteneur, Utilisateur u){
		
		this.conteneur = conteneur;
		this.utilisateur = u;
		this.compte = c;
		
		this.poles = new ArrayList<String>();
		this.donnees = new TreeMap<Date, TreeMap<String, Double> >();
		
		this.pSwapper = swapper;
		this.parent = parent;
		
		Calendar debutAnnee = new GregorianCalendar();
		debutAnnee.set(Calendar.DAY_OF_MONTH, 1);
		debutAnnee.set(Calendar.MONTH, Calendar.JANUARY);
		this.debut = debutAnnee.getTime();
		this.fin = new Date();
		this.period = PERIOD_MONTH;
		
		this.build();
		
		
	}

	private void build() {
		this.removeAll();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		JPanel pTitre = new JPanel();
		pTitre.setLayout(new FlowLayout());
		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
		this.lTitre = new JLabel("Principaux pôles de dépense du " + f.format(debut) + " au " + f.format(fin));
		pTitre.add(this.lTitre);
		this.add(pTitre);
		
		//Partie "dessin du Graphe"
		
		//Choix des trois pôles majeurs si besoin est
		if(this.poles.isEmpty())
			this.findMajorPoles();
		
		//Maintenant qu'on a les pôles on remplit les données du graphe
		this.fillData();
		
		//Puis on dessine le graphique (ce n'est pas la tâche la plus aisée)
		this.graphe = new Graphique(this.donnees);
		this.add(new JScrollPane(this.graphe, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		
		//Boutons en bas du Graphe
		JPanel pBoutons = new JPanel();
		pBoutons.setLayout(new FlowLayout());
		
		this.bPeriode = new JButton("Changer la période");
		this.bPeriode.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				@SuppressWarnings("unused")
				DialogChoixPeriode d = new DialogChoixPeriode();
			}
		});
		pBoutons.add(this.bPeriode);
		
		this.bPoles = new JButton("Choix des pôles de consommation");
		this.bPoles.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				@SuppressWarnings("unused")
				DialogChoixPoles d = new DialogChoixPoles();
			}
		});
		pBoutons.add(this.bPoles);
		
		this.bTableau = new JButton("Tableau");
		this.bTableau.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				pSwapper.show(parent, "Tableau");
			}
		});
		pBoutons.add(this.bTableau);
		
		this.add(pBoutons);
	}
	
	/**
	 * Les pôles de consommation ayant été attribués, découpe la période à afficher en intervalles et calcule les dépenses dans les trois pôles sur chacun des intervalles puis stocke le tout dans une vilaine TreeMap de TreeMap
	 */
	private void fillData(){
		this.donnees = new TreeMap<Date, TreeMap<String, Double> >();
		
		//Pour plus de commodité sur les modifications de dates, nos dates seront représentées par des GregorianCalendars (il est plus ardu et risqué de modifier des Date en plus leurs méthodes sont toutes obsolètes (deprecated)
		Calendar debut = new GregorianCalendar(), fin = new GregorianCalendar();
		debut.setTime(this.debut);
		fin.setTime(this.fin);
		
		Calendar di = debut, fi = (Calendar) debut.clone(); //Début et fin de l'intervalle i
		di.set(Calendar.HOUR_OF_DAY, 0);
		di.set(Calendar.MINUTE, 0);
		di.set(Calendar.SECOND, 0);
		di.set(Calendar.MILLISECOND, 0);
		
		do{
			fi.add(this.period, 1); //Si vous voulez savoir pourquoi j'utilise add (et pas set(period, get(period) + 1) ou encore roll) je vous conseille d'aller jeter un oeil aux notes dans la JavaDoc de la classe Calendar
			switch(this.period){
			case PERIOD_WEEK:
				fi.set(Calendar.DAY_OF_WEEK, fi.getFirstDayOfWeek()); //La aussi, merci Calendar, FirstDayOfWeek designera LUNDI en France, Dimanche aux US (cf la JavaDoc)... chapeau
				break;
			case PERIOD_MONTH:
				fi.set(Calendar.DAY_OF_MONTH, 1);
				break;
			case PERIOD_YEAR:
				fi.set(Calendar.DAY_OF_YEAR, 1);
				break;
			}
			fi.set(Calendar.HOUR_OF_DAY, 0);
			fi.set(Calendar.MINUTE, 0);
			fi.set(Calendar.SECOND, 0);
			fi.set(Calendar.MILLISECOND, 0);
			
			if(fi.compareTo(fin) > 0)
				fi = fin;
			
			//Initialisation de la TreeMap de l'intervalle i
			TreeMap<String, Double> hm = new TreeMap<String, Double>();
			Iterator<String> j = this.poles.listIterator();
			while(j.hasNext()){
				hm.put(j.next(), new Double(0));
			}
			this.donnees.put(di.getTime(), hm);
			
			//On itère sur les opérations ayant eu lieu entre entre di et fi
			Iterator<Operation> i = this.compte.getOperationsBetween(di.getTime(), fi.getTime()).listIterator();
			while(i.hasNext()){
				Operation op = i.next();
				if(this.poles.contains(op.getPoste())){ //Si l'opération est dans l'un des pôles de dépense on ajoute la valeur absolue de son montant au bon schmilblick
					this.donnees.get(di.getTime()).put(op.getPoste(), this.donnees.get(di.getTime()).get(op.getPoste()) + Math.abs(op.getMontant()));
				}
			}
			
			//Le début devient la fin et la fin deviendra la nouvelle fin au début du prochain tour de boucle... sauf si on sort parce que notre intervalle était le dernier
			di = (Calendar) fi.clone();
					
					
		}while(!fi.equals(fin));
		
		System.out.println(this.donnees);
		
	}
	
	/**
	 * Trouve les trois postes de dépenses majeurs entre les dates de début et de fin
	 */
	private void findMajorPoles() {
		TreeMap<String, Double> montantPoles = new TreeMap<String, Double>(); //Une TreeMap 'montant'->somme dépensée pour la période considérée
		
		List<Operation> opsSel = this.compte.getOperationsBetween(this.debut, this.fin);
		
		Iterator<Operation> i = opsSel.listIterator();
		while(i.hasNext()){
			Operation op = i.next();
			if(op.getMontant() <= 0){ //On ne fait pas de graphes sur les crédits
				if(montantPoles.containsKey(op.getPoste()))
					montantPoles.put(op.getPoste(), montantPoles.get(op.getPoste()) + Math.abs(op.getMontant()) );
				else
					montantPoles.put(op.getPoste(), Math.abs(op.getMontant()) );
			}
		}
		System.out.println(montantPoles);
		
		for(int j = 0; j < 3; j++){
			Entry<String, Double> e = getMaxEntry(montantPoles.entrySet());
			if(e != null){
				this.poles.add(e.getKey());
				montantPoles.remove(e.getKey());
			}
		}System.out.println(poles);
	}
	
	/**
	 * Retourne l'entrée ayant la plus grosse valeur dans l'ensemble de paires clef -> valeur passé en paramètre
	 * @param set
	 * @return
	 */
	private Entry<String, Double> getMaxEntry(Set<Entry<String, Double>> set){
		Entry<String, Double> ret = null;
		Iterator<Entry<String, Double> > j = set.iterator();
		Double currentMax = new Double(0);
		while(j.hasNext()){
			Entry<String, Double> e = j.next();
			if(e.getValue().compareTo(currentMax) >= 0){
				ret = e;
				currentMax = e.getValue();
			}
		}
		return ret;
	}
	
	/////////////////////////////////////////////////////////////
	//////// La boite de dialogue de choix de la période  ///////
	/////////////////////////////////////////////////////////////
	private class DialogChoixPeriode extends JDialog{
		private static final long serialVersionUID = 9030151802082481314L;
		
		JLabel lDu, lAu, lInt;
		JComboBox<String> cbInt;
		JFormattedTextField tfDu, tfAu;
		JButton bAfficher;
		
		public DialogChoixPeriode(){
			super(conteneur, "Choix de la période du Graphique", false);
			this.getContentPane().setLayout(new FlowLayout());
			
			this.lDu = new JLabel("Du ");
			this.getContentPane().add(this.lDu);
			
			try {
				this.tfDu = new JFormattedTextField(new MaskFormatter("##/##/####"));
				this.tfDu.setColumns(10);
				this.getContentPane().add(this.tfDu);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			this.lAu = new JLabel(" au ");
			this.getContentPane().add(this.lAu);
			
			try {
				this.tfAu = new JFormattedTextField(new MaskFormatter("##/##/####"));
				this.tfAu.setColumns(10);
				this.getContentPane().add(this.tfAu);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			this.lInt = new JLabel("  Intervalle de temps : ");
			this.getContentPane().add(this.lInt);
			
			String[] ints = {"Jour", "Semaine", "Mois", "Année"};
			this.cbInt = new JComboBox<String>(ints);
			this.getContentPane().add(this.cbInt);
			
			this.bAfficher = new JButton("Afficher");
			this.bAfficher.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					SimpleDateFormat parseur = new SimpleDateFormat("dd/MM/yyyy");
					try {
						debut = parseur.parse(tfDu.getText());
						fin = parseur.parse(tfAu.getText());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					period = cbInt.getSelectedIndex() == 0 ? PERIOD_DAY : (cbInt.getSelectedIndex() == 1 ? PERIOD_WEEK : (cbInt.getSelectedIndex() == 2 ? PERIOD_MONTH : PERIOD_YEAR) );
					poles = new ArrayList<String>();
					build();
					setVisible(false);
					
				}
			});
			
			this.getContentPane().add(this.bAfficher);
			
			this.setSize(600, 160);
			this.setLocationRelativeTo(null);
			this.setResizable(false);
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setVisible(true);
		}
		
	}
	
	/////////////////////////////////////////////////////////////
	////////// La boite de dialogue de choix des Poles  /////////
	/////////////////////////////////////////////////////////////
	private class DialogChoixPoles extends JDialog{
		private static final long serialVersionUID = 9030151802082481314L;
		
		JLabel lPoles, lPolesSel;
		JList<String> liPoles, liPolesSel;
		JButton bAfficher, bAdd, bRemove;
		
		List<String> mList, mListSel;
		
		public DialogChoixPoles(){
			super(conteneur, "Choix des Pôles à Afficher", false);
			
			mList = new ArrayList<String>(utilisateur.getPostesAsList());
			mList.addAll(utilisateur.getPostesCreditAsList());
			mListSel = new ArrayList<String>();
			
			this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
			
			JPanel pChoix = new JPanel();
			pChoix.setLayout(new BoxLayout(pChoix, BoxLayout.LINE_AXIS));
			
			JPanel pLeft = new JPanel();
			pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.PAGE_AXIS));
			
			lPoles = new JLabel("Pôles Disponibles");
			pLeft.add(lPoles);
			
			String[] spoles = toArray(mList);
			liPoles = new JList<String>(spoles);
			pLeft.add(new JScrollPane(liPoles));
			
			pChoix.add(pLeft);
			
			JPanel pCenter = new JPanel();
			pCenter.setLayout(new BoxLayout(pCenter, BoxLayout.PAGE_AXIS));
			
			bAdd = new JButton(">");
			bAdd.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(liPoles.getSelectedValue() != null){
						
						mList.remove(liPoles.getSelectedValue());
						mListSel.add(liPoles.getSelectedValue());
						
						String[] tmp = toArray(mList);
						liPoles.setListData(tmp);
						tmp = toArray(mListSel);
						liPolesSel.setListData(tmp);
						
						liPolesSel.setSelectedIndex(tmp.length - 1);
					}
				}
			});
			pCenter.add(bAdd);
			
			bRemove = new JButton("<");
			bRemove.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(liPolesSel.getSelectedValue() != null){
						
						mListSel.remove(liPolesSel.getSelectedValue());
						mList.add(liPolesSel.getSelectedValue());
						
						String[] tmp = toArray(mList);
						liPoles.setListData(tmp);
						tmp = toArray(mListSel);
						liPolesSel.setListData(tmp);
						
						liPoles.setSelectedIndex(mList.size()- 1);
					}
				}
			});
			pCenter.add(bRemove);
			
			pChoix.add(pCenter);
			
			JPanel pRight = new JPanel();
			pRight.setLayout(new BoxLayout(pRight, BoxLayout.PAGE_AXIS));
			
			lPolesSel = new JLabel("Pôles Sélectionnés");
			pRight.add(lPolesSel);
			
			liPolesSel = new JList<String>();
			pRight.add(new JScrollPane(liPolesSel));
			
			pChoix.add(pRight);
			
			this.getContentPane().add(pChoix);
			
			this.bAfficher = new JButton("Afficher Graphe");
			this.bAfficher.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					poles = mListSel;
					build();
					setVisible(false);
				}
			});
			this.getContentPane().add(bAfficher);
			
			this.setSize(800, 200);
			this.setLocationRelativeTo(null);
			this.setResizable(false);
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setVisible(true);
		
		}
		
		private String[] toArray(List<String> l){
			String[] a = new String[l.size()];
			Iterator<String> i = l.listIterator();
			int j = 0;
			while(i.hasNext())
				a[j++] = i.next();
			return a;
		}
	
	}
}
