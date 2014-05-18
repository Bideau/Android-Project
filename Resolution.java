package com.FaivreBideauCharriere.projet_sudoku;


public class Resolution {

	
//	    public static void main(String[] args) {
//	        int[][] Matrice =Saisie_Matrice(args);
//	        Affiche_Matrice(Matrice);
//	        if (Resolution_Sudoku(0,0,Matrice))   
//	            Affiche_Matrice(Matrice);
//	        else
//	            System.out.println("Erreur, Aucune Solution Possible");
//	    }
	    
	    public static int[][] Retour_Resolution(int[][] P_Matrice) {
	    	
	        if (Resolution_Sudoku(0,0,P_Matrice))
	            return P_Matrice;
	        else
	        	P_Matrice[0][0]=0; // Indication pour une résolution impossible, il est possible de le changer
	            return P_Matrice;
	    }
	    

	    static boolean Resolution_Sudoku(int P_Ligne, int P_Colonne, int[][] P_Matrice) {
	        if (P_Ligne == 9) {
	            P_Ligne = 0;
	            if (++P_Colonne == 9)
	                return true;
	        }
	        if (P_Matrice[P_Ligne][P_Colonne] != 0)  // skip filled cells
	            return Resolution_Sudoku(P_Ligne+1,P_Colonne,P_Matrice);

	        for (int L_Index = 1; L_Index <= 9; ++L_Index) {
	            if (Verification(P_Ligne,P_Colonne,L_Index,P_Matrice)) {
	                P_Matrice[P_Ligne][P_Colonne] = L_Index;
	                if (Resolution_Sudoku(P_Ligne+1,P_Colonne,P_Matrice))
	                    return true;
	            }
	        }
	        P_Matrice[P_Ligne][P_Colonne] = 0;
	        return false;
	    }

	    static boolean Verification(int P_Ligne, int P_Colonne, int P_Valeur, int[][] P_Matrice) {
	        for (int L_Index = 0; L_Index < 9; ++L_Index)  // Pour les lignes
	            if (P_Valeur == P_Matrice[L_Index][P_Colonne])
	                return false;

	        for (int L_Index = 0; L_Index < 9; ++L_Index) // Pour les colonnes
	            if (P_Valeur == P_Matrice[P_Ligne][L_Index])
	                return false;

	        int L_Ligne_Debut_Region = (P_Ligne / 3)*3;
	        int L_Colonne_Debut_Region = (P_Colonne / 3)*3;
	        for (int L_Ligne_Region = 0; L_Ligne_Region < 3; ++L_Ligne_Region) // box
	            for (int L_Colonne_Region = 0; L_Colonne_Region < 3; ++L_Colonne_Region)
	                if (P_Valeur == P_Matrice[L_Ligne_Debut_Region+L_Ligne_Region][L_Colonne_Debut_Region+L_Colonne_Region])
	                    return false;

	        return true;
	    }

	    static int[][] Saisie_Matrice(String[] args) {
	        int[][] L_Matrice = new int[9][9]; 
	        for (int L_Indice = 0; L_Indice < args.length; ++L_Indice) {
	            int L_Ligne = Integer.parseInt(args[L_Indice].substring(0,1));
	            int L_Colonne = Integer.parseInt(args[L_Indice].substring(1,2));
	            int L_Valeur = Integer.parseInt(args[L_Indice].substring(2,3));
	            L_Matrice[L_Ligne][L_Colonne] = L_Valeur;
	        }
	        return L_Matrice;
	    }

	    static void Affiche_Matrice(int[][] P_Matrice) {
	        for (int L_Ligne = 0; L_Ligne < 9; ++L_Ligne) {
	            if (L_Ligne % 3 == 0)
	                System.out.println(" -----------------------");
	            for (int L_Colonne = 0; L_Colonne < 9; ++L_Colonne) {
	                if (L_Colonne % 3 == 0) System.out.print("| ");
	                System.out.print(P_Matrice[L_Ligne][L_Colonne] == 0
	                                 ? " "
	                                 : Integer.toString(P_Matrice[L_Ligne][L_Colonne]));

	                System.out.print(' ');
	            }
	            System.out.println("|");
	        }
	        System.out.println(" -----------------------");
	    }

	}
