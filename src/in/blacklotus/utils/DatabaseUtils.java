package in.blacklotus.utils;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jakewharton.fliptables.FlipTableConverters;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import in.blacklotus.model.Opening;
import in.blacklotus.model.PriceTrend;
import in.blacklotus.model.PriceTrendData;
import in.blacklotus.model.Stock;
import in.blacklotus.model.VolumeTrend;
import in.blacklotus.model.VolumeTrendData;

public class DatabaseUtils {

	private static final String DATABASE_NAME = "unifier";

	private static MongoClient mongoClient;

	private static boolean isDatabaseConnected = true;

	private static MongoClient getInstance() {

		if (mongoClient == null) {

			try {

				mongoClient = new MongoClient();

				mongoClient.getDatabaseNames();

			} catch (UnknownHostException e) {

				isDatabaseConnected = false;

				mongoClient.close();

			} catch (Exception e) {

				isDatabaseConnected = false;

				mongoClient.close();
			}
		}

		return mongoClient;
	}

	public static void saveLowHigh10(List<Stock> stocksList) {

		DB db = getInstance().getDB(DATABASE_NAME);

		if (!isDatabaseConnected) {

			return;
		}

		DBCollection table = db.getCollection("lowhigh10");

		SimpleDateFormat dateSdf = new SimpleDateFormat("MMMM dd yyyy");

		SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm aaa");

		for (int i = 0; i < stocksList.size(); i++) {

			Stock stock = stocksList.get(i);

			BasicDBObject query = new BasicDBObject();

			query.put("Symbol", stock.getSymbol());

			query.put("FetchDate", dateSdf.format(new Date()));

			BasicDBObject document = new BasicDBObject();

			document.put("FetchDate", dateSdf.format(new Date()));

			document.put("FetchTime", timeSdf.format(new Date()));

			document.put("SNO", (i + 1));

			document.put("Symbol", stock.getSymbol());

			document.put("Low10", Utils.round(stock.getLow10()));

			document.put("Low10Date", stock.getLow10Date());

			document.put("Price", Utils.round(stock.getNow()));
			
			document.put("Volume", Utils.formattedVolume(stock.getVolume()));

			document.put("High10", Utils.round(stock.getHigh10()));

			document.put("High10Date", stock.getHigh10Date());

			document.put("PriceChange", Utils.round(stock.getPricage()));

			document.put("PercentPriceChange", Utils.round(stock.getNowPercent()));

			document.put("LowHighDiff", Utils.round(stock.getLowHighDiff()));

			document.put("SUPT", Utils.round(stock.getSupt()));

			document.put("REST", Utils.round(stock.getRest()));

			document.put("SRDIFF", Utils.round(stock.getSrdif()));

			document.put("PercentSUPT", Utils.round(stock.getSuptPercent()));

			document.put("PercentREST", Utils.round(stock.getRestPercent()));

			document.put("SURE", stock.getSmar());
			
			document.put("NEWLOHI", stock.getNewHigh());

			document.put("PercentLow10", Utils.round(stock.getLow10Percent()));

			document.put("PercentHigh10", Utils.round(stock.getHigh10Percent()));

			document.put("PercentLowHighDiff", Utils.round(stock.getLowHighDiffPercent()));

			document.put("PercentVolcage", Utils.round(stock.getVolumeChangePercent()));

			document.put("TENDCHG", Utils.round(stock.getDchg10()));

			document.put("PercentTENDCHG", Utils.round(stock.getDchgPercent()));

			document.put("PriceRank", stock.priceRank());

			document.put("VolumeRank", stock.volumeRank());

			table.update(query, document, true, false);
		}
	}

	public static void savePriceTrend(List<PriceTrend> stocksList, String trend) {

		DB db = getInstance().getDB(DATABASE_NAME);

		if (!isDatabaseConnected) {

			return;
		}

		DBCollection table = db.getCollection("pritrend");

		SimpleDateFormat dateSdf = new SimpleDateFormat("MMMM dd yyyy");

		SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm aaa");

		for (int i = 0; i < stocksList.size(); i++) {

			PriceTrend stock = stocksList.get(i);

			List<PriceTrendData> trends = stock.getTrends();

			double[] prices = new double[trends.size()];

			Date[] priceDates = new Date[trends.size()];

			double[] pricages = new double[trends.size() - 1];

			double[] pricagepercents = new double[trends.size() - 1];

			double[] volumes = new double[trends.size()];

			double[] volcagepercents = new double[trends.size() - 1];

			for (int j = 0; j < trends.size(); j++) {

				PriceTrendData trendData = trends.get(j);

				prices[j] = round(trendData.getValue());

				priceDates[j] = new Date(trendData.getTimestamp());

				volumes[j] = trendData.getVolume();

				if (j < pricages.length) {

					pricages[j] = round(trendData.getPriceDiff());

					pricagepercents[j] = round(trendData.getPriceDiffPercent());

					volcagepercents[j] = round(trendData.getVolumeDiffPercentage());
				}
			}

			BasicDBObject query = new BasicDBObject();

			query.put("Symbol", stock.getSymbol());

			query.put("FetchDate", dateSdf.format(new Date()));

			BasicDBObject document = new BasicDBObject();

			document.put("FetchDate", dateSdf.format(new Date()));

			document.put("FetchTime", timeSdf.format(new Date()));

			document.put("SNO", (i + 1));

			document.put("Symbol", stock.getSymbol());

			document.put("Low10/20", new double[] { round(trends.get(0).getLow10()), round(trends.get(0).getLow20()) });

			document.put("Low10/20Date", new Date[] { trends.get(0).getLow10Date(), trends.get(0).getLow20Date() });

			document.put(trend, prices);

			document.put("PriceDate", priceDates);

			document.put("High10/20",
					new double[] { round(trends.get(0).getHigh10()), round(trends.get(0).getHigh20()) });

			document.put("High10/20Date", new Date[] { trends.get(0).getHigh10Date(), trends.get(0).getHigh20Date() });

			document.put("PriceChange", pricages);

			document.put("PercentPriceChange", pricagepercents);

			document.put("LowHighDiff", round(stock.getLowHighDiff()));

			document.put("SUPT", round(stock.getSupt()));

			document.put("REST", round(stock.getRest()));

			document.put("SRDIFF", round(stock.getSrdif()));

			document.put("PercentSUPT", round(stock.getSuptPercent()));

			document.put("PercentREST", round(stock.getRestPercent()));

			document.put("SURE", stock.getSmar());
			
			document.put("NEWLOHI", stock.getNewHigh());

			document.put("PercentLow10", round(stock.getLow10Percent()));

			document.put("PercentHigh10", round(stock.getHigh10Percent()));

			document.put("PercentLowHighDiff", round(stock.getLowHighDiffPercent()));

			document.put("Volume", volumes);

			document.put("PercentVolcage", volcagepercents);

			document.put("TENDCHG", round(stock.getDchg10()));

			document.put("PercentTENDCHG", round(stock.getDchgPercent()));

			document.put("PriceRank", trends.get(0).getPriceRank());

			document.put("VolumeRank", trends.get(0).getVolumeRank());

			document.put("DayRank", trends.get(0).getDayRank());

			table.update(query, document, true, false);
		}
	}

	public static void saveVolumeTrend(List<VolumeTrend> stocksList) {

		DB db = getInstance().getDB(DATABASE_NAME);

		if (!isDatabaseConnected) {

			return;
		}

		DBCollection table = db.getCollection("voltrend");

		SimpleDateFormat dateSdf = new SimpleDateFormat("MMMM dd yyyy");

		SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm aaa");

		for (int i = 0; i < stocksList.size(); i++) {

			VolumeTrend stock = stocksList.get(i);

			List<VolumeTrendData> trends = stock.getTrends();

			double[] prices = new double[trends.size()];

			Date[] priceDates = new Date[trends.size()];

			double[] pricages = new double[trends.size() - 1];

			double[] pricagepercents = new double[trends.size() - 1];

			double[] volumes = new double[trends.size()];

			double[] volcagepercents = new double[trends.size() - 1];

			for (int j = 0; j < trends.size(); j++) {

				VolumeTrendData trendData = trends.get(j);

				prices[j] = round(trendData.getValue());

				priceDates[j] = new Date(trendData.getTimestamp());

				volumes[j] = trendData.getVolume();

				if (j < pricages.length) {

					pricages[j] = round(trendData.getPriceDiff());

					pricagepercents[j] = round(trendData.getPriceDiffPercent());

					volcagepercents[j] = round(trendData.getVolumeDiffPercentage());
				}
			}

			BasicDBObject query = new BasicDBObject();

			query.put("Symbol", stock.getSymbol());

			query.put("FetchDate", dateSdf.format(new Date()));

			BasicDBObject document = new BasicDBObject();

			document.put("FetchDate", dateSdf.format(new Date()));

			document.put("FetchTime", timeSdf.format(new Date()));

			document.put("SNO", (i + 1));

			document.put("Symbol", stock.getSymbol());

			document.put("Low10/20", new double[] { round(trends.get(0).getLow10()), round(trends.get(0).getLow20()) });

			document.put("Low10/20Date", new Date[] { trends.get(0).getLow10Date(), trends.get(0).getLow20Date() });

			document.put("Price", prices);

			document.put("PriceDate", priceDates);

			document.put("High10/20",
					new double[] { round(trends.get(0).getHigh10()), round(trends.get(0).getHigh20()) });

			document.put("High10/20Date", new Date[] { trends.get(0).getHigh10Date(), trends.get(0).getHigh20Date() });

			document.put("PriceChange", pricages);

			document.put("PercentPriceChange", pricagepercents);

			document.put("LowHighDiff", round(stock.getLowHighDiff()));

			document.put("SUPT", round(stock.getSupt()));

			document.put("REST", round(stock.getRest()));

			document.put("SRDIFF", round(stock.getSrdif()));

			document.put("PercentSUPT", round(stock.getSuptPercent()));

			document.put("PercentREST", round(stock.getRestPercent()));

			document.put("SURE", stock.getSmar());
			
			document.put("NEWLOHI", stock.getNewHigh());

			document.put("PercentLow10", round(stock.getLow10Percent()));

			document.put("PercentHigh10", round(stock.getHigh10Percent()));

			document.put("PercentLowHighDiff", round(stock.getLowHighDiffPercent()));

			document.put("Volume", volumes);

			document.put("PercentVolcage", volcagepercents);

			document.put("TENDCHG", round(stock.getDchg10()));

			document.put("PercentTENDCHG", round(stock.getDchgPercent()));

			document.put("PriceRank", stock.getRankForPricePercentDiff(trends.get(0).getPriceDiffPercent()));

			document.put("VolumeRank", trends.get(0).getVolumeRank());

			table.update(query, document, true, false);
		}
	}

	public static Set<String> getPreviousDaySymbols() {

		Set<String> symbolSet = new HashSet<>();

		DB db = getInstance().getDB(DATABASE_NAME);

		if (!isDatabaseConnected) {

			return symbolSet;
		}

		DBCollection table = db.getCollection("voltrend");

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");

		Calendar c = Calendar.getInstance();

		c.add(Calendar.DAY_OF_MONTH, -1);

		BasicDBObject query = new BasicDBObject("FetchDate", sdf.format(c.getTime()));

		DBCursor results = table.find(query);

		List<DBObject> stocks = results.toArray();

		for (DBObject dbObject : stocks) {

			symbolSet.add((String) dbObject.get("symbol"));
		}

		return symbolSet;

	}

	public static void saveOpeningValue(List<Opening> stocksList, boolean save, String type, Date date) {

		String[] headers = new String[] { "SNO", "SYMBOL", "OPEN", "CLOSE", "LOW", "HIGH", "GAINCLOSE", "%GAINCLOSE", "GAINHIGH", "%GAINHIGH", "%VOLCAGE2" };

		String[][] data = new String[stocksList.size()][];

		DB db = getInstance().getDB(DATABASE_NAME);

		if (!isDatabaseConnected || !save) {

			System.out.println("Unable to connect Database");

			for (int i = 0; i < stocksList.size(); i++) {

				Opening stock = stocksList.get(i);
				
				data[i] = new String[] { String.valueOf(i + 1), stock.getSymbol(),
						String.format("%.2f", stock.getOpen()), String.format("%.2f", stock.getNow()),
						String.format("%.2f", stock.getLow20()), String.format("%.2f", stock.getHigh20()),
						String.format("%.2f", stock.getGainClose()), String.format("%.2f%%", stock.getGainClosePercent()),
						String.format("%.2f", stock.getGainHigh()), String.format("%.2f%%", stock.getGainHighPercent()),
						String.format("%.2f%%", stock.getVolumeChangePercent2())};
			}

			System.out.println(FlipTableConverters.fromObjects(headers, data));

			return;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");

		DBCollection table = db.getCollection(type);

		for (int i = 0; i < stocksList.size(); i++) {

			Opening stock = stocksList.get(i);
			
			BasicDBObject query = new BasicDBObject("FetchDate", sdf.format(date));
			
			query.put("Symbol", stock.getSymbol());

			BasicDBObject document = new BasicDBObject();

			document.put("Open", Utils.round(stock.getOpen()));

			document.put("Close", Utils.round(stock.getNow()));

			document.put("Low", Utils.round(stock.getLow20()));

			document.put("High", Utils.round(stock.getHigh20()));
			
			document.put("GainClose", Utils.round(stock.getGainClose()));
			
			document.put("%GainClose", Utils.round(stock.getGainClosePercent()));
			
			document.put("GainHigh", Utils.round(stock.getGainHigh()));
			
			document.put("%GainHigh", Utils.round(stock.getGainHighPercent()));
			
			document.put("%VOLCAGE2", Utils.round(stock.getVolumeChangePercent2()));
			
			BasicDBObject setQuery = new BasicDBObject();
			
		    setQuery.append("$set", document);

			table.update(query, setQuery, true, false);

			data[i] = new String[] { String.valueOf(i + 1), stock.getSymbol(), String.format("%.2f", stock.getOpen()),
					String.format("%.2f", stock.getNow()), String.format("%.2f", stock.getLow20()),
					String.format("%.2f", stock.getHigh20()), String.format("%.2f", stock.getGainClose()), 
					String.format("%.2f%%", stock.getGainClosePercent()), String.format("%.2f", stock.getGainHigh()), 
					String.format("%.2f%%", stock.getGainHighPercent()), String.format("%.2f%%", stock.getVolumeChangePercent2()) };
		}

		System.out.println("Writing in to table");

		System.out.println(FlipTableConverters.fromObjects(headers, data));
	}

	private static double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

}
