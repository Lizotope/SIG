package tpsig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.postgis.Geometry;
import org.postgis.Point;

import tpsig.database.Utils;

public class Question11 
{
    public static void main(String[] args) throws Exception
    {
        Connection conn = Utils.getConnection();

        PreparedStatement stmt = conn.prepareStatement("select tags->'name' as name, geom from nodes where tags->'name' like 'Dom__ne _niversit%'");
        ResultSet res = stmt.executeQuery();
        while (res.next()) 
        {
            org.postgis.PGgeometry geom = (org.postgis.PGgeometry)res.getObject(2);
            org.postgis.Geometry geometry = geom.getGeometry();
            if (geometry.getType() == Geometry.POINT)
            {
                Point p = geometry.getPoint(0);
                System.out.println(String.format("| %s | %s | %s |", res.getString(1), p.getX(), p.getY()));
            }
        }
        Utils.closeConnection();
    }
}
