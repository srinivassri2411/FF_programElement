
package specializedAutomation2;

import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
public class GetPythonPath implements Nlp {
    @InputParams({@InputParam(name = "Image Path", type = "java.lang.String"), @InputParam(name = "Output Json Path", type = "java.lang.String")})
@ReturnType(name = "Python Code", type = "java.lang.String")

  public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

      Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
      String imagePath = (String) programElementsInput.get("Image Path");
      String outputJsonPath = (String) programElementsInput.get("Output Json Path");
      imagePath=imagePath.replace("\\","/");
      outputJsonPath=outputJsonPath.replace("\\","/");
      String returnValue=null;
      NlpResponseModel nlpResponseModel = new NlpResponseModel();
      try {
    	  File dir = new File(outputJsonPath);
          dir.mkdirs();  

          String pythonCode="""
import json
import cv2
import os
import numpy as np
from google.cloud import vision
from google.oauth2 import service_account

# -----------------------------------------------
# SERVICE ACCOUNT CREDENTIALS
# -----------------------------------------------
SERVICE_ACCOUNT_INFO = {
  "type": "service_account",
  "project_id": "acoe-expenses",
  "private_key_id": "6783558a3621c4619ff1d37ac0080d09617c37c4",
  "private_key": \"""-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCwE/Eeffed9DcO
TnRtCVM/PU72J7qyxDcb5FKzwzlFQsSLzqoc4bYFfnfSKppMaZGtV0Vo7iDaqz4W
rR9R1U1jsxtmkRBGGRPHwqJPRuVgJoZJiuS9Hy6GJFSmqR5CyxswtLJV++Ko2ZMi
j+MErKwA2TbJf4Gil+Mw1Wb30R9qsQjg+Ez3zkCTbg+pNgF3qpc1HTsBWfiu35Ci
C5FN6qYRe+ev7nNkgMyE1DET+9gZkNQ1t+wRzEMA2q0Usf6u2PHOubFsvHEwpL+p
Vkirjo4y3UnEcGW8LPuGnlKHIWwffnfxD0OrIhudPBMXRwNwrTIAAtCO0GicDUHZ
Xo8KCibbAgMBAAECggEAEkI/IrZVUx/cBbPFBvmMlGY7JfOAvMD0JgRLPzDjY3dF
Ampv4YbWZUN0EWsxjPL1lFAUdokbBxHy4k8ZjFbeK5SLJtusIFjBYFmiky364zmb
e0zX43S0Uly6Gn43ER4Wt7pFM/1mx7OoOlFJbVaqj3v23nZGbyHpAUtf3hrlcx60
AXCDGK+f/r0C4BBfQL2F04hJTTZhv3/8NpaUzc5TpbrAIuFfF9hcVZNPmInuAvrp
frlKW39BdJbFjLiAbUX8Ry/hMsYJY/5oGIyvirsym+611CjYzxtMkwLXTRq19WHk
tN0KIrZU5WxylsuFpmqc3iUUjp2f+fnVCjwEcm63IQKBgQD49CnS6HKhyRhqWf0G
HpbxXQS3BC9Q9MR6DfB1cmkHwp/qr08tBzeVtu9CL7sS7pW488M/whcw0BvNozr0
rp3szq2Qn+Rp3nHRP04COYwVvLQ52+3lZEERteePace7kagcuTqNy3+Fo5zDLUG2
BerpPuOgXCzcFIvwFawuDbhIFQKBgQC1D758H9Rv/dQAutWPknSWSN+HP1NgiV80
d18811HzIxKdHEo4AReidx4lcTqlpGRC0vPhBMKYmZuvPuk1espnzM+xMwgWnRfO
W/1sSYZpul4It+QafNVS5pWGsf4dCbME7VSsPVao0aRvSJD2jdtS0jnBnMirIbrq
PzZFUcj/LwKBgF5AsoTzFFNEO0Yog68JbcQ7n0lbAD76LFi5ZkxQ4IrbmIfVj3hQ
vy6jb7nzMDRet7iqMUW2SellIOGd5vg0E/5P0U5r+qJKYXUDLpmRCARCyUkOhubO
KgR2hhC5z7Cn2KnnRs/gN4t1AiiyvaoYW3OrJ6rSuvknnkNrMSDOePJxAoGAXP8a
Kmy6006/HiTYXeauRSZm0POgonvMT4lkfD5n1AbWn6WRJVTCS+r8q+SNZ9iYuo3Z
P2EPhguXoBoi9Bn+bfQPVQdOBzdmq9Ipxv24sQNNGxNAcK5fbzYJ5OX1C/sYXNZ9
X/4dTZZn69SMyGYyGUbq7VvW4bbwZzJScKZCI3cCgYEA3gI2U3lVfI7AclOl2SoF
6+HIRdX/iWZbWKIVfokHcKId2uIROsaX7DtnUByarg14W5rE6CNUPkAUHF6Y9Z1n
00xyylVwedYK4otWVMh3BrHiE33bNzXCYGL14xHk5QZpnO9TKlvlsHiOJkCOybFL
98y/Y843AuyOx33WwcxYDaQ=
-----END PRIVATE KEY-----\""",
  "client_email": "barcode@acoe-expenses.iam.gserviceaccount.com",
  "client_id": "105024418132038617831",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/barcode%40acoe-expenses.iam.gserviceaccount.com",
  "universe_domain": "googleapis.com"
}

# Create client using service account info
credentials = service_account.Credentials.from_service_account_info(SERVICE_ACCOUNT_INFO)
client = vision.ImageAnnotatorClient(credentials=credentials)

# -----------------------------------------------
# COLOR RANGES (HSV)
# -----------------------------------------------
COLOR_RANGES = {
    "red": [
        (np.array([0, 70, 50]), np.array([10, 255, 255])),
        (np.array([170, 70, 50]), np.array([180, 255, 255]))
    ],
    "green": [(np.array([35, 70, 50]), np.array([85, 255, 255]))],
    "purple": [(np.array([130, 70, 50]), np.array([165, 255, 255]))],
    "blue": [(np.array([90, 70, 50]), np.array([125, 255, 255]))],
    "teal": [(np.array([80, 50, 50]), np.array([95, 255, 255]))]
}

# -----------------------------------------------
# Extract OCR number using Google Vision
# -----------------------------------------------
def extract_number_google_vision(crop):
    _, buf = cv2.imencode('.png', crop)
    image = vision.Image(content=buf.tobytes())

    response = client.text_detection(image=image)
    texts = response.text_annotations

    if not texts:
        return None

    raw = texts[0].description
    digits = "".join([c for c in raw if c.isdigit()])
    
    return digits if digits else None

# -----------------------------------------------
# Detect colored overlays
# -----------------------------------------------
def detect_colored_boxes(img_path):
    img = cv2.imread(img_path)
    if img is None:
        return {"error": "Could not load image", "count": 0, "barcodes": []}
        
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    results = []

    for color_name, ranges in COLOR_RANGES.items():
        mask = np.zeros(hsv.shape[:2], dtype=np.uint8)

        for low, high in ranges:
            mask = cv2.bitwise_or(mask, cv2.inRange(hsv, low, high))

        cnts, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        for c in cnts:
            x, y, w, h = cv2.boundingRect(c)

            if w < 120 or h < 80:
                continue

            crop = img[y:y+h, x:x+w]
            number = extract_number_google_vision(crop)

            results.append({
                "color": color_name,
                "number": number,
                "bounding_box": [int(x), int(y), int(w), int(h)]
            })

    return {
        "count": len(results),
        "barcodes": results
    }

# -----------------------------------------------
# RUN
# -----------------------------------------------
if __name__ == "__main__":
    output = detect_colored_boxes("<<<<<INPUT>>>>>")
    # print(json.dumps(output, indent=4))
    os.makedirs("<<<<<OUTPUT>>>>>", exist_ok=True)

    # Save to result.json
    with open("<<<<<OUTPUT>>>>>/result.json", "w") as f:
        json.dump(output, f, indent=4)

              		""".replace("<<<<<INPUT>>>>>",imagePath).replace("<<<<<OUTPUT>>>>>",outputJsonPath);
          File file = new File(dir, "Test.py");
          try (FileWriter writer = new FileWriter(file)) {
              writer.write(pythonCode);
          }
          returnValue=file.getAbsolutePath();
          nlpResponseModel.setStatus(CommonConstants.pass);
      }
      catch(Exception e) {
         nlpResponseModel.setStatus(CommonConstants.fail);
      }
      nlpResponseModel.getAttributes().put("Python Code", returnValue);
          return nlpResponseModel;
      }

  } 