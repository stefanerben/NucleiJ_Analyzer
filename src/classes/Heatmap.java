package classes;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;


public class Heatmap
{

	public void create(String filename, String path, String newDirectoryname, int AUFLOESUNG_SLIDER, ImagePlus heatmapTmp, ImageProcessor heatmapMaske, ImageProcessor heatmap_ip, int w, int h)
	{
		//Dichte berechnen:		
  		float radiusFloat = (float) (w * 0.0002 * AUFLOESUNG_SLIDER);		//w/(w/aufloesung);
  		int radius = Math.round(radiusFloat);
  		int max = 0;
		double max_double = 0;
  		int xsuchsetzRadius = w / (w/10);		//512
		int ysuchsetzRadius = 10;		//467

		for (int x = 0; x <= w; x=x+xsuchsetzRadius)
  		{
  			//region ProgressBar !!
			if (x == 0)
  			{
  				IJ.showProgress(0.01);	
  			}
  			else if (x < (w/100*10))
  			{
  				IJ.showProgress(0.1);		
  			}
  			else if (x < (w/100*20))
  			{
  				IJ.showProgress(0.2);		
  			}
  			else if (x < (w/100*30))
  			{
  				IJ.showProgress(0.3);		
  			}
  			else if (x < (w/100*40))
  			{
  				IJ.showProgress(0.4);		
  			}
  			else if (x < (w/100*50))
  			{
  				IJ.showProgress(0.5);		
  			}
  			else if (x < (w/100*60))
  			{
  				IJ.showProgress(0.6);		
  			}
  			else if (x < (w/100*70))
  			{
  				IJ.showProgress(0.7);		
  			}
  			else if (x < (w/100*80))
  			{
  				IJ.showProgress(0.8);		
  			}
  			else if (x < (w/100*90))
  			{
  				IJ.showProgress(0.9);		
  			}
  			else if (x < (w/100*95))
  			{
  				IJ.showProgress(0.95);		
  			}
  			//endregion ProgressBar

  			for (int y = 0; y <= h; y=y+ysuchsetzRadius)
  			{
  				// in Radius Umgebung nach roten Pixeln suchen
  				int i, j, sum = 0, abgesuchtepixel=0;
  				float dichte;
  				for (j=-radius; j<=radius; j++)
  				{
  					for (i=-radius; i<=radius; i++)
  					{
  						if ( ((x+j) >= 0) && ((y+i) >= 0) )		//regel dass wir nicht im negativen bereich suchen
  						{
  							abgesuchtepixel++;
  							int c = heatmapMaske.getPixel(x+j, y+i);
  							if ( c == -16777216)				//-2420452
  							{
  								sum++;							//gefundene Pixel zaehlen
  							}
  						}
  					}
  				}				
  				//alles abgesucht, sum ist gesetzt
  				if (sum == 0)
  				{
  					dichte = 0;

					// TODO jeden Px setzten, da Heatmap eh schon kleiner ist

  				    if ( ((x/10) >= 0) && ((y/10) >= 0)   )
					{
						heatmap_ip.putPixel(x/10, y/10, 0);
					}

  				}
  				else
  				{

					// TODO jeden Px setzten, da Heatmap eh schon kleiner ist

					dichte = 255 / (abgesuchtepixel / sum);
  					if (dichte > max)
  					{
  						max = (int) dichte;
						max_double = dichte;
  					}
  					//float dichte zu int tmp
  					int tmp = Math.round(dichte); 					
  				    for (j=-xsuchsetzRadius; j<=xsuchsetzRadius; j++)
  				    {
  				    	for (i=-ysuchsetzRadius; i<=ysuchsetzRadius; i++)
  				    	{
  				    		if ( ((x/10) >= 0) && ((y/10) >= 0)   )
  				    		{
  				    			heatmap_ip.putPixel(x/10, y/10, tmp);
  				    		}
  				    	}
  				    }
  				}
  			}
  		}
  		heatmapTmp.show();

		double heatmapMaxDichteWert =  100 / (radius *radius / (max_double / 255 * (radius * radius)) );
		System.out.println(heatmapMaxDichteWert + ", " + radius + "\n");

  		float faktorfloat = (255 / max);
  		String befehl = "value=%";
  		String value = String.valueOf(faktorfloat);

		// LUT anwenden
		IJ.run(heatmapTmp, "Red/Green", "");

		//wieviel pixel sind auf 255?
		int heatmapWidth = heatmap_ip.getWidth();
		int heatmapHeight = heatmap_ip.getHeight();
		int maxPixel = 0;

		// Heatmap verbessern, falls es nur sehr kleine Hitzefelder gibt, wird die Heatmap aufgehellt
		while(maxPixel < (heatmapWidth*heatmapHeight/500) )
		{
			if (maxPixel == 0)
			{
				IJ.run(heatmapTmp, "Multiply...", befehl.replace("%", value));
			}
			else
			{
				IJ.run(heatmapTmp, "Multiply...", "value=1.1");
			}
			maxPixel = 0;
			for (int x = 0; x <= heatmapWidth; x++)
			{
				for (int y = 0; y <= heatmapHeight; y++)
				{
					int p = heatmap_ip.getPixel(x, y);
					if (p > 240)
					{
						maxPixel++;
					}
				}
			}

		}

		// Einfugen der Legende

		// Berechnen der Werte
		int hLegend = heatmapHeight / 900;
		if (hLegend < 5)
		{
			hLegend = 5;
		}

		int wLegend = heatmapWidth / 900;
		if (wLegend < 5)
		{
			wLegend = 5;
		}
		int hightLegend = heatmapHeight / 70;

		int yStartLegend = heatmapHeight - hLegend - hightLegend;
		int yEndLegend = heatmapHeight - hLegend;
		int xStartLegend = wLegend;
		int xEndLegend = heatmapWidth -wLegend;

		int farbe = 0;
		int lenghtLegend = heatmapWidth - (2 * wLegend);
		int farbWechsel = lenghtLegend / 255;
		int colorSwitchCounter = 0;

		//px fuer px setzen
		for (int x = xStartLegend; x <= xEndLegend; x++)
		{
			colorSwitchCounter++;

			for (int y = yStartLegend; y <= yEndLegend; y++)
			{
				heatmap_ip.putPixel(x, y, farbe);
			}


			if (colorSwitchCounter == farbWechsel)
			{
				farbe++;
				colorSwitchCounter = 0;
				System.out.println("\nColorswitch\n"+farbe);

			}

		}




		//TODO es fehlt das alte wait und dan LUT einstellen

		//Speichern der Heatmap
  		System.out.println("Image Created");
		String resultsFilename = filename.replaceFirst("[.][^.]+$", "") + "_Heatmap.tif";		//Neuen Filenamen festlegen
  		String exportHeatmap = path + newDirectoryname + "\\" + resultsFilename;

		IJ.run(heatmapTmp, "Median...", "radius=2");
		IJ.saveAs(heatmapTmp, "Tif", exportHeatmap);
	    System.out.print("\n\nHeatmap exportiert...\n");
	    System.out.print(exportHeatmap);
  		


		// Text erstellen

		//TODO besseres format damit immer gleich viele stellen agezeigt werden

		
		DecimalFormat d3 = new DecimalFormat("#.##");
		d3.setRoundingMode(RoundingMode.HALF_UP);

		String key = String.valueOf( d3.format(heatmapMaxDichteWert) )+ "%";


		//BufferedImage bufferedImage = BufferedImageCreator.create(heatmapTmp, 0);
		BufferedImage bufferedImage = heatmap_ip.getBufferedImage();

		Graphics graphics = bufferedImage.getGraphics();

		graphics.setColor(Color.GREEN);
		graphics.setFont(new Font("Arial Black", Font.BOLD, 10));
		graphics.drawString(key, lenghtLegend - 35, yStartLegend-5);
		try {
			ImageIO.write(bufferedImage, "png", new File(exportHeatmap));		//kein tif verfuegbar
		} catch (IOException e) {
			e.printStackTrace();
		}

		heatmapTmp.changes = false;

		heatmapTmp.close();

		/*
		// LUT anwenden
		IJ.run(heatmapTmp, "Red/Green", "");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/


		System.out.print("\n\nHeatmap created...");
  		
  		return;
	}
	

}
