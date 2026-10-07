package Business_Logics;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonValidator {
	public static void main(String[] args) {
		String schemaJson = """
								    {
				  "$schema": "http://json-schema.org/draft-04/schema#",
				  "type": "object",
				  "properties": {
				    "data": {
				      "type": "object",
				      "properties": {
				        "id": {
				          "type": "string"
				        },
				        "email": {
				          "type": "string"
				        },
				        "first_name": {
				          "type": "string"
				        },
				        "last_name": {
				          "type": "string"
				        },
				        "avatar": {
				          "type": "string"
				        }
				      },
				      "required": [
				        "id",
				        "email",
				        "first_name",
				        "last_name",
				        "avatar"
				      ]
				    },
				    "support": {
				      "type": "object",
				      "properties": {
				        "url": {
				          "type": "string"
				        },
				        "text": {
				          "type": "string"
				        }
				      },
				      "required": [
				        "url",
				        "text"
				      ]
				    }
				  },
				  "required": [
				    "data",
				    "support"
				  ]
				}
								""";

		String jsonToValidate = """
								    {
				    "data": {
				        "id": 2,
				        "email": "janet.weaver@reqres.in",
				        "first_name": "Janet",
				        "last_name": "Weaver",
				        "avatar": "https://reqres.in/img/faces/2-image.jpg"
				    },
				    "support": {
				        "url": "https://contentcaddy.io?utm_source=reqres&utm_medium=json&utm_campaign=referral",
				        "text": "Tired of writing endless social media content? Let Content Caddy generate it for you."
				    }
				}
								""";

		try {
			JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaJson));
			JSONObject jsonSubject = new JSONObject(new JSONTokener(jsonToValidate));

			Schema schema = SchemaLoader.load(jsonSchema);
			schema.validate(jsonSubject); // throws ValidationException if invalid

			System.out.println("JSON is valid!");
		} catch (Exception e) {
			System.out.println("JSON is invalid: " + e.getMessage());
		}
	}
}
