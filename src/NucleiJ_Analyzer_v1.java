import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class NucleiJ_Analyzer_v1 implements PlugInFilter 
{
	// Constants **************************************************************	
	public static String PIXEL_SIZE;		//fuer min Partikelgroesse
	public static int AUFLOESUNG_SLIDER; 	//fuer die aufloesung der heatmap
	public static boolean CROP_CHECKBOX;		//var ob bild zugeschnitten werden soll
	public static boolean HEATMAP_CHECKBOX;	//var ob heatmap erstellt werden soll
	public static boolean EXPORT_PIC_CHECKBOX;	//var ob markierter schnitt exportiert werden soll
	public static boolean EXPORT_RESULTS_CHECKBOX;	 //var ob Results exportiert werden sollen

	//Create Objektes
	StringAdder summaryStack = new StringAdder();
	StringTransfer resultStack = new StringTransfer();
	Timestamp today = new Timestamp();
	MeasureSettings settings = new MeasureSettings();
	PictureAnalyzer simplePixelAnalysis = new PictureAnalyzer();
	StringTransfer path = new StringTransfer();
	StringTransfer file = new StringTransfer();
	Heatmap heatmap = new Heatmap();
	Exporter startExporter = new Exporter();
	
    public int setup(String arg, ImagePlus imp) 
    {
    	//Beim Start wird kein geladenes Bild benoetigt
    	return NO_IMAGE_REQUIRED;
    }
    
    
	public void run(ImageProcessor original_alt) 
	{
				
		//Pfad Abfrage
		String choosenDirectory = "C:\\Users\\Stefan\\Desktop\\Medizin Projekt\\Bilder\\stapel\\";
		choosenDirectory = IJ.getString("Enter the path of the scan:", choosenDirectory);
		path.setValue(choosenDirectory);
		
		//Benutzeroberflaeche und Radioboxen
		String radiobox = initGraphics();
		if (radiobox == null) { return; }		//Error Checker

		
		//Stapelfunktion!!
		int gefundeneneElemente = 0;
		File folder = new File(path.getValue());
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) 
		{
		  if (listOfFiles[i].isFile())
		  {
			
			String dateiname = listOfFiles[i].getName();
		    if (dateiname.endsWith("jpg") == true)
		    {
		    	gefundeneneElemente++;
		    	
		    	file.setValue(dateiname);
		    	String pfad = path.getValue() + dateiname;
		    	System.out.println("Derzeit wird folgende Datei verarbeitet:" + pfad);
		    	
		    	//ist Scan in x10 oder x40 Aufloesung? -> Umrechnungsfaktor px=um
				boolean x10 = file.getValue().toLowerCase().contains("x10");
				double distance = settings.selectMagnificationAutomatically(pfad, x10);
		    			
				//gewaehltes Bild automatisch laden
				System.out.println("Diese Bild wird geoffnet:\n" + pfad);
				ImagePlus imp = IJ.openImage(pfad);
				imp.unlock();
				imp.show();

		    	//set Measurements Properties
		    	settings.setMeasurementProporties(imp, distance);
		    			
				//set ROI? - UP zum zuschneiden oeffnen
				setROI(imp);
				
		    	//Variablen usw erstellen
				ImageProcessor original = imp.getProcessor();
				int w = original.getWidth();
			    int h = original.getHeight();
			    ImageProcessor copy = original.duplicate();
			    ImageProcessor sicherung = original.duplicate();
			    
			    //Neues 8-Bit Bild erstellen fuer Heatmap
			    ImagePlus heatmapTmp = IJ.createImage("Heatmap", "8-bit", w, h, 1);
				ImageProcessor heatmap_ip = heatmapTmp.getProcessor();
			    
				
				//Hauptprozess zum Erkennen der Zellkerne
				startImageProcessingActivity(original, copy, sicherung, heatmapTmp, imp, heatmap_ip, x10, radiobox, w, h);
				
				//Ausgabe und Ende des Programms 
			    //IJ.showMessage("Es wurden" ,pixelanzahl+ " Pixel gefunden!");
			    System.out.print("\n\nScan verarbeitet\n");
		    
			   //Ende des HP
		    }
		    
		  }
		  else 
		  {
		    //System.out.println("Error");
			  
		  }
		   
		}
		
		//Alle Fenster schlieen
		if (IJ.isResultsWindow() == true)		//Error Handling
		{
			IJ.selectWindow("Results");
			IJ.run("Close");
			
		}	
		startExporter.summary(summaryStack.getString(), path.getValue(), today.getCurrentTimeStamp());
		
		System.out.println("\nAnzahl der gefundenen Elemente: " + gefundeneneElemente );	
		
		System.out.println(resultStack.getValue());
		
		System.exit(0);
  	}	


	//Particle Analyzer mit geaenderten Parametern starten, weitere Werte berechnen und Ausgabe starten
	public void startParticleAnalyzer(ImagePlus markiert, String radiobox) 
	{
		//Particle Analyzer parametrisieren und ausfuehren:
		IJ.run("Set Measurements...", "area standard centroid perimeter bounding shape redirect=None decimal=3");
		String befehl = "size=$-Infinity pixel circularity=0.00-1.00 show=% ";	//display
		
		String endbefehl = befehl.replace("$", PIXEL_SIZE);
		IJ.run("Analyze Particles...", endbefehl.replace("%", radiobox));	    
	    
	    System.out.print("Particle Analyzer wurde ausgefuehrt...");
	    
	    //Resultate auslesen:
	  	ResultsTable alt = Analyzer.getResultsTable();	
	  	ResultsTable rt = (ResultsTable)alt.clone();
	  	
	  	int counter = 0;
	  	counter = rt.getCounter();
	  	
	    //Erstelle Arrays fuer alle Messwerte
	  	double[] area = new double[counter];
	  	double[] roundness = new double[counter];
	  	double[] xCoordinate = new double[counter];
	  	double[] yCoordinate = new double[counter];
	  	double[] perim = new double[counter];
	  	double[] bx = new double[counter];
	  	double[] width = new double[counter];
	  	double[] height = new double[counter];
	  	double[] circ = new double[counter];
	  	double[] ar = new double[counter];
	  	double[] solidity = new double[counter];
	  	
	  	//X	Y	Perim.	BX	BY	Width	Height	Circ.	AR	Round	Solidity
	  	
	  	double area_max = 0;
	  	double area_min = 999999999;
	  	double area_all = 0;
	  	double area_arith = 0;
		int found_particles = 0;
	  	
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		
		//funktioniert noch nicht
		DecimalFormat nr = new DecimalFormat("######.#");
		nr.setRoundingMode(RoundingMode.HALF_UP);
		
		
		//STringBuffer erstellen, in diesen werden nun alle Results gespeichert
		StringBuffer resultzeile = new StringBuffer();
		resultzeile.append("Nummer \t\tX \tY \tPerim.\t BX\t BY\t Width\t Height\t Circ.\t AR\t Round\t Solidity\n");
		
		if (EXPORT_RESULTS_CHECKBOX == true)
		{
			for (int x = 0; x <= counter-1; x++)
		  	{
		  		//System.out.println(x + "\n");
		  		//Werte in Variablen laden
		  		area[x] = rt.getValue("Area", x);
				roundness[x] = rt.getValue("Round", x);
				xCoordinate[x] = rt.getValue("X", x);
				yCoordinate[x] = rt.getValue("Y", x);
				perim[x] = rt.getValue("Perim.", x);
				bx[x] = rt.getValue("BX", x);
				width[x] = rt.getValue("Width", x);
				height[x] = rt.getValue("Height", x);
				circ[x] = rt.getValue("Circ.", x);
				ar[x] = rt.getValue("AR", x);
				solidity[x] = rt.getValue("Solidity", x);
				
				//Diese Variablen in String speichern
				
				
				resultzeile.append(nr.format(x+1) + "\t");
				resultzeile.append(df.format(area[x]) + "\t");
				resultzeile.append(df.format(xCoordinate[x]) +"\t\t");
				resultzeile.append(df.format(yCoordinate[x]) + "\n");
				
			
				
				//resultStack.setString(rt.toString() + "\n" );
				
				/*
				//Diesen in File schreiben
				if (x % 1000 == 0)
				{
					writeResults2File();
					resultStack.setString("reset");
				}
				*/
		  	}
			resultStack.setValue(resultzeile.toString());
		  	
		}
		else
		{
			for (int x = 0; x <= counter-1; x++)
		  	{
		  		//Werte in Variablen laden
		  		area[x] = rt.getValue("Area", x);
				roundness[x] = rt.getValue("Round", x);
				
		  	}
		  	
		}
		
	  	
	  	for (int x = 0; x <= counter-1; x++)
	  	{
	  		/*
	  		//Null setzen
	  		area[x] = 0;
			roundness[x] = 0;
			xCoordinate[x] = 0;
			yCoordinate[x] = 0;
			perim[x] = 0;
			bx[x] = 0;
			width[x] = 0;
			height[x] = 0;
			circ[x] = 0;
			ar[x] = 0;
			solidity[x] = 0;
			*/
			
	  		//Berechnungen fuer Area:
	  	   	if (area[x] < area_min)
	  	   	{
	  	   		area_min = area[x];
	  	   	}
	  	   	else if (area[x] > area_max)
	  	   	{
	  	   		area_max = area[x];
	  	   	}
	  	   	area_all += area[x];
	  	   	
	  	   	//ovale Partikel zaehlen
	  	   	if (roundness[x] > 0.05 && roundness[x] < 0.4)
		   	{
		   		//Ausgabe der Anzahl der ovalen Partikel:
		   		found_particles++;
		   	}
	  	   	
	  	}
	  	
	  	//Arithmetisches Mittel berechnen
	  	area_arith = area_all / counter;	
		
		//Median berechnen:
		Arrays.sort(area);
		double median = 0;
		if (area.length % 2 == 0)
		{
		    median = ((double)area[area.length/2] + (double)area[area.length/2 - 1])/2;
		}
		else
		{
		    median = (double) area[area.length/2];
		}
		
	  	outputCellnucleiInfo(counter, area_all, area_min, area_max, area_arith, median, found_particles);
		
	  	//resultStack.setString("reset");
	  	//resultStack.setString(rt.toString() );
	  	
		rt.show("Results");
		IJ.run("Clear Results", "");
	}
	
	
	

	

	public void startImageProcessingActivity(ImageProcessor original, ImageProcessor copy, ImageProcessor sicherung, ImagePlus heatmapTmp, 
			ImagePlus imp, ImageProcessor heatmap_ip, boolean x10, String radiobox, int w, int h)
	{
		//Zellkerne erkennen und Fehlerpixel ignorieren
		simplePixelAnalysis.detectCellPixels(original, w, h);
	    simplePixelAnalysis.ignoreSinglePixels(original, copy, x10, w, h);
	        
	    //Image fuer Kommandos vorbereiten
	    ImagePlus markiert = IJ.getImage();
	    markiert.unlock();
	    
	    //Maske erstellen, Loecher ausfuellen, Filter anwenden
	    createMask(markiert, x10, radiobox, w, h);
	    ImageProcessor maske = markiert.getProcessor();
	    ImageProcessor heatmapMaske = maske.duplicate();

	    //Maske ueber Originalbild legen, Zellkerne Rot markieren
	    simplePixelAnalysis.addMasktoOriginal(maske, sicherung, w, h);    
	    
	    //Ergebnis anzeigen & als neue Datei speichern,
	    imp.updateAndRepaintWindow();
	    startExporter.heatmap(imp, file.getValue(), path.getValue(), EXPORT_PIC_CHECKBOX);
	    
	    //Heatmap erstellen
	    if (HEATMAP_CHECKBOX == true) {   heatmap.create(file.getValue(), path.getValue(), AUFLOESUNG_SLIDER, heatmapTmp, heatmapMaske, heatmap_ip, w, h);   }   
	    
	    //es wurden keine Aenderungen vorgenommen, -> "wollen Sie speichern" umgehen
	    imp.changes = false;
	    
	    //Bild schliessen
	    imp.close();
	    //Ende des Erkennungs Prozesses
		
	}

	


	public void setROI(ImagePlus imp)
	{
		if (CROP_CHECKBOX == true)		//Bild wird zugeschnitten
    	{
			CROP_CHECKBOX = false;
    		
    		do
    		{
    			//Bild oeffnen, var setzen dass bild bereits offen ist
        		imp.updateAndRepaintWindow();
        		
        		new WaitForUserDialog("Information", "Please set a rectangular\nROI and press OK").show();
        		
        		Roi roi = imp.getRoi();
        		if (roi instanceof Roi)
        		{ 
        		    IJ.run(imp, "Crop", "");
      		        imp.updateAndDraw();
      		        
      		        CROP_CHECKBOX = true;
       			}
    		}while (CROP_CHECKBOX==false);
    		
    		System.out.print("\n\nROI set:");
    	}
	}

	public String initGraphics()
	{
		GenericDialog gd = new GenericDialog("User Interface");
		gd.addMessage("Cell Counting Program (Stefan Erben v35):\n***********************************************");
		
		gd.addNumericField("min. particle size [px]:", 4, 1);
		String[] items = {"Nothing", "Outlines", "Masks"};
		gd.addRadioButtonGroup("Which result should be displayed?", items, 2, 1, "Nothing");	
		
		gd.addCheckbox("set ROI?", false);
		gd.addCheckbox("Export marked Scan", false);
		gd.addCheckbox("Export Results", false);
		gd.addCheckbox("create Heatmap?", false);	

		gd.addSlider("Quality", 1, 100, 60);
		gd.addHelp("http://imagej.nih.gov/ij");
		
		gd.showDialog();
		if (gd.wasCanceled())
            return null;
		
		double pixeldouble = gd.getNextNumber();		
		PIXEL_SIZE = String.valueOf(pixeldouble);
		
		CROP_CHECKBOX = gd.getNextBoolean();
		EXPORT_PIC_CHECKBOX  = gd.getNextBoolean();
		EXPORT_RESULTS_CHECKBOX = gd.getNextBoolean();
		HEATMAP_CHECKBOX = gd.getNextBoolean();

		double aufloesungdouble = (double)gd.getNextNumber();
		AUFLOESUNG_SLIDER = (int) aufloesungdouble;
		
		String radio = gd.getNextRadioButton();
		
		return radio;	
	}

	//Maske erstellen
	public void createMask(ImagePlus markiert, boolean x10, String radiobox, int w, int h)
	{
		IJ.run(markiert, "Multiply...", "value=5");
	    
		Prefs.blackBackground = false;    
		IJ.run(markiert, "Make Binary", "");	    
	    IJ.run(markiert, "Fill Holes", "");

	    //Erben Analyzer starten
	    startParticleAnalyzer(markiert, radiobox);  
	        
	    //Results exportieren
	    if (EXPORT_RESULTS_CHECKBOX == true)
	    {
	    	startExporter.Results(resultStack.getValue(), file.getValue(), path.getValue() );
	    	
	    	String resultsFilename = file.getValue() + "_Results.txt";		//Neuen Filenamen festlegen
	    	
	    	String exportResulttable = path.getValue() + "\\" + resultsFilename;
		    
	    	IJ.saveAs("Results", exportResulttable);
		    System.out.print("\n\nMesswerte als File (.txt) exportieren...\n");
		    System.out.print(exportResulttable);
	    }
	    
	    //Bild wieder entsperren und zurueck invertieren
	    markiert.unlock();
	    
	    //Invertieren oder nicht?
		ImageProcessor invertieren = markiert.getProcessor();
		int bwZaehler = 0;
		for (int x = 0; x <= w; x++) 
		{
			for (int y = 0; y <= h; y++) 
			{
				int p = invertieren.getPixel(x, y);
				if (p == 0)
				{
					//Zellkerne zaehlen
					bwZaehler++;					
				}								
			}
		}
		//Fehler verhindern, da Bild vom externen Plugin invertiert werden kann
		int gesamtzaehler = w*h;
		if (bwZaehler < (gesamtzaehler/2))
		{
			IJ.run(markiert, "Invert", ""); 
		}
	  
	    //Filter anwenden und wieder zu RGB Bild zurueckwandeln
		if (x10 == true)
		{
			IJ.run(markiert, "Median...", "radius=1");	
		}
		else
		{
			IJ.run(markiert, "Median...", "radius=3");
		}
		
	    IJ.run(markiert, "RGB Color", "");
	    
	    System.out.print("\n\nMaske wurde erstellt...");
	    
	    return;
	}

	
	//Ausgabe der Zellkern Informationen
	public void outputCellnucleiInfo(double counter, double area_all, double area_min, double area_max, double area_arith, double median, double found_particles)
	{
		//Titel des Scans: originalFilename
		// anzahl der _ (30 - lenght) / 2)
		// for 1 bis anzahl der _, ausgeben *
		
		String ueberschrift = "";
		int lenghtFilename = file.getValue().length();
		if ((50 -lenghtFilename) % 2 == 0)
		{
			for (int k = 0; k < ((50 - lenghtFilename) / 2); k++)
			{
				ueberschrift = ueberschrift + "_";
			}
			ueberschrift = ueberschrift + file.getValue();
			for (int k = 0; k < ((50 - lenghtFilename) / 2); k++)
			{
				ueberschrift = ueberschrift + "_";
			}
		}
		else
		{
			for (int k = 0; k < (((50 - lenghtFilename - 1) / 2)+1); k++)		
			{
				ueberschrift = ueberschrift + "_";
			}
			ueberschrift = ueberschrift + file.getValue();
			for (int k = 0; k < ((50 - lenghtFilename - 1) / 2); k++)
			{
				ueberschrift = ueberschrift + "_";
			}
		}
		int intcounter = (int) counter;
		counter = 0;
		
		//Ausgabe ImageJ-LOG
		IJ.log("\n\n" + ueberschrift);
		IJ.log("Founded nuclei:\t\t\t\t" + intcounter);
		IJ.log("Additional measured values:");
		  	
		if (IJ.getLog() != null)
		{
			System.out.println(IJ.getLog()); 
		}else
		{
			System.out.println("ERROR no Log founded"); 
		}
		  	
			  	
		//Ausgabe in String -> Summary-File
		String summaryString = "";
		summaryString = summaryString + "\n\n" + ueberschrift + "\nFounded nuclei:\t\t\t\t" + intcounter + "\nAdditional measured values:\n" ;
		intcounter = 0;	  	
			  	
		//Werte auf 3 Kommastellen runden und anzeigen
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		 	
		//Ausgaben in einem ImageJ Log Fenster / speichern in String:
		IJ.log("Total area of all nuclei:\t" + df.format(area_all) + " um2");		//Ausgabe der Gesamtflaeche aller Zellkerne (auf 3 Kommastellen genau)
		summaryString = summaryString + "Total area of all nuclei:\t" + df.format(area_all) + " um2\n";
			  	
		IJ.log("Smallest cell nucleus:\t\t" + df.format(area_min) + " um2");	//Ausgabe der Flaeche des kleinsten gefunden Zellkerns (auf 3 Kommastellen genau)
		summaryString = summaryString + "Smallest cell nucleus:\t\t" + df.format(area_min) + " um2\n";	//Ausgabe der Flaeche des kleinsten gefunden Zellkerns (auf 3 Kommastellen genau)
			  	
		IJ.log("largest cell nucleus:\t\t" + df.format(area_max) + " um2");		//Ausgabe der Flaeche des groessten gefunden Zellkerns (auf 3 Kommastellen genau)
		summaryString = summaryString + "largest cell nucleus:\t\t" + df.format(area_max) + " um2\n";		//Ausgabe der Flaeche des groessten gefunden Zellkerns (auf 3 Kommastellen genau)
				
		IJ.log("Arithmetic mean area:\t\t" + df.format(area_arith) + " um2");	//Ausgabe des arithmetishen Mittels aller Zellkernflaechen (auf 3 Kommastellen genau)
		summaryString = summaryString + "Arithmetic mean area:\t\t" + df.format(area_arith) + " um2\n";	//Ausgabe des arithmetishen Mittels aller Zellkernflaechen (auf 3 Kommastellen genau)
			  	
		IJ.log("Median area:\t\t\t\t" + df.format(median) + " um2");				//Ausgabe des Medianwerts aller Zellkernfleachen (auf 3 Kommastellen genau)
		summaryString = summaryString + "Median area:\t\t\t\t" + df.format(median) + " um2\n";				//Ausgabe des Medianwerts aller Zellkernfleachen (auf 3 Kommastellen genau)
			  	
		IJ.log("oval cell nucleus:\t\t\t" + found_particles);		//Ausgabe der gefundenen ovalen Zellkernen
		summaryString = summaryString + "oval cell nucleus:\t\t\t" + found_particles +"\n";		//Ausgabe der gefundenen ovalen Zellkernen
		
		//An StringStack uebergeben
		summaryStack.appendString(summaryString);
		
		return;
	}
	
	//UP zum exportieren des rot markierten Scans
	
	
	
	
}

