	package Mail;
 
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
 
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
 
public class GetUrlAndPassword implements Nlp {
    @InputParams({@InputParam(name = "HTML", type = "java.lang.String")})
    @ReturnType(name = "URL and Password", type = "Map")
 
      public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
 
          Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
          String hTML = (String) programElementsInput.get("HTML");
          Map<String, String> returnValue=null;
 
          NlpResponseModel nlpResponseModel = new NlpResponseModel();
          try {
              returnValue= extractPasswordAndUrl(hTML);
              nlpResponseModel.setMessage("URL and Temp Password Fetched : "+returnValue);
              nlpResponseModel.setStatus(CommonConstants.pass);
          }
          catch(Exception e) {
             nlpResponseModel.setStatus(CommonConstants.fail);
             nlpResponseModel.setMessage("Failed to fetch URL and TempPassword");
          }
          nlpResponseModel.getAttributes().put("URL and Password", returnValue);
          return nlpResponseModel;
      }
    public static Map<String, String> extractPasswordAndUrl(String htmlContent) {
        Map<String, String> result = new HashMap<>();
        if (htmlContent == null) return result;
 
        Document doc = Jsoup.parse(htmlContent);
 
        // 1) Extract Temporary password
        String tempPassword = null;
 
        // Try: find bold element that contains "Temporary password"
        Element bold = doc.selectFirst("b:matchesOwn((?i)Temporary password)");
        if (bold != null) {
            // look for nearby text nodes after the <b> element
            Node sibling = bold.nextSibling();
            while (sibling != null) {
                if (sibling instanceof TextNode) {
                    String txt = ((TextNode) sibling).text().trim();
                    if (!txt.isEmpty()) {
                        tempPassword = txt.split("\\s+")[0].trim(); // first token
                        break;
                    }
                } else if (sibling instanceof Element) {
                    String txt = ((Element) sibling).text().trim();
                    if (!txt.isEmpty()) {
                        tempPassword = txt.split("\\s+")[0].trim();
                        break;
                    }
                }
                sibling = sibling.nextSibling();
            }
 
            // if still null, try reading parent text and removing "Temporary password" label
            if (tempPassword == null) {
                String parentText = bold.parent() != null ? bold.parent().text() : null;
                if (parentText != null) {
                    parentText = parentText.replaceAll("(?i)Temporary password[:\\s]*", "").trim();
                    if (!parentText.isEmpty()) {
                        // often the password is the first token
                        tempPassword = parentText.split("\\s+")[0].trim();
                    }
                }
            }
        }
 
        // fallback: simple text search near the label if jsoup selection didn't work
        if (tempPassword == null) {
            // look for the literal "Temporary password" in full text and pick the following token
            String allText = doc.text();
            int idx = allText.toLowerCase().indexOf("temporary password");
            if (idx >= 0) {
                String after = allText.substring(idx + "temporary password".length()).trim();
                // remove punctuation/colon if present
                after = after.replaceFirst("^[\\:\\-\\s]+", "");
                if (!after.isEmpty()) {
                    tempPassword = after.split("\\s+")[0].trim();
                }
            }
        }
 
        result.put("temporaryPassword", tempPassword);
 
        // 2) Extract "Set Password" URL
        String setUrl = null;
 
        // Prefer anchors that contain the CTA text "SET PASSWORD" (case-insensitive)
        Element cta = doc.selectFirst("a:matchesOwn((?i)SET PASSWORD)");
        if (cta == null) {
            // some emails use slightly different anchor text (like "SET PASSWORD and GET STARTED")
            cta = doc.selectFirst("a:matchesOwn((?i)SET PASSWORD.*)");
        }
        if (cta == null) {
            // fallback: find anchor with common target strings
            Elements anchors = doc.select("a[href]");
            for (Element a : anchors) {
                String href = a.attr("href");
                if (href != null && (href.contains("oidc/login") || href.contains("auth-release") || href.toLowerCase().contains("set-password"))) {
                    cta = a;
                    break;
                }
            }
        }
 
        if (cta != null) {
            String href = cta.attr("href").trim();
            // If href is a tracking redirect that contains an encoded inner URL, try to extract inner https substring
            // Example pattern in your sample: .../https:%2F%2Fqa-automation-sasikala.auth-release.bnntest.com%2Foidc%2Flogin%3Forg_name=...
            int httpsIdx = href.indexOf("https");
            if (httpsIdx >= 0) {
                String innerEncoded = href.substring(httpsIdx);
                try {
                    // decode percent-encoding
                    String decoded = URLDecoder.decode(innerEncoded, StandardCharsets.UTF_8.name());
                    // decoded should start with "https://..."
                    setUrl = decoded;
                } catch (Exception e) {
                    // if decode fails, use the raw substring as best-effort
                    setUrl = innerEncoded.replace("%2F", "/").replace("%3A", ":");
                }
            } else {
                // not a redirect, return the href as-is
                setUrl = href;
            }
        } else {
            // final fallback: pick first anchor that looks like an auth URL
            Element firstAuth = doc.selectFirst("a[href*=\"auth-release\"], a[href*=\"oidc/login\"]");
            if (firstAuth != null) setUrl = firstAuth.attr("href");
        }
 
        result.put("setPasswordUrl", setUrl);
        return result;
    }
 
  }