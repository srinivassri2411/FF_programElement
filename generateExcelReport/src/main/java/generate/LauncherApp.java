package generate;

public class LauncherApp {
	public static void main(String[] args) {
		doValidation(args);
		String url = args[0];
		String userName = args[1];
		String passtoken = args[2];
		String licenseId = args[3];

//		String url = "https://us-app.fireflink.com/";
//		String passtoken = "Qwerty@123";
//		String userName = "srinivas.m@fireflink.com";
//		String licenseId = "LIC1026593";

//		String url = "https://sg-app.fireflink.com/";
//		String passtoken = "@Channu123";
//		String userName = "channa.s@fireflink.com";
//		String licenseId = "LIC2027085";
		try {
			GenerateReport.run(url, licenseId, passtoken, userName);
		} catch (Exception e) {
			throw new ErrorHandler(e.toString());
		}
	}

	private static void doValidation(String[] args) {
		if (args.length < 4)
			throw new ErrorHandler(
					"INVALID ARGS.: Please provide following arguments <URL> <SUITE_ID> <TOKEN> <PROJECT_TYPE>");
		String url = args[0];
		String userName = args[1];
		String passtoken = args[2];
		String licenseId = args[3];
		if (!url.startsWith("http"))
			throw new ErrorHandler("INVALID URL.: Please provide valid url");
		if (licenseId.isBlank())
			throw new ErrorHandler("INVALID PROJECT TYPE.: Please provide valid license id");
		if (userName.isBlank())
			throw new ErrorHandler("INVALID PROJECT TYPE.: Please provide valid user name");
		if (passtoken.isBlank())
			throw new ErrorHandler("INVALID PROJECT TYPE.: Please provide valid token");
	}
}
