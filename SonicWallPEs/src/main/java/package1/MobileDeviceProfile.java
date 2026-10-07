package package1;

public class MobileDeviceProfile {

    public int width;
    public int height;
    public double pixelRatio;
    public String userAgent;

    public MobileDeviceProfile(int width, int height, double pixelRatio, String userAgent) {
        this.width = width;
        this.height = height;
        this.pixelRatio = pixelRatio;
        this.userAgent = userAgent;
    }

    public static MobileDeviceProfile getProfile(String deviceName) {

        if (deviceName == null) {
            throw new RuntimeException("DeviceName cannot be null");
        }

        switch (deviceName.toLowerCase()) {

            case "pixel 7":
                return new MobileDeviceProfile(
                        412,
                        915,
                        3.5,
                        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Mobile Safari/537.36"
                );

            case "pixel 6":
                return new MobileDeviceProfile(
                        412,
                        915,
                        3,
                        "Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Mobile Safari/537.36"
                );

            case "iphone 12":
                return new MobileDeviceProfile(
                        390,
                        844,
                        3,
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile Safari/604.1"
                );

            case "iphone 14 pro":
                return new MobileDeviceProfile(
                        393,
                        852,
                        3,
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile Safari/604.1"
                );

            default:
                throw new RuntimeException("Unsupported device: " + deviceName);
        }
    }
}
