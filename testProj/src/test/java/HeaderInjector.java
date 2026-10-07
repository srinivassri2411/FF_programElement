import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v139.network.Network;
import org.openqa.selenium.devtools.v139.network.model.Headers;
import java.util.HashMap;
import java.util.Map;

public class HeaderInjector {
	public static void main(String[] args) {
		ChromeDriver driver = new ChromeDriver();
		DevTools devTools = driver.getDevTools();
		devTools.createSession();
		devTools.send(Network.enable(java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(),
				java.util.Optional.empty()));
		Map<String, Object> headers = new HashMap<>();
		headers.put("x-pb-uat-api-build", "myacc_uat");
		devTools.send(Network.setExtraHTTPHeaders(new Headers(headers)));
		driver.get("https://httpbin.org/headers");
	}
}
