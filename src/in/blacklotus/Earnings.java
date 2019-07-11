package in.blacklotus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jakewharton.fliptables.FlipTableConverters;

import in.blacklotus.model.Earning;
import in.blacklotus.model.Earning.Quarter;
import in.blacklotus.model.EarningData;
import in.blacklotus.utils.Utils;

public class Earnings {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.0) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2";

	public static final String YAHOO_EARNINGS = "https://finance.yahoo.com/calendar/earnings?day=";

	public static final String MARKET_BEAT = "https://www.marketbeat.com/stocks/";

	public static final String YAHOO_ANALYSIS = "https://finance.yahoo.com/quote/-symbol-/analysis";

	public static final String YAHOO_HISTORY = "https://finance.yahoo.com/quote/-symbol-/history?interval=1d&filter=history&frequency=1d";

	private static ArrayList<Earning> earningsList = new ArrayList<>();

	private static int totalCount = 0;

	private static int completedCount = 0;

	private static boolean processing = false;

	private static int percentage = 0;

	private static String inputDate;

	public static void main(String[] args) {

		Map<String, List<String>> params = Utils.parseArguments(args);

		if (params.containsKey("date")) {

			inputDate = params.get("date").get(0);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			try {

				Date mDate = sdf.parse(inputDate);

				Calendar c = Calendar.getInstance();

				c.setTime(mDate);
				
				if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
						|| c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

					System.out.println("*** Its a holiday, enter a different date ***");

					return;
				}

				inputDate = sdf.format(mDate);

			} catch (ParseException e) {

				System.out.println("*** Enter the date in yyyy-MM-dd format ***");

				return;
			}

		} else {

			System.out.println("*** Enter the date in yyyy-MM-dd format ***");

			return;
		}

		processing = true;

		new Thread(new Runnable() {

			@Override
			public void run() {

				showProgress();
			}

		}).start();

		retrieveSymbols();
		
		Thread threads[] = new Thread[earningsList.size()];
		
		for (int i = 0; i < earningsList.size(); i++) {

			final int index = i;

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {

					retrieveEstimatesByQuarters(index);
				}

			});
			
			threads[i] = t;
			
			t.start();
		}
		
		for(Thread t : threads) {
			
			try {
				
				t.join();
			
			} catch (InterruptedException e) {
			
			}
		}
		
		processing = false;
		
		printData();
	}

	private static void retrieveSymbols() {

		try {

			Document doc = Jsoup.connect(YAHOO_EARNINGS + inputDate).userAgent(USER_AGENT).timeout(0).get();

			Elements rows = doc.getElementsByTag("tr");
			
			if (rows != null && !rows.isEmpty()) {
				
				for (int i = 0; i < rows.size(); i++) {

					Element row = rows.get(i);

					Element symbol = row.getElementsByAttributeValue("aria-label", "Symbol").first();

					Element epsEstimate = row.getElementsByAttributeValue("aria-label", "EPS Estimate").first();

					if (epsEstimate != null) {

						try {

							Double.parseDouble(epsEstimate.text());

							if (symbol != null) {

								Earning earning = new Earning();

								earning.setSymbol(symbol.text());
								
								earningsList.add(earning);
							}

						} catch (NumberFormatException e) {

						}
					}
				}

			} else {

				System.out.println("*** No Earnings for the day " + inputDate);

				System.exit(0);
			}

		} catch (IOException e) {

			System.out.println("*** No Earnings for the day " + inputDate);

			System.exit(0);
		}
	}

	private static void retrieveEstimatesByQuarters(int index) {

		String symbol = earningsList.get(index).getSymbol();
		
		try {
			
			Document doc = Jsoup.connect(MARKET_BEAT + symbol).userAgent(USER_AGENT).timeout(0).get();

			Element earningsTab = doc.getElementById("tabEarnings");
			
			if (earningsTab != null) {

				String earningsUrl = earningsTab.attr("abs:href");
				
				if (earningsUrl != null) {
					
					doc = Jsoup.connect(earningsUrl).userAgent(USER_AGENT).timeout(0).get();

					Elements tables = doc.getElementsByTag("table");

					if (tables != null && !tables.isEmpty()) {

						Element table = tables.get(tables.size() - 1);

						Elements rows = table.getElementsByTag("tr");
						
						if (rows != null && !rows.isEmpty()) {

							ArrayList<Quarter> quarters = new ArrayList<>();

							for (int i = 2; i < Math.min(6, rows.size()); i++) {

								Element row = rows.get(i);

								Elements columns = row.getElementsByTag("td");

								Quarter quarter = new Quarter();

								if (columns != null && !columns.isEmpty()) {

									Element date = columns.get(0);

									if (date != null) {

										quarter.setDate(date.text());
									}

									Element consensusEstimate = columns.get(2);

									if (consensusEstimate != null) {

										try {

											String conEst = consensusEstimate.text();

											conEst = conEst.replaceAll("\\(", "");

											conEst = conEst.replaceAll("\\$", "");

											double value = Double.parseDouble(conEst);

											quarter.setConsensusEstimate(value);

										} catch (NumberFormatException e) {

										}
									}

									Element reportEps = columns.get(3);

									if (reportEps != null) {

										try {

											String repEps = reportEps.text();

											repEps = repEps.replaceAll("\\(", "");

											repEps = repEps.replaceAll("\\$", "");

											double value = Double.parseDouble(repEps);

											quarter.setReportEps(value);

										} catch (NumberFormatException e) {

										}
									}
								}

								quarters.add(quarter);
							}

							totalCount += quarters.size();

							earningsList.get(index).setQuarters(quarters);
							
							retrieveAnalysis(index);
						}
					}
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private static void retrieveAnalysis(int index) {

		String symbol = earningsList.get(index).getSymbol();

		try {

			String url = YAHOO_ANALYSIS.replace("-symbol-", symbol);

			Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(0).get();

			Elements tables = doc.select("table:contains(Earnings History)");

			if (tables != null && !tables.isEmpty()) {

				ArrayList<Quarter> quarters = earningsList.get(index).getQuarters();

				Elements headers = tables.get(0).select("thead > tr");

				if (headers != null && !headers.isEmpty()) {

					Elements columnHeads = headers.get(0).getElementsByTag("th");

					if (columnHeads != null && !columnHeads.isEmpty()) {

						for (int j = 0; j < Math.min(4, quarters.size()); j++) {

							quarters.get(j)
									.setEarningsHistory(columnHeads.get(Math.min(4, quarters.size()) - j).text());
						}
					}
				}

				Elements rows = tables.get(0).select("tbody > tr");

				if (rows != null && !rows.isEmpty()) {

					for (int i = 0; i < rows.size(); i++) {

						Element row = rows.get(i);

						Elements columns = row.getElementsByTag("td");

						if (columns != null && !columns.isEmpty()) {

							for (int j = 0; j < Math.min(4, quarters.size()); j++) {

								try {

									double value = Double.parseDouble(
											columns.get(Math.min(4, quarters.size()) - j).text().replaceAll("%", ""));

									switch (i) {

									case 0:

										quarters.get(j).setEpsEst(value);

										break;

									case 1:

										quarters.get(j).setEpsActual(value);

										break;

									case 2:

										quarters.get(j).setDifference(value);

										break;

									case 3:

										quarters.get(j).setSurprise(value);

										break;
									}

								} catch (NumberFormatException e) {

								}
							}
						}
					}
					
					Thread threads[] = new Thread[quarters.size()];

					for (int i = 0; i < quarters.size(); i++) {

						final int ind = i;

						Thread t = new Thread(new Runnable() {

							@Override
							public void run() {

								retrieveHistory(index, ind);
							}

						});
						
						threads[i] = t;
						
						t.start();
					}
					
					for(Thread t : threads) {
						
						t.join();
					}
				}
			}

		} catch (IOException e) {

		} catch (InterruptedException e) {
			
		}
	}

	private static void retrieveHistory(int index, int quarter) {

		String symbol = earningsList.get(index).getSymbol();

		try {

			String url = getHisrotyUrl(earningsList.get(index).getQuarters().get(quarter).getEarningsHistory(), 5);

			if (url != null) {

				url = url.replaceAll("-symbol-", symbol);

				Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(0).get();

				Elements rows = doc.select("tbody > tr");

				if (rows != null && !rows.isEmpty()) {

					ArrayList<EarningData> earningsDataList = new ArrayList<>();

					for (int i = 0; i < rows.size(); i++) {

						Element row = rows.get(i);

						Elements columns = row.getElementsByTag("td");

						if (columns != null && columns.size() == 7) {

							EarningData data = new EarningData();

							SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

							Date parsedDate = sdf.parse(columns.get(0).text());

							sdf = new SimpleDateFormat("MM/dd/yyyy");

							data.setDate(sdf.format(parsedDate));

							try {

								double value = Double.parseDouble(columns.get(1).text());

								data.setOpen(value);

							} catch (NumberFormatException e) {

							}

							try {

								double value = Double.parseDouble(columns.get(2).text());

								data.setHigh(value);

							} catch (NumberFormatException e) {

							}

							try {

								double value = Double.parseDouble(columns.get(3).text());

								data.setLow(value);

							} catch (NumberFormatException e) {

							}

							try {

								double value = Double.parseDouble(columns.get(4).text());

								data.setClose(value);

							} catch (NumberFormatException e) {

							}

							try {

								String text = columns.get(6).text();

								text = text.replaceAll(",", "");

								long value = Long.parseLong(text);

								data.setVolume(value);

							} catch (NumberFormatException e) {

							}

							// System.out.println(data);

							earningsDataList.add(data);
						}
					}

					earningsList.get(index).getQuarters().get(quarter).setEarningData(earningsDataList);

					checkCompletion();
				}

			} else {

				checkCompletion();
			}

		} catch (IOException e) {

		} catch (ParseException e1) {

		}
	}

	private static void checkCompletion() {

		if(completedCount < totalCount) {
			
			completedCount++;
		}

		percentage = completedCount * 100 / totalCount;
	}

	private static void printData() {
		
		String[] headers = new String[] { "SNO", "SYMBOL", "LOW10", "HIGH10", "PRICE", "$PRICAGE", "%PRICAGE",
				"%VOLCAGE", "ETREND", "EARNING HISTORY", "CONSENSUS EST.", "REPORT EPS", "EPS EST.", "EPS ACTUAL", "DIFFERENCE", "SURPRISE%" };

		ArrayList<String[]> printableStrings = new ArrayList<>();

		for (int i = 0; i < earningsList.size(); i++) {

			ArrayList<Quarter> quarters = earningsList.get(i).getQuarters();

			for (int j = 0; j < quarters.size(); j++) {

				String sym = "";

				String ind = "";

				if (j == 0) {

					sym = earningsList.get(i).getSymbol();

					ind = String.valueOf(i + 1);
				}

				if (quarters.get(j).getEarningData() != null) {

					for (String str : quarters.get(j).toPrintableString(ind, sym)) {

						printableStrings.add(str.split(","));
					}
				}
			}
		}

		String[][] data = new String[printableStrings.size()][];

		for (int k = 0; k < printableStrings.size(); k++) {

			data[k] = printableStrings.get(k);
		}

		System.out.println(FlipTableConverters.fromObjects(headers, data));
		
		writeEarningsToFile(headers);
	}

	private static String getHisrotyUrl(String date, int count) {

		String params = "";

		try {

			Calendar previous = Calendar.getInstance();

			previous.setTime(new Date(date));

			previous.add(Calendar.DAY_OF_YEAR, -1 * count);

			params += "&period1=" + previous.getTimeInMillis() / 1000;

			Calendar next = Calendar.getInstance();

			next.setTime(new Date(date));

			next.add(Calendar.DAY_OF_YEAR, count);

			params += "&period2=" + next.getTimeInMillis() / 1000;

		} catch (Exception e) {

			return null;
		}

		return YAHOO_HISTORY + params;
	}
	
	private static void writeEarningsToFile(String[] headers) {

		try {

			File file = Utils.generateOutputFile("earnings", Utils.generateOutputDir());

			if (!file.exists()) {

				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(new FileWriter(file));

			String header = "";

			for (int i = 0; i < headers.length - 1; i++) {

				header += headers[i] + ",";
			}

			header += headers[headers.length - 1];

			writer.println(header);
			
			for (int i = 0; i < earningsList.size(); i++) {

				ArrayList<Quarter> quarters = earningsList.get(i).getQuarters();

				for (int j = 0; j < quarters.size(); j++) {

					String sym = "";

					String ind = "";

					if (j == 0) {

						sym = earningsList.get(i).getSymbol();

						ind = String.valueOf(i + 1);
					}

					if (quarters.get(j).getEarningData() != null) {

						for (String str : quarters.get(j).toPrintableString(ind, sym)) {
							
							writer.println(str);
						}
					}
				}
			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private static void showProgress() {

		char[] animationChars = new char[] { '|', '/', '-', '\\' };

		int i = 0;

		while (processing) {

			System.out.print(" Processing: " + percentage + "% " + animationChars[i++ % 4] + "\r");

			try {

				Thread.sleep(100);

			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

		System.out.println("                                                                      ");
	}

}
