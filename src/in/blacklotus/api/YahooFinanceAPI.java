package in.blacklotus.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface YahooFinanceAPI {

	@GET("finance/chart/{stock}?interval=1d&range=10y")
	Call<ResponseBody> getStockInfo(@Path("stock") String stock);
	
	@GET("finance/chart/{stock}?interval=1d")
	Call<ResponseBody> getStockInfo(@Path("stock") String stock, @Query("period1") long start, @Query("period2") long end);
}
