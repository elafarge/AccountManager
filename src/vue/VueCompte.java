/**
 * JPanel décrivant les infos importantes à avoir sur un compte (solde, possibilité de faire un débit/crédit et dernières opérations + possibilité d'afficher toutes les opérations)
 */

/**
 * @author Etienne LAFARGE - Ecole Nationale Supérieure des Mines de Paris
 *
 */
package vue;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.MaskFormatter;

import modele.Compte;
import modele.Operation;
import modele.Utilisateur;

public class VueCompte extends JPanel  {
	private static final long serialVersionUID = -4829326353049156071L;
	///////////////////////////
	//////// ATTRIBUTS ////////
	///////////////////////////
	
	private Utilisateur utilisateur;
	private Compte compte;
	private List<Operation> displayedOps;
	
	private int sortIndex;
	private boolean global;
	
	private JFrame conteneur;
	
	//Widgets
	private JButton bDebit, bCredit, bSelOp, bDelOp, bGraph;
	private JLabel lNomCompte, lSolde1, lSolde2, lDisplay;
	private JPanel pMilieu, pTableau, pGraph, pNorth, pSouth;
	private JTable tOperations;
	private CardLayout pSwapper;
	
	///////////////////////////
	//////// METHODES /////////
	///////////////////////////
	
	public VueCompte(Compte c, JFrame conteneur, Utilisateur u) {
		this(c, conteneur, u, false);
	}
	
	public VueCompte(Compte c, JFrame conteneur, Utilisateur u, boolean global) {
		this.compte = c;
		this.conteneur = conteneur;
		this.utilisateur = u;
		this.global = global;
		
		//this.compte.addObserver(this); Pas besoin : un rafraichissement de compe entraine le rafraichissement de vueUtilisater toute entière
		
		this.build();
	}
	
	
	private void build(){
		this.removeAll();
		
		this.setLayout(new BorderLayout());
		
		//Construction de la partie Nord du Panel
		this.pNorth = new JPanel();
		this.pNorth.setLayout(new GridLayout(2,1));
		
		this.lNomCompte = new JLabel("Compte " + this.compte.getNom());
		this.pNorth.add(this.lNomCompte);
		
		//Construction de la partie médiane du Panel
		this.pMilieu = new JPanel();
		this.pSwapper = new CardLayout();
		this.pMilieu.setLayout(this.pSwapper);
		
		this.pTableau = new JPanel();
		this.pTableau.setLayout(new BoxLayout(this.pTableau, BoxLayout.PAGE_AXIS));
		
		JPanel pSolde = new JPanel();
		pSolde.setLayout(new FlowLayout());
		this.lSolde1 = new JLabel("Solde : ");
		this.lSolde2 = new JLabel( new DecimalFormat("0.00").format(this.compte.getSolde()) + "€");
		pSolde.add(this.lSolde1);
		pSolde.add(this.lSolde2);
		
		this.bDebit = new JButton("+ Debit");
		if(this.global)
			this.bDebit.setEnabled(false);
		else
			this.bDebit.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					@SuppressWarnings("unused")
					DialogAjouterOperation d = new DialogAjouterOperation(true);
				}
			});
		pSolde.add(this.bDebit);
		
		this.bCredit = new JButton("+ Credit");
		if(this.global)
			this.bCredit.setEnabled(false);
		else
			this.bCredit.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					@SuppressWarnings("unused")
					DialogAjouterOperation d = new DialogAjouterOperation(false);
				}
			});
		pSolde.add(this.bCredit);
		
		this.bDelOp = new JButton("Supprimer Opération");
		this.bDelOp.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int lineN = tOperations.getSelectedRow();
				
				if(lineN != -1){
					int option = JOptionPane.showConfirmDialog(null, 
						"Voulez-vous vous mettre le solde à jour lors de la suppression de cette opération (par exemple si c'est un débit de 60€, le solde augmentera de 60€ après suppression) ? En clair si vous voulez juste cacher cette opération cliquez sur non. Par contre si elle n'a vraiment jamais eu lieu ou a été annulée, cliquez sur oui.", 
						"Supprimer une opération", 
						JOptionPane.YES_NO_CANCEL_OPTION, 
						JOptionPane.QUESTION_MESSAGE);
					
					if(option == JOptionPane.YES_OPTION){
						displayedOps.get(lineN).getCompte().removeOperation(displayedOps.get(lineN), true);
						//utilisateur.notifyObservers(); //Parce que le solde total de l'utilisateur à changer et qu'on ne peut pas ajoute VueUtilisateur comme observateur de chaque compte sans déclencher un
					}else if(option == JOptionPane.NO_OPTION){
						displayedOps.get(lineN).getCompte().removeOperation(displayedOps.get(lineN), false);
					}
				}
			}
		});
		pSolde.add(this.bDelOp);
		
		this.pNorth.add(pSolde);
		
		this.pTableau.add(this.pNorth, BorderLayout.NORTH);
		
		//Construction du tableau
		
		this.tOperations = this.buildTable();
		this.sortIndex = 0;
		
		this.tOperations.getTableHeader().addMouseListener(new MouseAdapter() {
		      @Override
		      public void mouseClicked(MouseEvent mouseEvent) {
		        int index = tOperations.convertColumnIndexToModel(tOperations.columnAtPoint(mouseEvent.getPoint()));
		        
		        if(global)
		        	index--;
		        
		        if (index >= -1) {
		          switch(index){
		          case -1:
		        	  if(sortIndex == -1)
		        		  Collections.reverse(displayedOps);
		        	  else
		        		  Collections.sort(displayedOps, new Operation.CompCompte());
		        	  sortIndex = -1;
		        	  break;
		          case 1:
		        	  if(sortIndex == 1)
		        		  Collections.reverse(displayedOps);
		        	  else
		        		  Collections.sort(displayedOps, new Operation.CompMdp());
		        	  sortIndex = 1;
		        	  break;
		          case 2:
		        	  if(sortIndex == 2)
		        		  Collections.reverse(displayedOps);
		        	  else
		        		  Collections.sort(displayedOps, new Operation.CompMontant());
		        	  sortIndex = 2;
		        	  break;
		          case 3:
		        	  if(sortIndex == 3)
		        		  Collections.reverse(displayedOps);
		        	  else
		        		  Collections.sort(displayedOps, new Operation.CompPoste());
		        	  sortIndex = 3;
		        	  break;
		          case 4:
		        	  if(sortIndex == 4)
		        		  Collections.reverse(displayedOps);
		        	  else
		        		  Collections.sort(displayedOps, new Operation.CompBenef_creancier());
		        	  sortIndex = 4;
		        	  break;
		          case 5:
		        	  if(sortIndex == 5)
		        		  Collections.reverse(displayedOps);
		        	  else
		        		  Collections.sort(displayedOps, new Operation.CompSoldeAvant());
		        	  sortIndex = 5;
		        	  break;
		          case 6:
		        	  if(sortIndex == 6)
		        		  Collections.reverse(displayedOps);
		        	  else
		        		  Collections.sort(displayedOps, new Operation.CompSoldeApres());
		        	  sortIndex = 6;
		        	  break;
		          default:
		        	  if(sortIndex == 0)
		        		  Collections.reverse(displayedOps);
		        	  else
		        		  Collections.sort(displayedOps);
		        	  sortIndex = 0;
		          }
		          if(global)
		        	  tOperations.setModel(new OperationsModelGlobal(displayedOps));
					else
						tOperations.setModel(new OperationsModel(displayedOps));
		        }
		      }
		});
		
		this.pTableau.add(new JScrollPane(tOperations));
		
		
		//Construction de la partie Sud du Panel
		this.pSouth = new JPanel();
		this.pSouth.setLayout(new FlowLayout());
		
		this.lDisplay = new JLabel("Liste des 10 dernières opérations");
		this.pSouth.add(this.lDisplay);
		
		this.bSelOp = new JButton("Plus d'opérations");
		this.bSelOp.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				@SuppressWarnings("unused")
				DialogSelectionnerOperations d = new DialogSelectionnerOperations();
			}
		});
		this.pSouth.add(this.bSelOp);
		
		this.bGraph = new JButton("Graphes");
		this.bGraph.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pSwapper.show(pMilieu, "Graphe");
			}
		});
		this.pSouth.add(this.bGraph);
		
		this.pTableau.add(this.pSouth);
		
		this.pMilieu.add(this.pTableau, "Tableau");
		
		
		//Ajout du Graphe (dans une autre Classe pour ne pas alourdir encore plus le code de celle-ci)
		
		this.pGraph = new VueGraphe(this.compte, this.pSwapper, this.pMilieu, this.conteneur, this.utilisateur);
		
		this.pMilieu.add(this.pGraph, "Graphe");
		
		this.add(this.pMilieu);
		
		this.pSwapper.show(this.pMilieu, "Tableau");
	}
	
	private JTable buildTable() {		
		this.displayedOps = this.compte.getLastOperations(10);
		
		/*Object[][] data = new Object[this.displayedOps.size()][7];
		
		Iterator<Operation> i = this.displayedOps.listIterator();
		int k = 0;
		while(i.hasNext()){
			Operation o = i.next();
			SimpleDateFormat formatteur = new SimpleDateFormat("dd/MM/yyyy");
			data[k][0] = formatteur.format(o.getDate());
			data[k][1] = o.getMdp();
			data[k][2] = new DecimalFormat("0.00").format(o.getMontant()) + "€";
			data[k][3] = o.getPoste();
			data[k][4] = o.getBenef_creancier();
			data[k][5] = new DecimalFormat("0.00").format(o.getSoldeAvant()) + "€";
			data[k++][6] = new DecimalFormat("0.00").format(o.getSoldeAvant() + o.getMontant()) + "€";
		}*/
		JTable t;
		if(global)
			t = new JTable(new OperationsModelGlobal(this.displayedOps));
		else
			t = new JTable(new OperationsModel(this.displayedOps));
		
		JComboBox<String> cbEditMdP = new JComboBox<String>(compte.getMdPs());
		JComboBox<String> cbEditPole = new JComboBox<String>(utilisateur.getPostes());
		JFormattedTextField tfEditDate;
		try {
			tfEditDate = new JFormattedTextField(new MaskFormatter("##/##/####"));
			t.getColumn("Date").setCellEditor(new DefaultCellEditor(tfEditDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.getColumn("Payé par").setCellEditor(new DefaultCellEditor(cbEditMdP));
		t.getColumn("Poste").setCellEditor(new DefaultCellEditor(cbEditPole));
		
		return t;
	}
	
	/////////////////////////////////////////////////////////////
	///////// Les modèles et éditeurs relatifs au JTable ////////
	/////////////////////////////////////////////////////////////
	private class OperationsModel extends AbstractTableModel{
		private static final long serialVersionUID = 4802061572890934193L;
		
		protected List<Operation> ops;
		protected final String[] colsTitles = {"Date", "Payé par", "Montant", "Poste", "Bénéficiaire", "Solde avant", "Solde après"};
		
		public OperationsModel(List<Operation> ops){
			this.ops = ops;
		}
		
		@Override
		public int getColumnCount() {
			return colsTitles.length;
		}
		
		@Override
		public String getColumnName(int i){
			return this.colsTitles[i];
		}

		@Override
		public int getRowCount() {
			return this.ops.size();
		}

		@Override
		public Object getValueAt(int x, int y) {
			switch(y){
			case 0:
				SimpleDateFormat formatteur = new SimpleDateFormat("dd/MM/yyyy");
				return formatteur.format(this.ops.get(x).getDate());
			case 1:
				return this.ops.get(x).getMdp();
			case 2:
				return this.ops.get(x).getMontant();
			case 3:
				return this.ops.get(x).getPoste();
			case 4:
				return this.ops.get(x).getBenef_creancier();
			case 5:
				return this.ops.get(x).getSoldeAvant();
			case 6:
				return this.ops.get(x).getSoldeAvant() + this.ops.get(x).getMontant();
			}
			return null;
		}
		
		@Override
		public void setValueAt(Object ob, int x, int y){
			if(isCellEditable(x,y)){
				switch(y){
				case 0:
					SimpleDateFormat parseur = new SimpleDateFormat("dd/MM/yyyy");
					try {
						this.ops.get(x).setDate(parseur.parse((String) ob));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 1:
					this.ops.get(x).setMdp((String)ob);
					break;
				case 2:
					this.ops.get(x).setMontant(Double.parseDouble((String)ob));
					break;
				case 3:
					this.ops.get(x).setPoste((String)ob);
					break;
				case 4:
					this.ops.get(x).setBenef_creancier((String)ob);
					break;
				case 5:
					this.ops.get(x).setSoldeAvant(Double.parseDouble((String)ob));
					break;
				}
			}
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Class getColumnClass(int y){
			return String.class;
		}
		
		@Override
		public boolean isCellEditable(int x, int y){
			return y < this.colsTitles.length - 2;
		}
		
	}
	
	private class OperationsModelGlobal extends OperationsModel{
		private static final long serialVersionUID = 6173419085580998744L;
		
		private final String[] colsTitles2 = {"Compte", "Date", "Payé par", "Montant", "Poste", "Bénéficiaire", "Solde avant", "Solde après"};
		
		public OperationsModelGlobal(List<Operation> ops){
			super(ops);
		}
		
		@Override
		public int getColumnCount() {
			return colsTitles2.length;
		}
		
		@Override
		public String getColumnName(int i){
			return this.colsTitles2[i];
		}

		@Override
		public int getRowCount() {
			return this.ops.size();
		}

		@Override
		public Object getValueAt(int x, int y) {
			switch(y){
			case 0:
				return this.ops.get(x).getCompte().getNom();
			case 1:
				SimpleDateFormat formatteur = new SimpleDateFormat("dd/MM/yyyy");
				return formatteur.format(this.ops.get(x).getDate());
			case 2:
				return this.ops.get(x).getMdp();
			case 3:
				return this.ops.get(x).getMontant();
			case 4:
				return this.ops.get(x).getPoste();
			case 5:
				return this.ops.get(x).getBenef_creancier();
			case 6:
				return this.ops.get(x).getSoldeAvant();
			case 7:
				return this.ops.get(x).getSoldeAvant() + this.ops.get(x).getMontant();
			}
			return null;
		}
		
		@Override
		public void setValueAt(Object ob, int x, int y){
			if(isCellEditable(x,y)){
				switch(y){
				case 1:
					SimpleDateFormat parseur = new SimpleDateFormat("dd/MM/yyyy");
					try {
						this.ops.get(x).setDate(parseur.parse((String) ob));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 2:
					this.ops.get(x).setMdp((String)ob);
					break;
				case 3:
					this.ops.get(x).setMontant(Double.parseDouble((String)ob));
					break;
				case 4:
					this.ops.get(x).setPoste((String)ob);
					break;
				case 5:
					this.ops.get(x).setBenef_creancier((String)ob);
					break;
				case 6:
					this.ops.get(x).setSoldeAvant(Double.parseDouble((String)ob));
					break;
				}
			}
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Class getColumnClass(int y){
			return String.class;
		}
		
		@Override
		public boolean isCellEditable(int x, int y){
			return y < this.colsTitles.length - 2 && y > 0;
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////////
	/////// La boite de dialogue permettant de sélectionner une opération //////
	////////////////////////////////////////////////////////////////////////////
	private class DialogSelectionnerOperations extends JDialog implements ActionListener{
		private static final long serialVersionUID = -5307688663525995047L;
		
		ButtonGroup bg;
		JRadioButton rbNop, rbAll, rbPeriod;
		JLabel  lOp, lAu, lExcl;
		JFormattedTextField tfNbOp, tfDu, tfAu;
		JButton bGo;
		
		public DialogSelectionnerOperations(){
			super(conteneur, "Opérations bancaires à afficher ", false);
			
			this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
			
			this.bg = new ButtonGroup();
			
			//Afficher n opérations
			JPanel pNop = new JPanel();
			pNop.setLayout(new FlowLayout());
			
			this.rbNop = new JRadioButton("Afficher ");
			this.bg.add(rbNop);
			pNop.add(this.rbNop);
			
			this.tfNbOp = new JFormattedTextField(NumberFormat.getIntegerInstance());
			this.tfNbOp.setColumns(3);
			pNop.add(this.tfNbOp);
			
			this.lOp = new JLabel(" opérations à la fois.");
			pNop.add(this.lOp);
			
			this.getContentPane().add(pNop);
			
			//Afficher toutes les opérations
			JPanel pAll = new JPanel();
			pAll.setLayout(new FlowLayout());
			this.rbAll = new JRadioButton("Afficher toutes les opérations");
			this.bg.add(rbAll);
			pAll.add(this.rbAll);
			this.getContentPane().add(pAll);
			
			//Afficher les opérations pour une période donnée
			JPanel pPer = new JPanel();
			pPer.setLayout(new FlowLayout());
			
			this.rbPeriod = new JRadioButton("Afficher les opérations ayant eu lieu entre le ");
			this.bg.add(rbPeriod);
			pPer.add(this.rbPeriod);
			
			try {
				this.tfDu = new JFormattedTextField(new MaskFormatter("##/##/####"));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.tfDu.setColumns(10);
			pPer.add(this.tfDu);
			
			this.lAu = new JLabel(" (inclus) et le ");
			pPer.add(this.lAu);
			
			try {
				this.tfAu = new JFormattedTextField(new MaskFormatter("##/##/####"));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.tfAu.setColumns(10);
			pPer.add(this.tfAu);
			
			this.lExcl = new JLabel(" (exclus).");
			pPer.add(this.lExcl);
			
			this.getContentPane().add(pPer);
			
			JPanel pGo = new JPanel();
			pGo.setLayout(new FlowLayout());
			
			this.bGo = new JButton("Recherche");
			this.bGo.addActionListener(this);
			pGo.add(this.bGo);
			
			this.getContentPane().add(pGo);
			
			this.setSize(800, 160);
			this.setLocationRelativeTo(null);
			this.setResizable(false);
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setVisible(true);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			OperationsModel model = new OperationsModel(compte.getLastOperations(10));
			
			if(this.rbAll.isSelected()){
				List<Operation> op = compte.getOperations();
				if(global)
					model = new OperationsModelGlobal(op);
				else
					model = new OperationsModel(op);
				lDisplay.setText("Liste de toutes les opérations");
			}else if(this.rbPeriod.isSelected()){
				SimpleDateFormat parseur = new SimpleDateFormat("dd/MM/yyyy");
				List<Operation> op;
				
				try {
					Date d1 = parseur.parse(this.tfDu.getText()), d2 = parseur.parse(this.tfAu.getText());
					op = compte.getOperationsBetween(d1, d2);
					lDisplay.setText("Liste des opérations entre le " +parseur.format(d1)+ "et le" + parseur.format(d2));
				} catch (ParseException e) {
					op = compte.getLastOperations(10);
					lDisplay.setText("Liste des 10 dernières opérations");
					e.printStackTrace();
				}
				if(global)
					model = new OperationsModelGlobal(op);
				else
					model = new OperationsModel(op);
			}else{
				if(!this.tfNbOp.getText().isEmpty()){
					List<Operation> op;
					try {
						op = compte.getLastOperations(NumberFormat.getIntegerInstance().parse(this.tfNbOp.getText()).intValue());
						lDisplay.setText("Liste des " +Integer.toString(NumberFormat.getIntegerInstance().parse(this.tfNbOp.getText()).intValue())+ " dernières opérations");
					} catch (ParseException e) {
						op = compte.getLastOperations(10);
						lDisplay.setText("Liste des 10 dernières opérations");
						e.printStackTrace();
					}
					if(global)
						model = new OperationsModelGlobal(op);
					else
						model = new OperationsModel(op);
				}
			}
			tOperations.setModel(model);
			sortIndex = 0;
			
			this.setVisible(false);
		}
	}
	
	/////////////////////////////////////////////////////////////
	/////// La boite de dialogue d'ajout d'un débit/crédit //////
	/////////////////////////////////////////////////////////////
	private class DialogAjouterOperation extends JDialog{
		private static final long serialVersionUID = 1355722129989101154L;
		
		JLabel lDate, lMdP, lMontant, lPoste, lBeneficiaire;
		JComboBox<String> cbMdP, cbPole;
		JTextField tfBenef;
		JFormattedTextField tfDate, tfMontant;
		JButton bValider, bAjouterMode, bAjouterPoste;
		boolean debit;

		public DialogAjouterOperation(boolean d){
			super(conteneur, "Ajouter un " + (d?"débit":"crédit") + " sur le compte \"" + compte.getNom() + "\" ", false);
			
			this.debit = d;
			
			this.getContentPane().setLayout(new FlowLayout());
		
			this.lDate = new JLabel("Date (JJ/MM/AAAA) : ");
			this.getContentPane().add(this.lDate);
			
			try {
				this.tfDate = new JFormattedTextField(new MaskFormatter("##/##/####"));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SimpleDateFormat formatteur = new SimpleDateFormat("dd/MM/yyyy");
			this.tfDate.setText(formatteur.format(new Date()));
			this.tfDate.setColumns(10);
			this.getContentPane().add(this.tfDate);
			
			this.lMdP = new JLabel("  Mode de Paiement : ");
			this.getContentPane().add(this.lMdP);
			
			this.cbMdP = new JComboBox<String>(compte.getMdPs());
			this.getContentPane().add(this.cbMdP);
			
			this.bAjouterMode = new JButton("+");
			this.bAjouterMode.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					DialogEditStringList d = new DialogEditStringList(conteneur, compte.getMdPsAsList(), "Moyens de paiement pour le compte " + compte.getNom() , true);
					
					compte.setMdPs(d.resultat());
					
					//MaJ de la Combo Box
					getContentPane().remove(3); 
					cbMdP = new JComboBox<String>(compte.getMdPs());
					getContentPane().add(cbMdP, 3);
					getContentPane().validate();
				}
			});
			this.getContentPane().add(this.bAjouterMode);
			
			this.lMontant = new JLabel("  Montant : ");
			this.getContentPane().add(this.lMontant);
			
			this.tfMontant = new JFormattedTextField(NumberFormat.getNumberInstance());
			this.tfMontant.setColumns(8);
			this.getContentPane().add(this.tfMontant);
			
			this.getContentPane().add(new JLabel(" €    "));
			
			this.lPoste = new JLabel("  Motif : ");
			this.getContentPane().add(this.lPoste);
			
			this.cbPole = new JComboBox<String>(debit?utilisateur.getPostes():utilisateur.getPostesCredit());
			this.getContentPane().add(this.cbPole);
			
			this.bAjouterPoste = new JButton("+");
			this.bAjouterPoste.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					DialogEditStringList d = new DialogEditStringList(conteneur, debit?utilisateur.getPostesAsList():utilisateur.getPostesCreditAsList(), "Motifs de " + (debit?"débit":"crédit") , true);
					if(debit)
						utilisateur.setPostes(d.resultat());
					else
						utilisateur.setPostesCredit(d.resultat());
					
					//MaJ de la Combo Box
					getContentPane().remove(9); 
					cbPole = new JComboBox<String>(debit?utilisateur.getPostes():utilisateur.getPostesCredit());
					getContentPane().add(cbPole, 9);
					getContentPane().validate();
				}
			});
			this.getContentPane().add(this.bAjouterPoste);
			
			this.lBeneficiaire = new JLabel(debit?"  Identité du Bénéficiaire : ":"  Identité du Créancier : ");
			this.getContentPane().add(this.lBeneficiaire);
			
			this.tfBenef = new JTextField(12);
			this.getContentPane().add(this.tfBenef);
			
			this.bValider = new JButton("Ajouter " + (debit?"Débit":"Crédit") );
			this.bValider.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					SimpleDateFormat parseur = new SimpleDateFormat("dd/MM/yyyy");
					
					try {
						compte.addOperation(new Operation(compte, parseur.parse(tfDate.getText()), (String) cbMdP.getSelectedItem(), (debit?-1:1)*NumberFormat.getNumberInstance().parse(tfMontant.getText()).doubleValue(), (String) cbPole.getSelectedItem(), tfBenef.getText(), compte.getSolde() ));
					} catch (NumberFormatException | ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					setVisible(false);
				}
			});
			this.getContentPane().add(this.bValider);
			
			this.setSize(600, 160);
			this.setLocationRelativeTo(null);
			this.setResizable(false);
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setVisible(true);
		}
	}

}
