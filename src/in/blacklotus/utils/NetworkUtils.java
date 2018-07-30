package in.blacklotus.utils;

import in.blacklotus.api.YahooFinanceAPI;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkUtils {

	private static YahooFinanceAPI service;

	private static Retrofit retrofitBuilder;

	public static YahooFinanceAPI getYahooFinanceAPIService() {
		if (service == null) {
			service = getRetrofitBuilder().create(YahooFinanceAPI.class);
		}
		return service;
	}

	private static Retrofit getRetrofitBuilder() {
		if (retrofitBuilder == null) {
			retrofitBuilder = new Retrofit.Builder().baseUrl("https://query1.finance.yahoo.com/v8/")
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		return retrofitBuilder;
	}

}
