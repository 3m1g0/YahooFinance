package in.blacklotus.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;

import in.blacklotus.model.VolumeTrend;

public class DatabaseHelper {

	public static void insertVolumeTrend(ArrayList<VolumeTrend> trendsList) {
		Connection con = null;

		Statement stmt = null;
		int result = 0;

		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/testdb", "SA", "");
			stmt = con.createStatement();

			result = stmt.executeUpdate("CREATE MEMORY TABLE VOLTREND (" + "FetchDate date," + "FetchTime time,"
					+ "SNO VARCHAR(30) NOT NULL," + "Symbol VARCHAR(50) NOT NULL," + "Low10 DECIMAL(10,2) NOT NULL,"
					+ "High10 DECIMAL(10,2) NOT NULL," + "Price DECIMAL(10,2) NOT NULL,"
					+ "PriceChange DECIMAL(10,2) NOT NULL," + "PercentPriceChange DECIMAL(10,2) NOT NULL,"
					+ "PriceRank INT NOT NULL," + "Volume INT NOT NULL," + "PercentVolumeChange DECIMAL(10,2) NOT NULL,"
					+ "VolumeRank INT NOT NULL,");

			for(VolumeTrend trend : trendsList) {
				result = stmt.executeUpdate("INSERT INTO VOLTREND VALUES ()");
			}
			con.commit();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		System.out.println("Table created successfully");
	}

	public static void main(String[] args) {
		Connection con = null;

		try {
			// Registering the HSQLDB JDBC driver
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			// Creating the connection with HSQLDB
			con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/testdb", "SA", "");
			if (con != null) {
				System.out.println("Connection created successfully");

			} else {
				System.out.println("Problem with creating connection");
			}

		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
