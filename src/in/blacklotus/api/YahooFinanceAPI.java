package in.blacklotus.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface YahooFinanceAPI {

	@GET("finance/chart/{stock}?interval=1d&range=1mo")
	Call<ResponseBody> getStockInfo(@Path("stock") String stock);
}
