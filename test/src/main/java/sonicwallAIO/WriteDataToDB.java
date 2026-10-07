package sonicwallAIO;

import static io.restassured.RestAssured.given;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class WriteDataToDB {
	public static void main(String[] args) throws Exception {
		String moduleResponse = getRequest(
				"/customerextension/optimize/api/v3/project-assets?assetType=SCRIPT" + "&projectId=" + "PJT1001"
						+ "&licenseId=" + "LIC27000",
				"Authorization",
				"Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmNzVCeFN0YzBtOFNLSTdvejUzMURYYkZuSlAtZmpuaFZrQkdyckRIM0hzIn0.eyJleHAiOjE3ODY4NTc5NjgsImlhdCI6MTc4NTEyOTk2OCwianRpIjoib25ydHJvOmNlZjYwNjkwLTdjYWMtNDEzYi1hMDFjLTQ3YjkyZDVkMDYyYyIsImlzcyI6Imh0dHA6Ly8xMTUuODQuMTcwLjkzOjgwODAvcmVhbG1zL0ZpcmVGbGluayIsImF1ZCI6InRhcmdldC1jbGllbnQiLCJzdWIiOiJmOjc3MGMxZDQxLTJlMmQtNDUwMy1hZjliLWM3ZWIxMjRhMmE0ZTp2YXJ1bi5rbkBmaXJlZmxpbmsuY29tIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZmxpbmstc2VydmljZSIsInNpZCI6IjAwYmVmNDYwLTFhNjEtNDUxMS04NzZmLTk1NDBkNTI1NmEyOSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiY3VycmVudExpY2Vuc2VJZCI6IkxJQzI3MDAwIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJjdXJyZW50UHJpdmlsZWdlIjoiVXNlciIsImZ1bGxOYW1lIjoidmFydW4ga3VtYXIgZ293ZGEga24iLCJhY3RpdmF0aW9uU3RhdHVzIjoiQUNUSVZFIiwicHJpdmlsZWdlIjoiVXNlciIsImxpY2Vuc2VOYW1lIjoiU29uaWNXYWxsIENTRV9MSUMyNzAwMCIsInByZWZlcnJlZF91c2VybmFtZSI6InZhcnVuIGt1bWFyIGdvd2RhIGtuIiwidXNlck5hbWUiOiJ2YXJ1bi5rbkBmaXJlZmxpbmsuY29tIiwiYmlsbGluZ0N5Y2xlIjoiUXVhcnRlcmx5IiwiaWQiOiJVU1I1NjA5MCIsImxpY2Vuc2VJZCI6IkxJQzI3MDAwIiwiZW1haWwiOiJ2YXJ1bi5rbkBmaXJlZmxpbmsuY29tIn0.ZEvNz9d9QhEn3t_A87u6LcXvyLUIy9Vo8R4VqYDmqkTRmc-N93K255HLsrt4VaPGKgr53I2zcCMPHCI56t9QssHXwQ0ODBCpMD35nOyHBbGFqGDRHNIbrsv2ZlTqLPk5VLxj9IliQ84nO1hPyNVS--IBiU9Yuz2wphHS_6ZkZ9YOmZ7__b4OhDB6PabIGYd-f_vx0NEk2MDV1vNgAhGVJIxGhD1caUKg0TTlhaHFkTQ611g8WVV_zNoe9yyGD6cGbiOpGa8yQGlTNDJGEkWkId2AIArd4eenRFq_6lvHMujGM28ZKzKZK_98P6kdTENpKyzIFMVrxQx8qd5Ql3QEAg");
		System.out.println(moduleResponse);
	}

	private static String getRequest(String endpoint, String... headers) {
//		RestAssured.baseURI = "https://sonicwall.fireflink.com";
		RequestSpecification request = given();

		// Add headers if provided (expects key-value pairs)
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Headers should be passed as key-value pairs.");
		}

		for (int i = 0; i < headers.length; i += 2) {
			request.header(headers[i], headers[i + 1]);
		}
		ValidatableResponse response = request.when().get("https://sonicwall.fireflink.com" + endpoint).then().log()
				.all();
		System.out.println(response);
		System.out.println(response.extract().statusCode());
		return response.extract().asString();
	}
}
