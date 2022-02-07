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

public class Question13 
{
	MapPanel mp;
	CoordinateConverter cc;
	
	public static void main(String[] args) throws Exception
	{
		Question13 q13 = new Question13();
		
		try
		{
			q13.init();
			q13.buildHighways();
			q13.buildLanduses();
			q13.buildLeisures();
			q13.buildBuildings();
			q13.exit();
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
    	GeoMainFrame main = new GeoMainFrame("Question13", mp);
    	main.setBounds(0, 0, 1024, 768);
    	cc = new CoordinateConverter(1024, 768, 0, 0, 1000);
    	main.setVisible(true);
    	mp.autoAdjust();
    }
    
    public void exit()
    {
        Utils.closeConnection();    	
    }
    
    public void buildLanduses() throws SQLException
    {
    	System.out.println("Retrieve landuses...");
    	PreparedStatement stmt = Utils.getConnection().prepareStatement("select  ST_Transform(w.linestring, 2154) "
        		+ "from	ways w "
        		+ "where	ST_Contains(ST_PolygonFromText('POLYGON((5.7 45.1, 5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1))', 4326), w.linestring) "
        		+ "and		exist(tags, 'landuse');");
    	ResultSet res = stmt.executeQuery();
        
    	System.out.println("Build landuses...");
        while (res.next()) 
        {
            Geometry p  = ((PGgeometry)res.getObject(1)).getGeometry();
            
            if (p.getType() == Geometry.LINESTRING)
            {
            	org.postgis.LineString building = (org.postgis.LineString)p;
            	
            	float[] color = {0, 1, 0};
            	Color c = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB) , color, (float) 0.2);
            	
            	Polygon pol = new Polygon(c, c);
            	for (int i = 0; i < building.numPoints(); i++)
            		pol.addPoint(new Point(cc.xMapToScreen(building.getPoint(i).getX()), -cc.yMapToScreen(building.getPoint(i).getY())));
            	mp.addPrimitive(pol);
            }
        }
        res.close();
        stmt.close();
        System.out.println("Build landuses... done");
    }

    public void buildLeisures() throws SQLException
    {
    	System.out.println("Retrieve leisures...");
    	PreparedStatement stmt = Utils.getConnection().prepareStatement("select  ST_Transform(w.linestring, 2154) "
        		+ "from	ways w "
        		+ "where	ST_Contains(ST_PolygonFromText('POLYGON((5.7 45.1, 5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1))', 4326), w.linestring) "
        		+ "and		exist(tags, 'leisure');");
    	ResultSet res = stmt.executeQuery();
        
    	System.out.println("Build leisures...");
        while (res.next()) 
        {
            Geometry p  = ((PGgeometry)res.getObject(1)).getGeometry();
            
            if (p.getType() == Geometry.LINESTRING)
            {
            	org.postgis.LineString building = (org.postgis.LineString)p;
            	
            	float[] color = {0, 0, (float)0.8};
            	Color c = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB) , color, (float) 0.2);
            	
            	Polygon pol = new Polygon(c, c);
            	for (int i = 0; i < building.numPoints(); i++)
            		pol.addPoint(new Point(cc.xMapToScreen(building.getPoint(i).getX()), -cc.yMapToScreen(building.getPoint(i).getY())));
            	mp.addPrimitive(pol);
            }
        }
        res.close();
        stmt.close();
        System.out.println("Build leisures... done");
    }
    
    public void buildHighways() throws SQLException
    {
    	System.out.println("Retrieve highways...");
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
    
    public void buildBuildings() throws SQLException
    {
    	System.out.println("Retrieve buildings...");
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
