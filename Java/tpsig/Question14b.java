package tpsig;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgis.Geometry;
import org.postgis.PGgeometry;

import tpsig.database.Utils;
import tpsig.geoexplorer.gui.CoordinateConverter;
import tpsig.geoexplorer.gui.GeoMainFrame;
import tpsig.geoexplorer.gui.LineString;
import tpsig.geoexplorer.gui.MapPanel;
import tpsig.geoexplorer.gui.Point;
import tpsig.geoexplorer.gui.Polygon;

public class Question14b 
{
	MapPanel mp;
	CoordinateConverter cc;
	
	public static void main(String[] args) throws Exception
	{
		Question14b q14b = new Question14b();
		
		try
		{
			q14b.init();
			q14b.buildHighways();
			q14b.buildNuisance();
			q14b.buildBuildings();
			q14b.exit();
		}
		catch(Exception ex)
		{
			System.out.println(String.format("Erreur : %s", ex.getMessage()));
		}
	}
	
    public void init()
    {
    	// Preparation du graphique
    	mp = new MapPanel(0, 0, 1000);
    	GeoMainFrame main = new GeoMainFrame("Question14b", mp);
    	main.setBounds(0, 0, 1024, 768);
    	cc = new CoordinateConverter(1024, 768, 0, 0, 1000);
    	main.setVisible(true);
    	mp.autoAdjust();
    }
    
    public void exit()
    {
        Utils.closeConnection();    	
    }

    public void buildHighways() throws SQLException
    {
        PreparedStatement stmt = Utils.getConnection().prepareStatement(
        		  "select  ST_Transform(w.linestring, 2154) , w.tags->'highway' "
        		+ "from	   ways w "
        		+ "where   ST_Contains(ST_PolygonFromText('POLYGON((5.7 45.1, 5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1))', 4326), w.linestring) "
        		+ "and	   exist(w.tags, 'highway') ");
        ResultSet res = stmt.executeQuery();
        
        System.out.println("Build highways...");
        while (res.next()) 
        {
            Geometry p  = ((PGgeometry)res.getObject(1)).getGeometry();
            String highwayType = res.getString(2);
            
            if (p.getType() == Geometry.LINESTRING)
            {
            	org.postgis.LineString highway = (org.postgis.LineString)p;
            	Color c = Color.RED;
            	switch (highwayType)
            	{
            		case "road":
            			c = Color.GRAY;
            			break;
            		case "residential":
            			c = Color.BLUE;
            			break;
            		case "path":
            			c = Color.LIGHT_GRAY;
            			break;
            		case "footway":
            			c = Color.PINK;
            			break;
            		case "service":
            			c = Color.ORANGE;
            			break;
            		case "track":
            			c = Color.BLACK;
            			break;
            		default:
            			c = Color.BLACK;
            	}
            	
            	LineString ls = new LineString(c);
            	for (int i = 0; i < highway.numPoints(); i++)
            		ls.addPoint( new Point(cc.xMapToScreen(highway.getPoint(i).getX()), -cc.yMapToScreen(highway.getPoint(i).getY())));
            	mp.addPrimitive(ls);
            }
        }
        mp.autoAdjust();
        res.close();
        stmt.close();
        System.out.println("Build highways... done");
    }
    
    public void buildNuisance() throws SQLException
    {
        PreparedStatement stmt = Utils.getConnection().prepareStatement(
        		  "select  ST_Buffer(ST_Transform(w.linestring, 2154), 100) , w.tags->'highway' "
        		+ "from	   ways w "
        		+ "where   ST_Contains(ST_PolygonFromText('POLYGON((5.7 45.1, 5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1))', 4326), w.linestring) "
        		+ "and		(w.tags->'highway' in ('primary', 'motorway', 'trunk', 'via ferrata') "
        		+ "or		exist(w.tags, 'railway')) ");
        
        ResultSet res = stmt.executeQuery();
        
        System.out.println("Build nuisance...");
        while (res.next()) 
        {
            Geometry p  = ((PGgeometry)res.getObject(1)).getGeometry();
            String highwayType = res.getString(2);
            
            if (p.getType() == Geometry.POLYGON)
            {
            	org.postgis.Polygon area = (org.postgis.Polygon)p;
            	float[] cRGB = { 0, 0, 0 };
            	if (highwayType != null)
            	{
	            	switch (highwayType)
	            	{
	            		case "primary":
	            			cRGB[0] = (float)0.4539;
	            			cRGB[1] = (float)0.4611;
	            			cRGB[2] = (float)0.085;
	            			break;
	            		case "motorway":
	            			cRGB[0] = (float)1;
	            			cRGB[1] = (float)0;
	            			cRGB[2] = (float)0;
	            			break;
	            		case "trunk" :
	            			cRGB[0] = (float)1;
	            			cRGB[1] = (float)0;
	            			cRGB[2] = (float)0;
	            			break;
	            		case "via ferrata":
	            			cRGB[0] = (float)1;
	            			cRGB[1] = (float)0;
	            			cRGB[2] = (float)0;
	            			break;	            				            			
	            		default:
	            			cRGB[0] = (float)0;
	            			cRGB[1] = (float)0;
	            			cRGB[2] = (float)0;
	            	}
            	}
            	else 
            	{
        			cRGB[0] = (float)1;
        			cRGB[1] = (float)0;
        			cRGB[2] = (float)0;
            	}
            	
            	Color c = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), cRGB, (float)0.3);
            	Polygon nuisanceArea = new Polygon(c, c);
            	for (int i = 0; i < area.numPoints(); i++)
            		nuisanceArea.addPoint( new Point(cc.xMapToScreen(area.getPoint(i).getX()), -cc.yMapToScreen(area.getPoint(i).getY())));
            	mp.addPrimitive(nuisanceArea);
            }
        }
        mp.autoAdjust();
        res.close();
        stmt.close();
        System.out.println("Build nuisance... done");
    }
    
    public void buildBuildings() throws SQLException
    {
    	PreparedStatement stmt = Utils.getConnection().prepareStatement(
    			  "select  ST_Transform(w.linestring, 2154) "
        		+ "from	   ways w "
        		+ "where   ST_Contains(ST_PolygonFromText('POLYGON((5.7 45.1, 5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1))', 4326), w.linestring) "
        		+ "and	   exist(w.tags, 'building')");
    	ResultSet res = stmt.executeQuery();
        
        System.out.println("Build buildings...");
        while (res.next()) 
        {
            Geometry p  = ((PGgeometry)res.getObject(1)).getGeometry();
            if (p.getType() == Geometry.LINESTRING)
            {
            	org.postgis.LineString building = (org.postgis.LineString)p;
            	
            	Polygon pol = new Polygon();
            	for (int i = 0; i < building.numPoints(); i++)
            		pol.addPoint(new Point(cc.xMapToScreen(building.getPoint(i).getX()), -cc.yMapToScreen(building.getPoint(i).getY())));
            	mp.addPrimitive(pol);
            }
        }
        res.close();
        stmt.close();
        System.out.println("Build buildings... done");
    }
}
