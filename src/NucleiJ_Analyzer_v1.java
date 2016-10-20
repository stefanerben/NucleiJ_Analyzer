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

import classes.*;

public class NucleiJ_Analyzer_v1 implements PlugInFilter 
{
	// Constants **************************************************************	
	public static String PIXEL_SIZE;		//fuer min Partikelgroesse
	public static int AUFLOESUNG_SLIDER; 	//fuer die aufloesung der heatmap
	public static boolean CROP_CHECKBOX;		//var ob das bild zugeschnitten werden soll
	public static boolean HEATMAP_CHECKBOX;	//var ob heatmap erstellt werden soll
	public static boolean EXPORT_PIC_CHECKBOX;	//var ob markierter schnitt exportiert werden soll
	public static boolean EXPORT_RESULTS_CHECKBOX;	 //var ob Results exportiert werden sollen

	//Create Objektes
	StringAdder summaryStack = new StringAdder();
	StringAdder csvSummaryStack = new StringAdder();
	StringTransfer resultStack = new StringTransfer();
	Timestamp today = new Timestamp();
	MeasureSettings settings = new MeasureSettings();
	PictureAnalyzer simplePixelAnalysis = new PictureAnalyzer();
	StringTransfer path = new StringTransfer();
	StringTransfer file = new StringTransfer();
	Heatmap heatmap = new Heatmap();
	Exporter startExporter = new Exporter();
	PictureCharacteristics properties = new PictureCharacteristics();
	
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

		//einen Ordner erstellen:
		startExporter.setnewDirectoryname("Output");
		boolean success = new File(path.getValue() + startExporter.getnewDirectoryname() ).mkdirs();
		if (!success)
		{
			// Directory creation failed
			System.out.println("Error beim Verzeichnis erstellen!");
		}

		csvSummaryStack.appendString("csvHeader");
		//csvSummaryStack.appendString("gesamte Gewebeflaeche [um2];Total area of all nuclei [um2];Zellkernflaeche in %;Zellkerne / mm2;Arithmetic Perimeter [um];Smallest cell nucleus [um2];largest cell nucleus [um2];Arithmetic mean area [um2];Median area [um2];oval cell nucleus;\n");

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
		    if (dateiname.endsWith("tif") == true)
		    {
		    	gefundeneneElemente++;
		    	
		    	file.setValue(dateiname);
		    	String pfad = path.getValue() + dateiname;
		    	System.out.println("Derzeit wird folgende Datei verarbeitet:" + pfad);
		    	
		    	//ist Scan in x10 oder x40 Aufloesung? -> Umrechnungsfaktor px=um
				boolean x10 = file.getValue().toLowerCase().contains("x10");
				double distance = settings.selectMagnificationAutomatically(pfad, x10);
				properties.setMagnification(distance);
		    			
				//gewaehltes Bild automatisch laden
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
			    System.out.print("\n\n############################################################\n");
		    
			   //Ende des HP
		    }
		    
		  }

		   
		}
		
		//Alle Fenster schlieen
		if (IJ.isResultsWindow() == true)		//Error Handling
		{
			IJ.selectWindow("Results");
			IJ.run("Close");
			
		}	
		startExporter.summary(summaryStack.getString(), path.getValue(), today.getCurrentTimeStamp());

		startExporter.csvSummary(csvSummaryStack.getString(), path.getValue());
		
		System.out.println("\nAnzahl der gefundenen Elemente: " + gefundeneneElemente );

		System.exit(0);
  	}


	//Particle Analyzer mit geaenderten Parametern starten, weitere Werte berechnen und Ausgabe starten
	public void startParticleAnalyzer(ImagePlus markiert, String radiobox)
	{
		//Particle Analyzer parametrisieren und ausfuehren:
		System.out.print("Particle Analyzer wurde ausgefuehrt...");

		IJ.run("Set Measurements...", "area standard centroid perimeter bounding shape redirect=None decimal=3");
		String befehl = "size=$-Infinity pixel circularity=0.00-1.00 show=% ";	//display

		String endbefehl = befehl.replace("$", PIXEL_SIZE);
		IJ.run("Analyze Particles...", endbefehl.replace("%", radiobox));

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
		double[] by = new double[counter];
		double[] width = new double[counter];
	  	double[] height = new double[counter];
	  	double[] circ = new double[counter];
	  	double[] ar = new double[counter];
	  	double[] solidity = new double[counter];
	  	
	  	//X	Y	Perim.	BX	BY	Width	Height	Circ.	AR	Round	Solidity
	  	
	  	double area_max = 0;
	  	double area_min = 999999999;
	  	double area_all = 0;
		double perim_all = 0;
	  	double area_arith = 0;
		double perim_arith = 0;
		int found_particles = 0;
	  	
		DecimalFormat d3 = new DecimalFormat("#.###");

		d3.setRoundingMode(RoundingMode.HALF_UP);
		DecimalFormat d1 = new DecimalFormat("#.##");
		d1.setRoundingMode(RoundingMode.HALF_UP);
		
		//Zahlenformate und Kommastellendefiniton
		DecimalFormat nr = new DecimalFormat( "00000" );
		DecimalFormat n13 = new DecimalFormat( "0.000" );
		DecimalFormat n32 = new DecimalFormat( "000.00" );

		//StringBuffer erstellen, in diesen werden nun alle Results gespeichert
		StringBuffer resultzeile = new StringBuffer();
		resultzeile.append("Nummer\tArea\tPerim.\tRound\tWidth\tHeight\tX\t\t\tY\t\tCirc.\tSolidity\n");
		
		if (EXPORT_RESULTS_CHECKBOX == true)
		{
			for (int x = 0; x <= counter-1; x++)
		  	{
		  		area[x] = rt.getValue("Area", x);
				roundness[x] = rt.getValue("Round", x);
				xCoordinate[x] = rt.getValue("X", x);
				yCoordinate[x] = rt.getValue("Y", x);
				perim[x] = rt.getValue("Perim.", x);
				width[x] = rt.getValue("Width", x);
				height[x] = rt.getValue("Height", x);
				circ[x] = rt.getValue("Circ.", x);
				ar[x] = rt.getValue("AR", x);
				solidity[x] = rt.getValue("Solidity", x);
				
				//Diese Variablen in String speichern
				resultzeile.append(nr.format(x+1) + "\t");
				resultzeile.append(d3.format(area[x]) + "\t");

				resultzeile.append(d3.format(perim[x]) + "\t");
				resultzeile.append(n13.format(roundness[x]) + "\t");

				resultzeile.append(d3.format(width[x]) + "\t");
				resultzeile.append(d3.format(height[x]) + "\t");

				resultzeile.append(n32.format(xCoordinate[x]) +"\t\t");
				resultzeile.append(n32.format(yCoordinate[x]) + "\t");

				resultzeile.append(n13.format(circ[x]) + "\t");
				resultzeile.append(n13.format(solidity[x]) + "\n");

				//arithmetischen Umfang berechnen
				perim_all += perim[x];


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
				perim[x] = rt.getValue("Perim.", x);

				//arithmetischen Umfang berechnen
				perim_all += perim[x];

		  	}
		  	
		}

		for (int x = 0; x <= counter-1; x++)
	  	{
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
		perim_arith = perim_all / counter;
		
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
		
	  	outputCellnucleiInfo(counter, perim_arith, area_all, area_min, area_max, area_arith, median, found_particles);

		IJ.run("Clear Results", "");

	}


	public void startImageProcessingActivity(ImageProcessor original, ImageProcessor copy, ImageProcessor sicherung, ImagePlus heatmapTmp, 
			ImagePlus imp, ImageProcessor heatmap_ip, boolean x10, String radiobox, int w, int h)
	{
		//Zellkerne erkennen und Fehlerpixel ignorieren sowie Tumarflaeche berechnen
		int gewebepixel = simplePixelAnalysis.detectCellPixels(original, w, h);
		properties.setGewebepixel(gewebepixel);
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
	    startExporter.marked(imp, file.getValue(), path.getValue(), startExporter.getnewDirectoryname(), EXPORT_PIC_CHECKBOX);
	    
	    //Heatmap erstellen
	    if (HEATMAP_CHECKBOX == true) {   heatmap.create(file.getValue(), path.getValue(), startExporter.getnewDirectoryname(), AUFLOESUNG_SLIDER, heatmapTmp, heatmapMaske, heatmap_ip, w, h);   }
	    
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
		gd.addHelp("http://htl-hl.ac.at");

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
	    	startExporter.results(resultStack.getValue(), file.getValue(), path.getValue() );

			/*
			String resultsFilename = file.getValue() + "_Results.txt";		//Neuen Filenamen festlegen
	    	String exportResulttable = path.getValue() + "\\" + resultsFilename;

	    	IJ.saveAs("Results", exportResulttable);
	    	*/
		    System.out.print("\n\nMesswerte als File (.txt) exportiert...\n");
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

	    return;
	}


	//Ausgabe der Zellkern Informationen
	public void outputCellnucleiInfo(double counter, double perim_arith, double area_all, double area_min, double area_max, double area_arith, double median, double found_particles)
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
		}

		//Ausgabe in String -> Summary-File
		String summaryString = "";
		String csvSummaryString = "";
		summaryString = summaryString + "\n\n" + ueberschrift + "\nFound nuclei:\t\t\t\t" + intcounter + "\nAdditional measured values:\n" ;


		//Werte auf 3 Kommastellen runden und anzeigen
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);

		//Ausgaben in einem ImageJ Log Fenster / speichern in String:
		csvSummaryString = csvSummaryString + file.getValue().replaceFirst("[.][^.]+$", "") + ";";

		summaryString = summaryString + "gesamte Gewebeflaeche:\t\t" + df.format(properties.getTumorArea()) + " um2\n";
		csvSummaryString = csvSummaryString + df.format(properties.getTumorArea()) + ";";

		summaryString = summaryString + "Total area of all nuclei:\t" + df.format(area_all) + " um2\n";
		csvSummaryString = csvSummaryString + df.format(area_all) + ";";

		summaryString = summaryString + "Zellkernflaeche in %:\t\t" + df.format(100 / (properties.getTumorArea() / area_all)) + "%\n";
		csvSummaryString = csvSummaryString + df.format(100 / (properties.getTumorArea() / area_all)) + ";";

		summaryString = summaryString + "Zellkerne pro mm2:\t\t\t" + df.format((intcounter / properties.getTumorArea() ) * 1000000 ) + "\n";		//Zellkerne 1 / Tumorfläche in um2 / 1000000
		csvSummaryString = csvSummaryString + df.format((intcounter / properties.getTumorArea() ) * 1000000 ) + ";";		//Zellkerne 1 / Tumorfläche in um2 / 1000000

		summaryString = summaryString + "Arithmetic Perimeter:\t\t" + df.format(perim_arith) + " um\n";
		csvSummaryString = csvSummaryString + df.format(perim_arith) + ";";

		summaryString = summaryString + "\nSmallest cell nucleus:\t\t" + df.format(area_min) + " um2\n";	//Ausgabe der Flaeche des kleinsten gefunden Zellkerns (auf 3 Kommastellen genau)
		csvSummaryString = csvSummaryString + df.format(area_min) + ";";	//Ausgabe der Flaeche des kleinsten gefunden Zellkerns (auf 3 Kommastellen genau)

		summaryString = summaryString + "largest cell nucleus:\t\t" + df.format(area_max) + " um2\n";		//Ausgabe der Flaeche des groessten gefunden Zellkerns (auf 3 Kommastellen genau)
		csvSummaryString = csvSummaryString + df.format(area_max) + ";";		//Ausgabe der Flaeche des groessten gefunden Zellkerns (auf 3 Kommastellen genau)

		summaryString = summaryString + "Arithmetic mean area:\t\t" + df.format(area_arith) + " um2\n";	//Ausgabe des arithmetishen Mittels aller Zellkernflaechen (auf 3 Kommastellen genau)
		csvSummaryString = csvSummaryString + df.format(area_arith) + ";";	//Ausgabe des arithmetishen Mittels aller Zellkernflaechen (auf 3 Kommastellen genau)

		summaryString = summaryString + "Median area:\t\t\t\t" + df.format(median) + " um2\n";				//Ausgabe des Medianwerts aller Zellkernfleachen (auf 3 Kommastellen genau)
		csvSummaryString = csvSummaryString + df.format(median) + ";";				//Ausgabe des Medianwerts aller Zellkernfleachen (auf 3 Kommastellen genau)

		summaryString = summaryString + "oval cell nucleus:\t\t\t" + found_particles +"\n";		//Ausgabe der gefundenen ovalen Zellkernen
		csvSummaryString = csvSummaryString + found_particles +"\n";		//Ausgabe der gefundenen ovalen Zellkernen

		//An StringStack uebergeben
		summaryStack.appendString(summaryString);
		csvSummaryStack.appendString(csvSummaryString);

		//counter wieder null setzen
		intcounter = 0;

		return;
	}

	//UP zum exportieren des rot markierten Scans




}

