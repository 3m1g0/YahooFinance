package in.blacklotus.utils;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

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

		for (int i = 0; i < stocksList.size(); i++) {

			Stock stock = stocksList.get(i);

			BasicDBObject document = new BasicDBObject();

			document.put("FetchDate", new Date());

			document.put("SNO", (i + 1));

			document.put("Symbol", stock.getSymbol());

			document.put("Low10", stock.getLow10());

			document.put("Low10Date", stock.getLow10Date());

			document.put("Price", stock.getNow());

			document.put("High10", stock.getHigh10());

			document.put("High10Date", stock.getHigh10Date());

			document.put("PriceChange", stock.getPricage());

			document.put("PercentPriceChange", stock.getNowPercent());

			document.put("LowHighDiff", stock.getLowHighDiff());

			document.put("SUPT", stock.getSupt());

			document.put("REST", stock.getRest());

			document.put("SRDIFF", stock.getSrdif());

			document.put("PercentSUPT", stock.getSuptPercent());

			document.put("PercentREST", stock.getRestPercent());

			document.put("SURE", stock.getSmar());

			document.put("PercentLow10", stock.getLow10Percent());

			document.put("PercentHigh10", stock.getHigh10Percent());

			document.put("PercentLowHighDiff", stock.getLowHighDiffPercent());

			document.put("PercentVolcage", stock.getVolumeChangePercent());

			document.put("TENDCHG", stock.getDchg10());

			document.put("PercentTENDCHG", stock.getDchgPercent());

			document.put("PriceRank", stock.priceRank());

			document.put("VolumeRank", stock.volumeRank());

			table.insert(document);
		}
	}

	public static void savePriceTrend(List<PriceTrend> stocksList, String trend) {

		DB db = getInstance().getDB(DATABASE_NAME);
		
		if (!isDatabaseConnected) {

			return;
		}

		DBCollection table = db.getCollection("pritrend");

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

			BasicDBObject document = new BasicDBObject();

			document.put("FetchDate", new Date());

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

			table.insert(document);
		}
	}

	public static void saveVolumeTrend(List<VolumeTrend> stocksList) {

		DB db = getInstance().getDB(DATABASE_NAME);
		
		if (!isDatabaseConnected) {

			return;
		}

		DBCollection table = db.getCollection("voltrend");

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

			BasicDBObject document = new BasicDBObject();

			document.put("FetchDate", new Date());

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

			document.put("PercentLow10", round(stock.getLow10Percent()));

			document.put("PercentHigh10", round(stock.getHigh10Percent()));

			document.put("PercentLowHighDiff", round(stock.getLowHighDiffPercent()));

			document.put("Volume", volumes);

			document.put("PercentVolcage", volcagepercents);

			document.put("TENDCHG", round(stock.getDchg10()));

			document.put("PercentTENDCHG", round(stock.getDchgPercent()));

			document.put("PriceRank", stock.getRankForPricePercentDiff(trends.get(0).getPriceDiffPercent()));

			document.put("VolumeRank", trends.get(0).getVolumeRank());

			table.insert(document);
		}
	}

	private static double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

}
