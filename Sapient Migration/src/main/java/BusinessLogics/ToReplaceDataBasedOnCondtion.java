
package BusinessLogics;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;

public class ToReplaceDataBasedOnCondtion implements Nlp {
	static String missingdata=null;
	@InputParams({ @InputParam(name = "RequestBody", type = "java.lang.String"),
			@InputParam(name = "Condtion", type = "java.util.List"),
			@InputParam(name = "ExcelData", type = "java.util.Map"),
			@InputParam(name = "FolderPath", type = "java.lang.String") })
	@ReturnType(name = "Division Result", type = "java.lang.String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		java.util.List<String> condtion = (java.util.List) programElementsInput.get("Condtion");
		String RequestBody = (String) programElementsInput.get("RequestBody");
		Map<String, String> ExcelData = (Map<String, String>) programElementsInput.get("ExcelData");
		String folderPath = (String) programElementsInput.get("FolderPath");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();

		try {

			for (String object : condtion) {
				ReadContext ctx = JsonPath.parse(object);
				RequestBody = processNode(ctx, "$", ExcelData, RequestBody, folderPath);
			}

			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Succesfully Replaced Data with Missing header "+missingdata);

		}

		catch (Exception e) {
			 StringWriter exception = new StringWriter();
 			e.printStackTrace(new PrintWriter(exception));
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed To Replace Data "+exception);

		}
        nlpResponseModel.getAttributes().put("Division Result", RequestBody);

		return nlpResponseModel;
	}

	public static String processNode(ReadContext ctx, String path, Map<String, String> excelData, String json,
			String FolderPath) throws IOException {

		String columnName = ctx.read(path + ".CloumnName");

		if (isNullOrEmptyOrBlank(columnName)) {
			return json;
		}

		String condition = ctx.read(path + ".Condition");
		String operator = ctx.read(path + ".Operator");

		String cellValue = excelData.get(columnName);

		System.out.println(columnName);
		System.out.println(compare(cellValue, operator, condition));

		if (compare(cellValue, operator, condition)) {

			List<Map<String, Object>> actions = ctx.read(path + ".WhatToPerform[*]");
			json = getFromFile(actions, json, FolderPath, excelData);
		} else {
			try {
				List<Map<String, Object>> actions1 = ctx.read(path + ".OnFailurePerform[*]");
				json = getFromFile(actions1, json, FolderPath, excelData);
			} catch (com.jayway.jsonpath.PathNotFoundException e) {
				System.out.println("Expcet");
			}

		}

		List<Object> children = ctx.read(path + ".children[*]");

		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				String childPath = path + ".children[" + i + "]";
				json = processNode(ctx, childPath, excelData, json, FolderPath);
			}
		}

		return json;
	}

	public static String getFromFile(List<Map<String, Object>> actions, String json, String FolderPath,
			Map<String, String> excelData) throws IOException {
		if (actions != null) {
			for (Map<String, Object> map : actions) {

				String file = String.valueOf(map.get("file"));

				if (!"no".equalsIgnoreCase(file)) {

					Path filePath = Paths.get(FolderPath, file).normalize();
					json = new String(Files.readAllBytes(filePath));
                   
				}
				

				String headerName = String.valueOf(map.get("headerName"));
				String replaceToken = String.valueOf(map.get("StringToBeReplaced"));

				String datai= excelData.get(headerName);

                if (datai != null) {
                	if (!isNullOrEmptyOrBlank(headerName)) {
    					json = json.replace(replaceToken, datai);
    				}
                
                }else {
                	missingdata=missingdata+"  "+headerName;
				}
                
				
				
			}
		}
		return json;
	}

	public static boolean compare(String value1, String condition, String value2) {

		String normalized = normalizeCondition(condition);

		switch (normalized) {

		case "equals":
			if ("empty".equalsIgnoreCase(value2)) {
				return isNullOrEmptyOrBlank(value1);
			}
			return value1 != null && value1.equals(value2);

		case "notEquals":
			if ("empty".equalsIgnoreCase(value2)) {
				return !isNullOrEmptyOrBlank(value1);
			}
			return value1 == null ? value2 != null : !value1.equals(value2);

		case "contains":
			if (value1 == null)
				return false;
			if ("empty".equalsIgnoreCase(value2)) {
				return !isNullOrEmptyOrBlank(value1);
			}
			return value1.contains(value2);

		default:
			throw new IllegalArgumentException("Invalid condition: " + condition);
		}
	}

	private static String normalizeCondition(String condition) {
		if (condition == null)
			return "";

		condition = condition.trim().toLowerCase();

		switch (condition) {

		case "equals":
		case "equal":
		case "==":
		case "=":
			return "equals";

		case "notequals":
		case "notequal":
		case "!=":
		case "<>":
			return "notEquals";

		case "contains":
		case "contain":
			return "contains";

		default:
			return condition;
		}
	}

	public static boolean isNullOrEmptyOrBlank(String str) {
		return str == null || str.trim().isEmpty();
	}
	
	
	
	


	    public static void main(String[] args) {

	        // Create instance of your class
	        ToReplaceDataBasedOnCondtion obj = new ToReplaceDataBasedOnCondtion();

	        // Prepare input data
	        NlpRequestModel request = new NlpRequestModel();

	        Map<String, Object> attributes = new HashMap<>();

	        // Sample condition (JSON string)
	        List<String> conditionList = new ArrayList<>();
	        conditionList.add("{\"CloumnName\":\"ServiceEnhancementsCode2\",\"Operator\":\"==\",\"Condition\":\"empty\",\"WhatToPerform\":[{\"file\":\"int_all_NoService_Test.txt\",\"headerName\":\"\",\"StringToBeReplaced\":\"\"}],\"OnFailurePerform\":[],\"children\":[]}");
	        conditionList.add("{   \"CloumnName\": \"ServiceEnhancementsCode1\",   \"Operator\": \"==\",   \"Condition\": \"empty\",   \"WhatToPerform\": [     {       \"file\": \"int_all_Emptyservice_Test.txt\",       \"headerName\": \"\",       \"StringToBeReplaced\": \"\"     }   ],   \"OnFailurePerform\": [],   \"children\": [] }");
	        conditionList.add("{   \"CloumnName\": \"ServiceEnhancementsCode1\",   \"Operator\": \"==\",   \"Condition\": \"Safeplace\",   \"WhatToPerform\": [],   \"OnFailurePerform\": [],   \"children\": [     {       \"CloumnName\": \"ServiceEnhancementsCode2\",       \"Operator\": \"==\",       \"Condition\": \"empty\",       \"WhatToPerform\": [         {           \"file\": \"int_all_safePlace_Test.txt\",           \"headerName\": \"SafeplaceLocation\",           \"StringToBeReplaced\": \"@SafeplaceLocation\"         }       ],       \"OnFailurePerform\": [         {           \"file\": \"intAllSafePlace_Code2Test.txt\",           \"headerName\": \"ServiceEnhancementscode2\",           \"StringToBeReplaced\": \"@ServiceEnhancementsCode2\"         }       ],       \"children\": []     }   ] }");
	       


	        
	        attributes.put("Condtion", conditionList);
	        attributes.put("RequestBody", "{     \"CarrierSpecifics\": {         \"ServiceLevel\": \"@ServiceLevel\",         \"EbayVtn\": \"@EbayVtn\",         \"ServiceEnhancements\": [             {                 \"Code\": \"@ServiceEnhancementscode1\"             },             {                 \"Code\": \"@ServiceEnhancementscode2\"             }         ]     },     \"ShipmentInformation\": {         \"Action\": \"@Action\",         \"LabelFormat\": \"@LabelFormat\",         \"ReturnLabelsAtPackageLevel\": \"@ReturnLabelsAtPackageLevel\",         \"ContentType\": \"@ContentType\",         \"ServiceCode\": \"@ServiceCode\",         \"DescriptionOfGoods\": \"@DescriptionOfGoods\",         \"ShipmentDate\": \"@ShipmentDate\",         \"CurrencyCode\": \"@CurrencyCode\",         \"WeightUnitOfMeasure\": \"@WeightUnitOfMeasure\",         \"DimensionsUnitOfMeasure\": \"@DimensionsUnitOfMeasure\"     },     \"Shipper\": {         \"ShippingAccountId\": \"@ShippingAccountId\",         \"ShippingLocationId\": \"@ShippingLocationId\",         \"Reference1\": \"@Reference1\",         \"DepartmentNumber\": \"@DepartmentNumber\",         \"EoriNumber\": \"@EoriNumber\",         \"VatNumber\": \"@VatNumber\",         \"Address\": {             \"ContactPhone\": \"@ShipperContactPhone\",             \"ContactName\": \"@ShipperContactName\",             \"CompanyName\": \"@ShipperCompanyName\",             \"ContactEmail\": \"@ShipperContactEmail\",             \"Line1\": \"@ShipperLine1\",             \"Line2\": \"@ShipperLine2\",             \"Line3\": \"@ShipperLine3\",             \"Town\": \"@ShipperTown\",             \"Postcode\": \"@ShipperPostcode\",             \"County\": \"@ShipperCounty\",             \"CountryCode\": \"@ShipperCountryCode\"         }     },     \"Destination\": {         \"EoriNumber\": \"@EoriNumber\",         \"VatNumber\": \"@VatNumber\",         \"Address\": {             \"ContactPhone\": \"@DestinationContactPhone\",             \"ContactName\": \"@DestinationContactName\",             \"CompanyName\": \"@DestinationCompanyName\",             \"ContactEmail\": \"@DestinationContactEmail\",             \"Line1\": \"@DestinationLine1\",             \"Town\": \"@DestinationTown\",             \"Postcode\": \"@DestinationPostcode\",             \"County\": \"@DestinationCounty\",             \"CountryCode\": \"@DestinationCountryCode\"         }     },     \"Packages\": [         {             \"PackageType\": \"@PackageType\",             \"PackageOccurrence\": @PackageOccurrence,             \"DeclaredWeight\": @DeclaredWeight,             \"DeclaredValue\": @DeclaredValue,             \"Dimensions\": {                 \"Length\": @Length,                 \"Width\": @Width,                 \"Height\": @Height             }         }     ],     \"Items\": [         {             \"SkuCode\": \"@SkuCode\",             \"PackageOccurrence\": @PackageOccurrence,             \"Quantity\": @Quantity,             \"Description\": \"@Description\",             \"Value\": @Value,             \"Weight\": @Weight,             \"ImageURL\": \"@ImageURL\",             \"HSCode\": \"@HSCode\",             \"CountryOfOrigin\": \"@CountryOfOrigin\"         },         {             \"SkuCode\": \"@SkuCode1\",             \"PackageOccurrence\": @PackageOccurrence1,             \"Quantity\": @Quantity1,             \"Description\": \"@Description1\",             \"Value\": @Value1,             \"Weight\": @Weight1,             \"ImageURL\": \"@ImageURL1\",             \"HSCode\": \"@HSCode1\",             \"CountryOfOrigin\": \"@CountryOfOrigi1\"         }     ],     \"Customs\": {         \"ReasonForExport\": \"@ReasonForExport\",         \"Incoterms\": \"@Incoterms\",         \"PreRegistrationNumber\": \"@PreRegistrationNumber\",         \"PreRegistrationType\": \"@PreRegistrationType\",         \"ShippingCharges\": @ShippingCharges,         \"OtherCharges\": @OtherCharges,         \"QuotedLandedCost\": @QuotedLandedCost,         \"InvoiceNumber\": \"@InvoiceNumber\",         \"InvoiceDate\": \"@InvoiceDate\",         \"ExportLicenceRequired\": @ExportLicenceRequired,         \"Airn\": \"@Airn\"     },     \"ReturnToSender\": {         \"Address\": {             \"ContactPhone\": \"@ReturnToSenderContactPhone\",             \"ContactName\": \"@ReturnToSenderContactName\",             \"CompanyName\": \"@ReturnToSenderCompanyName\",             \"ContactEmail\": \"@ReturnToSenderContactEmail\",             \"Line1\": \"@ReturnToSenderLine1\",             \"Line2\": \"@ReturnToSenderLine2\",             \"Line3\": \"@ReturnToSenderLine3\",             \"Town\": \"@ReturnToSenderTown\",             \"Postcode\": \"@ReturnToSenderPostcode\",             \"County\": \"@ReturnToSenderCounty\",             \"CountryCode\": \"@ReturnToSenderCountryCode\"         }     } }");

	        Map<String, String> excelData = new HashMap<>();
	        excelData.put("ServiceEnhancementsCode2", "");
	        excelData.put("CurrencyCode", "20");

	        Map<String, Object> map = new HashMap<>();

	        map.put("Test Case", "BG1_International LargeLetter");
	        map.put("ShipmentInformationAction", "Process");
	        map.put("LabelFormat", "PDF");
	        map.put("ShipmentContentType", "DOX");
	        map.put("ServiceCode", "BG1");
	        map.put("DescriptionOfGoods", "Leather");
	        map.put("ShipmentDate", "YYYY-MM-DD");
	        map.put("CurrencyCode", "GBP");
	        map.put("WeightUnitOfMeasure", "Grams");

	        map.put("ShipperReference1", "OrderRef08");
	        map.put("ShipperDepartmentNumber", "");
	        map.put("ShipperVATNumber", "GB123456796");
	        map.put("ShipperEORINumber", "GB123456789000");

	        map.put("ShipperContactName", "Riley Lang");
	        map.put("ShipperCompanyName", "Company & Co.");
	        map.put("ShipperContactEmail", "naveen.maruthachalam@intersoft.co.uk");
	        map.put("ShipperContactPhone", "0123456789");

	        map.put("ShipperAddressLine1", "Beechwood Family Centre");
	        map.put("ShipperAddressLine2", "Former Road");
	        map.put("ShipperAddressLine3", "453 St Albans Rd");
	        map.put("ShipperTown", "Watford");
	        map.put("ShipperPostcode", "WD24 7RZ");
	        map.put("ShipperCounty", "Hertfordshire");
	        map.put("ShipperCountryCode", "GB");

	        map.put("DestinationContactName", "Conor McLean");
	        map.put("DestinationCompanyName", "");
	        map.put("DestinationContactEmail", "soummya.nair@intersoft.co.uk");
	        map.put("DestinationContactPhone", "0123456789");

	        map.put("DestinationAddressLine1", "1279 Landis Ave");
	        map.put("DestinationAddressLine2", "Maple Shade");
	        map.put("DestinationAddressLine3", "Waterford Works");
	        map.put("DestinationTown", "NewJersey");
	        map.put("DestinationPostcode", "28051");
	        map.put("DestinationCounty", "NJ");
	        map.put("DestinationCountryCode", "US");

	        map.put("PackageOccurrence", 1);
	        map.put("PackageType", "LargeLetter");
	        map.put("PackagesWeight", 100);
	        map.put("PackagesLength", 10);
	        map.put("PackagesWidth", 10);
	        map.put("PackagesHeight", 0.2);

	        // Item 1
	        map.put("ItemsPackageOccurence1", 1);
	        map.put("ItemsQuantity1", 2);
	        map.put("ItemsDescription1", "Mens Knitted Pant");
	        map.put("ItemsValue1", 26.99);
	        map.put("ItemsWeight1", 10);
	        map.put("ItemsSkuCode1", "SKU10256");
	        map.put("ItemsHSCode1", "611020");
	        map.put("ItemsCountryOfOrigin1", "CN");

	        // Item 2
	        map.put("ItemsPackageOccurence2", 1);
	        map.put("ItemsQuantity2", 2);
	        map.put("ItemsDescription2", "Mens Knitted Pant");
	        map.put("ItemsValue2", 26.99);
	        map.put("ItemsWeight2", 10);
	        map.put("ItemsSkuCode2", "SKU10256");
	        map.put("ItemsHSCode2", "611020");
	        map.put("ItemsCountryOfOrigin2", "CN");

	        map.put("CustomsReasonForExport", "Gift");
	        map.put("CustomsIncoterms", "DDU");
	        map.put("CustomsPreRegistrationNumber", "GB123456789001");
	        map.put("CustomsPreRegistrationType", "GST");
	        map.put("CustomsShippingCharges", 62.82);
	        map.put("CustomsOtherCharges", 37);
	        map.put("CustomsQuotedLandedCost", 47.74);

	        map.put("CustomsInvoiceNumber", "INV-12598");
	        map.put("CustomsInvoiceDate", "YYYY-MM-DD");
	        map.put("CustomsExportLicenceRequired", true);
	        map.put("CustomsAirn", "231.002.999-10");

	        map.put("CarrierSpecificsServiceLevel", "01");
	        map.put("CarrierSpecificsEbayVtn", "ebay1234abc");

	        map.put("ServiceEnhancementsCode1", "CustomsEmail");
	        map.put("SafeplaceLocation", "");
	        map.put("ServiceEnhancementsCode2", "CustomsPhone");

	        map.put("ReturnContactName", "Riley Lang");
	        map.put("ReturnCompanyName", "Company & Co.");
	        map.put("ReturnContactEmail", "naveen.maruthachalam@intersoft.co.uk");
	        map.put("ReturnContactPhone", "0123456789");

	        map.put("ReturnAddressLine1", "Beechwood Family Centre");
	        map.put("ReturnAddressLine2", "Former Road");
	        map.put("ReturnAddressLine3", "453 St Albans Rd");
	        map.put("ReturnTown", "Watford");
	        map.put("ReturnPostcode", "WD24 7RZ");
	        map.put("ReturnCounty", "Hertfordshire");
	        map.put("ReturnCountryCode", "GB");

	        map.put("MaximumAllotedWeight", 500);
	        map.put("ErrorCode", "");
	        map.put("ErrorDescription", "");
	        
	        
	        attributes.put("ExcelData", map);
	        attributes.put("FolderPath", "D:\\android\\InterSoft");

	        request.setAttributes(attributes);

	        try {
	            NlpResponseModel response = obj.execute(request);
                System.out.println(response);
	            System.out.println("Status: " + response.getStatus());
	            System.out.println("Message: " + response.getMessage());

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	
	
	
	
	
	
	
	
	
	
	

