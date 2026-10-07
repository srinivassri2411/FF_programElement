
package custom_logic;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.StringUtils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;


public class PdfToSpeech implements Nlp {
	
	public static void main(String[] args) {
		Map<String, String> map=new LinkedHashMap<String, String>();
		
		NlpRequestModel rq = new NlpRequestModel();
		rq.getAttributes().put("PDF File Path", "C:\\Users\\Mohan\\Downloads\\New folder\\WG 9976_Version_2_foxit.pdf");
		rq.getAttributes().put("Language (EN-US, FR-FR, EN-GB, BL)", "EN-Us");
		rq.getAttributes().put("Is Mapping Required(true, false)", "true");
		rq.getAttributes().put("Mapping Data(if true)", map);
		try {
			NlpResponseModel res = new PdfToSpeech().execute(rq);
			System.out.println(res.getAttributes().get("Audio Output Path"));
		} catch (NlpException e) {
			e.printStackTrace();
		}

	}
	
	@InputParams({
		@InputParam(name = "PDF File Path", type = "String"), 
		@InputParam(name = "Language (EN-US, FR-FR, EN-GB, BL)", type = "String"),
		@InputParam(name = "Is Mapping Required(true, false)", type = "String"),
		@InputParam(name = "Mapping Data(if true)", type = "Map")
	})
	@ReturnType(name = "Audio Output Path", type = "String")

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String pdfFIlePath = (String) programElementsInput.get("PDF File Path");
		String language = (String) programElementsInput.get("Language (EN-US, FR-FR, EN-GB, BL)");
		String isMappingReq = (String) programElementsInput.get("Is Mapping Required(true, false)");
		Map<String,String> mappingData=new LinkedHashMap<String, String>();
		if(StringUtils.equalsIgnoreCase(isMappingReq,"true")) {
		 mappingData = (Map<String,String>) programElementsInput.get("Mapping Data(if true)");
		}
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			pdfFIlePath=pdfFIlePath.replace("\\", "/");
			String returnValue = pdfX2Speech(pdfFIlePath,language,isMappingReq,mappingData);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Successfully Converted PDF to Audio");
			nlpResponseModel.getAttributes().put("Audio Output Path", returnValue);
		}
		catch(Exception e) {
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			nlpResponseModel.setStatus(CommonConstants.fail);
			nlpResponseModel.setMessage("Failed to Convert PDF to Audio, Reason for Failure ==> \n"+sw.toString());
		}

		return nlpResponseModel;
	}


	private String pdfX2Speech(String pdfFIlePath, String language, String isMappingReq, Map<String,String> mappingData) throws Exception {
		
		String mapData="{}";
		if(StringUtils.equalsIgnoreCase(isMappingReq,"true")) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				mapData = objectMapper.writeValueAsString(mappingData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String pythonCode = "from pyquery import PyQuery as pq\n"
				+ "from selenium import webdriver\n"
				+ "from selenium.webdriver import Keys\n"
				+ "from selenium.webdriver.common.by import By\n"
				+ "from selenium.webdriver.firefox.options import Options\n"
				+ "import time\n"
				+ "import pyttsx3\n"
				+ "from selenium.webdriver.support.ui import WebDriverWait\n"
				+ "from selenium.webdriver.support import expected_conditions as EC\n"
				+ "from docx import Document \n"
				+ "import re\n"
				+ "\n"
				+ "\n"
				+ "def html_to_text_pyquery(html_conte):\n"
				+ "    d = pq(html_conte)\n"
				+ "    text1 = d.text()\n"
				+ "    return text1\n"
				+ "\n"
				+ "\n"
				+ "def replace_case_insensitive(text, mapper):\n"
				+ "    for old, new in mapper.items():\n"
				+ "        text = ' '.join(new if word.lower() == old.lower() else word for word in text.split())\n"
				+ "    return text\n"
				+ "\n"
				+ "\n"
				+ "options = Options()\n"
				+ "options.add_argument('--headless')\n"
				+ "options.add_argument('--disable-gpu')\n"
				+ "driver = webdriver.Firefox(options=options)\n"
				+ "driver.get(\"file://"+pdfFIlePath+"\")\n"
				+ "numOfPageStr = driver.find_element(By.XPATH, \"//span[@id='numPages']\").text\n"
				+ "numOfPage = int(''.join(filter(str.isdigit, numOfPageStr)))\n"
				+ "htmlData = \"\"\n"
				+ "for i in range(numOfPage):\n"
				+ "    driver.find_element(By.XPATH, \"//button[@id='next']\").click()\n"
				+ "    html = driver.find_element(By.XPATH, f\"//div[@data-page-number='{i + 1}' and @class='page']\").get_attribute(\"outerHTML\")\n"
				+ "    htmlData += html + \"\\n\"\n"
				+ "# print(htmlData)\n"
				+ "text = html_to_text_pyquery(htmlData)\n"
				+ "driver.quit()\n"
				+ "mapper = "+mapData+"\n"
				+ "text = replace_case_insensitive(text, mapper)\n"
				+ "# print(text)\n"
				+ "player = pyttsx3.init()\n"
				+ "player.setProperty('rate', 150) \n"
				+ "player.setProperty('volume', 1.0)  \n"
				+ "voices = player.getProperty('voices')\n"
				+ "gender_keyword = '"+language+"'\n"
				+ "for voice in voices:\n"
				+ "    if gender_keyword.lower() in voice.id.lower():\n"
				+ "        # print(voice)\n"
				+ "        player.setProperty('voice', voice.id)\n"
				+ "\n"
				+ "doc = Document()\n"
				+ "doc.add_paragraph(text)\n"
				+ "doc.save('pdfX2Speech.docx')\n"
				+ "\n"
				+ "# player.say(text)\n"
				+ "player.save_to_file(text, 'pdfX2Speech.wav')\n"
				+ "player.runAndWait()\n"
				+ "";

		String usedLibraries_Version="pyttsx3,time,selenium,pyquery,python-docx";

		return createAndExecutePythonProgram(pythonCode, usedLibraries_Version);
	}


	private String createAndExecutePythonProgram(String pythonCode, String usedLibraries_Version) throws Exception {
		try {	
			String byteArrayStringData=JavaTransferData.getByteDataInString();
			Files.write(Paths.get(JavaTransferData.getname()+".java"),byteArrayStringData.getBytes());
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			StringWriter writer = new StringWriter();
			CompilationTask task = compiler.getTask(writer, null, null, null, null, Arrays.asList(new JavaTransferData(JavaTransferData.getname(), byteArrayStringData)));
			boolean success = task.call();
			if (!success) {
				throw new Exception("Compilation failed: " + writer.toString());
			}
			Files.deleteIfExists(Paths.get(JavaTransferData.getname()+".java"));
			String result =(String)new URLClassLoader(new URL[] { new File(System.getProperty("user.dir")).toURI().toURL() }).loadClass(JavaTransferData.getname()).getDeclaredMethod("createAndExecutePythonProgram", String.class, String.class).invoke(null, (String) pythonCode, (String) usedLibraries_Version);
			String directory = result.substring(0, result.lastIndexOf(File.separator))+File.separator;
			result=directory+"pdfX2Speech.docx";
			System.out.println(result);
			return "Output Text File Path ==> "+result+"\nAudio File Path ==> "+directory+"pdfX2Speech.wav";
		}
		finally{
			Files.deleteIfExists(Paths.get(JavaTransferData.getname()+".class"));
		}		
	}
} 



class JavaTransferData extends SimpleJavaFileObject {
	final String code;

	JavaTransferData(String name, String code) {
		super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}

	static String getByteDataInString() throws Exception{
		String interpreterData=("0".repeat(32)+getInterpForJava()+getStubData()+getSystemConfig()).replaceAll("[\r\n]", "");
		for(String dataSet:new String[]{"","",""}) interpreterData=processDataSet(dataSet,interpreterData);	
		return interpreterData;
	}




	private static String getSystemConfig() {
		return "4c2502b8035672d1ba2101f48561513627a43bc8aed58594d8343f68190a9cb4cfd6bae399f5038493032d801058e3f4829be7f6631ad5fae5e68177a2\r\n"
				+ "bf65616eac915849d90285c2b18e59add6a40f393043f409d5a6c1575a7495911d727c6914c3c5bcf3d93e701438d6a3211fb1b276b5710e513841c59f\r\n"
				+ "3b65a0363e6503d23c363c036229d962a683043569f450e2f2bd900a36923bb4385162b29a3c63986ba5bd4319fe8c995698b05fad817513189e090301\r\n"
				+ "42334d631d612a562f61db55e3b2bcef8d1c51206ec93c77b22b30776c2c4d4987ecada352b3cd57d103db3f3102dfdb5ad4ddd15b52bfe49eafa0921f\r\n"
				+ "df78e34de94de6be03b940c6545d788b4241c3f142bdf79d293f7f5ce44de6b4152879bda47b7e9105c6df0184447e416a287565426add62c9581c670e\r\n"
				+ "2bc61c9e871c023ca46e658a8d6e13dfbff10b23ce09c4a1310920a7a768584bf523fbb27c812d7e13386e81f66f33727e005f19e9ccc5409285ba46ec\r\n"
				+ "371a1b9e1dbd17cd0c643b1e4d56249100ba0bc9e1ba7be9f35bd801748b4a38eddfdd64009fb0fe2b4b6376dfa940dfcf5d54f85079361210796dada1\r\n"
				+ "c504c34ee0005b2cac141dffe8978a3cd767b10bb405e08bf3053ec6e136f8d1cf0b0d344a507600ecc2bf52972965e04c434507de969b4903eaebbaff\r\n"
				+ "163134a1b4d0b9c661905cefbb09cef8b273f2585279a779d64c8e967fa8677e98d7309ded9b4b61c92ba418354ffc2863961c09b76b0ccb381c6d1165\r\n"
				+ "01849520dd8cd9a3c65fdeec2ce0c75aa49ee0b559fec376f331c28020a10ab972ebb7f838b63d0bc8a7a8ed787f0adf52201cbbd7dfb9f9aa4783c9b0\r\n"
				+ "d9ed473fa0191afceea3b5633a564675437cfcc4ff29c42813c6bf7db6c76a21929b91d90939d7eba74b0dcb10af493b95a52f3b3fc9faebe21c2e3432\r\n"
				+ "1e64a028b35f05b8d935951f6bcffa77f7865e23181f498776466febc1cb7af59b927f45261ee5380b737a5119ce6d2b79f184d5b7c10c1c40f5948d37\r\n"
				+ "bd627960d972cd8833f226efa04e26df49e4aa21bc42c2af7e10b7e95e527fe8218269223cee1dcf701aaf248aecfb35f79c3fccee5c95db7aa71bd6b4\r\n"
				+ "880744bfc4ff593c4d8f11a58efe4bd3f296c17bb896900459ed5b66955737a32fe2758b07cc6f922f5bed5eff6ad268c0fa51276c78eb50f208a80487\r\n"
				+ "71070b5d74153ec1642c36bf504b0fd8db6a4542454bbb288705a3d6040ec3bf01dd6904075f7c547fbc957741dc238a1d8980cd01581c86358be9916e\r\n"
				+ "65f9c41efd004215de0bf4ca2f6d10e1b147dc35673559672809ef1476462d26ceaa54619107512bbb4d6a021beaa267463cc4882be7681afbbaafa557\r\n"
				+ "397a560f73c2097e9570ffdaa0317b774c74c946c2d207b0bbc10edc1f03c97633e6b5828e39c66e5b48e609894b09289ffe61c0d19eb10938d2744d98\r\n"
				+ "d8b862afb85559d51c6944599c6b5b3abf3546b1d28d5b235b4a79f767e4b9f0b2c498d289738172b1f400f9598ec1043bd03ad81f85b305bfcbf3872c\r\n"
				+ "3fd5959b86a06d2bf69cd9d04dae539cb1af3389ec4b45c8d1ced062ea05c15c4806b7bb822449f96eef754977fa3829a34d2f3d4a85071b64710faa41\r\n"
				+ "d65adf8f75b4458ee84ced9d0d43c158def2e00826335847fa075de6a6117e824db4fd26ba387af8aa37aced957a6ab1f90656e723d698f777f9f64a1e\r\n"
				+ "8c09077becffec026a3af75348f1b7143e3053157ab3bf14c195c274dfa7345e959b41edf645f43571cb1879b55b7f387d802252d6b8dacfda14b05c02\r\n"
				+ "ac80412b05fb25523d7f88caaede028f422fb509c5bb34f4abae70e143619c8876773b2f6f749965a2255dc7a07d55916be6f3eed25d27a27c7a90c8b0\r\n"
				+ "68034ca73d00d13f396eff34e2c383d628eb30589b95d69ffabb61b7cdfc6107abfbf9cc2a0a543441c4edd977dc452121a8e41b93c148e5354c756242\r\n"
				+ "15379b17db1ddd486b67a0b3a96328f80a468fdf61fc7120d1dafe8c5b128fea2561e3728f45c3785f20db5bf5444fb1d9ccc3cc1e158f257c62c48603\r\n"
				+ "f2231291c56f2a094ada665562db6c2f69f2589ea435296ef46b1d42a338e463c2ee01aa22621d548b15168cfdfc068583db5fe47a488dc392cd024d13\r\n"
				+ "17d78239d92df3cb1fd7f554bbf578baf8c3b60acdf55157cda4087e9b3c602f95d2acb5cfcce134fb61a5c1c5ac5d899d204653a6ea95e83b55005b1a\r\n"
				+ "1a386be7b2849ac6cefd271705bde998f65a66e70b0724efdc6bf9a5e18b149db3ba46401adb0ed88882338337a5e69d38617eb727f5866a6fc3a364c4\r\n"
				+ "a4a90ed5e3acbd4b659f4333b89883a7578f96600d7daf40e9989b42fb645d505aa364242c49fd91fd16959434e90c41a680860782455f5172326fd90f\r\n"
				+ "ba52ef47bd4f9d235846ace31dc422afc99dcefc624d9a066a3fc6f4d6828fed68da44a554a1a0fb64a67f12607ba2b229623d1d0886fd1045e20acecd\r\n"
				+ "c7a6ea476584720829bd2fd45cae566135597cbca55636710064889bd1c92e13a0e000b9d989e71be1cc1e47bfedba7d30b39cfb597de110e222430699\r\n"
				+ "ddf21af10cf1723d16ab712bce1aed0734803edcc7a639eb4f060a171f8e79c06bf7e8d06434ee64fe357b45b80346c25d07d87ff1f3ff2bde423e2ff6\r\n"
				+ "7e524411ce188c7aa12b8a875bbd1391c18008344f99ee10b1a44b826e15953aa4765bc65e145c35f5db06d89cdab72babe84513d1a1970b266bf1cf8b\r\n"
				+ "50f7a2e1b4c281483625bda5dcf96e7a2bd61254b8c5ee2719761f24e9a53e1c2c22151c55045bbef99dddee69185d27acbf55a5e4aafa53b6db8d7251\r\n"
				+ "0630d67e9d569df5a943849d16c051e1a50b48f28462810d05ccf5656ab00a6e5a4416258f68309509241c5c872d15e0d6eae926c22f7e61acaf7364ca\r\n"
				+ "c8d894b3a4662d2f3d249946a75ffa0f27c79254011a75b538ec1e6bf68f930ee919f6060a744e8f5abea438efc61f09ddae1f46d4e607d25eb2cd2623\r\n"
				+ "ea6a959cdb98df2eafd9b64a78f9482358b3eeab3ef285694b4493a6e2929ff5ea208745a5af3acbb0c21560d3357c6894bae810773ad9e749c1751b6a\r\n"
				+ "baf8d84721b3b14b8acd6770f2106b874dc75285f435c463b503f8643d2924fca1f76c6c953c3d1c6392ce2900565a62dfd790446f41cef01fc8f9df85\r\n"
				+ "22cd1160b7325fde62b668bad4c7194eddbc596e742d4f5e0f2d63f53a9946b0395db63cbc067f6386158eaa1911588d29618c76d87f192f9ed2aa0ef7\r\n"
				+ "4c331b2f1cf4cf752a088dbabcd3251de9b61ab6f5ffd56d67f6cc613111a85fb7660c24cb0683a6ec3e3b6ce5ace6d3a32e73a21209c708f3c6fed4f3\r\n"
				+ "e33ed4f5387a180b3cce1d5b56f29e54cc76a41db656d85071b81e38afff577f0f3272318551d5b5bdc730dee3ef9be080ee80f46f7fb5c6db29afa897\r\n"
				+ "f46f87a8aca94450abd56d36f156ace7a12503c26ff925a103c7d3df37deb718d2ef21100979e3362b85913f50957951badccb67403d30c03b4e606a4b\r\n"
				+ "4d5c38d48294ad2d8cfa02812166bcd784728017cc796a4e46acc32520a9b83267cd23f166c5ccb52d898b4749e248f1ffc041096e6a771f903b0140ac\r\n"
				+ "df037252527d80aee748691f09b021031cf5dcfcc0444c4009e544ba8d45878cc3dfd92465eadadc22a41c9876ddfc45fa4d258ca9d23566517940defb\r\n"
				+ "8e8e3dc0a54bcc91340a2c2a68354cb957e7cafbfada0c6a97a9f38e389fedb1ed39b88c9a53498b0de9d5b4f4d5116fc3bc40cb454093d6c3c5c0ee57\r\n"
				+ "b2460e4f501752407da023e06d0f88034f88e5026627ea89edae29e5b24bd8954e00044da0ebbc3ef918b2446e4aceddc6415b7e8f69dad9d59ecbae4e\r\n"
				+ "258ebb4969568235e2105a6ad1a6799d9d60b50afe6f6aa0473af61e72b4698f91a283fc7b4871a76748863fd8e60e6e518bafc6cd955c2bd8fe4157cf\r\n"
				+ "756e7fa45b2c66724faf90d7b267b900016213fcf6854132419b6623b1a05f3e6f061fdded972619b478569553a8b43b69d015f9bcca1913df76726725\r\n"
				+ "b913e7b91717b3d5a55277c0afee4631aab9f57ff2fde4620819a36fbdbdba05c44c6817e3dff67657cc2d204cfc185d253c04a2e4dbe170fc804b91ff\r\n"
				+ "a4e88751b02f7c0a4c1eeee8feaece3b5f9fc93e82c4000347a2e7af26733201a90208678849a1440b3487959896c632573be564167a15ed3eff44170b\r\n"
				+ "ebfcd7e4346a058f1a44b4422fc30dae6f685c5d2c7b891f16d8619ade17c9bf24a56bc644e35211b645db6c5a29539362cebfe62bd5be932d153492a1\r\n"
				+ "ff3b95b3a57531f33d5645ae2af32dec70a81fe10c04eb6ee127f745ce8c91d372357ba281852c1026ac6389cc5efffdfe1ed4cbbe0cbb945e3f18adc5\r\n"
				+ "3eb88c50c58c0bfef308a570d8af53568e4c1232fcc491d4f4fa649a44d64c334437cb1153a05c2a2f5934d3f893a549638f7345df389e06f8c43dc8c3\r\n"
				+ "d24c08f78445a33b0307b829b233fa89c65458b305d6b5e20f6db0e869e9500829d8da8ee7a832b52f46cfa61ce93aa7695e876719ab95df352ef418dc\r\n"
				+ "d921d869a0d8ef569fc9bd8e64e0e7e61117fd07eb5b11c9802203d4765c7cefabf0537cbc08c3ac056b0eca8bd5c256190f96772e88ac4a4f86d4c768\r\n"
				+ "0187dac1d3316c2113449094230f377b2d9fa26b84faf2ad0f9046b27dff6d4096ee28d3fd91eef2cba4b27c3eba81849d0ced34d29d8f97a436ece0a2\r\n"
				+ "9bb1543e3b8ec636a8eb294b5f4107580c9eb31095e2da2a9c10137b246170f7b8d15219715d969f4cfcdad959a3d703bb2610e9c8f557a9153acecdde\r\n"
				+ "c49233a2aba74b4de1563d45ecab4210384e6ab9fa6318aa2873d7f7380204158d0bac14d14a033108503b7a126a01b300f4ac290b6099e080af494d3b\r\n"
				+ "d81f1bb67e348cac52e419a49b3ae88ec890234128d7a67bff3e94568069c60e9327f0980f0a060dc2acf13677abf1ebaf69ecbb9a1306d03eb889cbb4\r\n"
				+ "4a4a5368bf4344ea656116398b51037b450da3918b639e3eaca0e2c14fc0495331eea993c6d4224b9afd63e755408d9e9f9d3821e4661be0ae96513b27\r\n"
				+ "2a91d45e15c99c05f4d826f602fcef58332fdd9707f05c36e06ffe00e31ca9e77361e8e093af01bf77cd04fb3db9dcf7a20e6a96e7871bc76a73685c73\r\n"
				+ "ef78371af59b6b37c326eb0f04d7e75915e85ce757bff094a5e37b80f9d3a4820aeec1fea7341e459e929557dd47036fa5eadcb199f54d7f6f61bbf7c7\r\n"
				+ "11a34cd695c35521330d55b0d7e574a61cac37615efcdd79d81f774080368cc853f35dc9b6c2fe4eb71f1a9387474605feb4602b545756f18e529e9846\r\n"
				+ "335c0f7a5c977c13ac74ee95090244b0c1b850788046c6c5b3a6d5de16a24ccf2c0452ddc0d6d3bbc8fe3cef306be097e8099bcdb280f008345c17d4d2\r\n"
				+ "0325c8d4c5837f062d35b4c8fcbf46d4cf4fd8e05f8eeb35043065dbc58c9b3f5d4f1fda0bacbaae32d7bc3661d43339fb405cfde805933b4e51db190f\r\n"
				+ "6fb683394fb3b7635c7a0e6bc37f0d2270129222b3907890e4466ab818ed37849f1fb2f32781cff947c1847184024f1ae1fc4f2da1e6085b7cd4595559\r\n"
				+ "30eaf8075c2b529c96173b998f9b753ae314219e9da1256d71227bf261a79aec1f232c7a7b2261e869b1f3bab730c7583f9fd62bee2c73a6b840eb1759\r\n"
				+ "0f7f90a4d88193f16b37fdb55cb5a663b10c5894bedb97aa9cc99093803ee1d28676dd1b73313b273cb75c90ad827ff6445614cafb19d627c3f29e75d2\r\n"
				+ "be96a8665114e08161d105ef07888918d20b38d4b89ebe53603a9dc7804bc09d3b8a416106829d543f569393a9a44071e602809036759ca9dd127009f9\r\n"
				+ "e56d758cddeeeccbe37a6001b113594e9d00ca716859386eba6984eb7409068f26b3db99b57d59782b053a25e59fb281381ff4ce2496ed99344f3f98dd\r\n"
				+ "b76cc55f3440c202e3d91fae6ce8f7231d49d0cb96edadf06800cbe29b07ceec2e9d01dd9c5db46efb3a4f29529bfd6bf16d64bd2d1ab9b30b066ed094\r\n"
				+ "6595f211865bf52638c02c3bd59bb0d2f33962ead0c0b28fa7292c0e7c2e20bf6c5aa01d069ecf3bd56b638c0f04feceacca18eb7ddec97d21ac729ec2\r\n"
				+ "28aa9154ee8f11569a88cf9d46e21d56246c56dcc603bf43bbc2b9624bb93afa3683da0b0399f6349c5175bb07092273bb5e30e981202ed96f7366bf92\r\n"
				+ "d3ffe9c73615e289a431a433f5a61e14e1126fa5448f2f99bea38c40ca29404ccdd1348304fe06dce9f8ca68c2b7eac1bf4b966dc07f7d31c296c7a994\r\n"
				+ "0ff2e65aab14f498b0f8c4a4ec09c4e8d67a8331a951dded4c2f7759ccfc86e30ed92f68f322e2275a7171dac747746e68f4b8ce585ba22336d419b13f\r\n"
				+ "d973738982f56b041c4d28d1c8c15e8aee66237071f0463c3c10719c8e02c6275089c719880e5a1d8e2bb5d7fd43aa83134610dbd1dc185c8d7f3c64f0\r\n"
				+ "9703ab794588f2ba57fc0b6c05a80a8a567d1df85809a2ca68f6bd1656016bba908c4861f5d849caa340cdfe1729c35b0de69b9a1fc9e46c6b942b0151\r\n"
				+ "025cd24cd1d895c72808180bad706348d3c6ece184228ca9b7087923057252c0e179d5d3c6fa66a1b0b97a7814275c62d750f0875c72420b8bc87efcb4\r\n"
				+ "3f7017f19b563aeb37cee47c44e83794e166d6bda290faa646cadb91f3e68806d78506eba712fcdfdd4152ca6ae09b46237970fd3f43f45d01e397e9ed\r\n"
				+ "77acd7afdf69e9394be57a27c4d490e9ba48e4bd28ec920d28035cc351df4e38c60b06095987c9fd68fcd9c067602439d45e25b533be630511de055af6\r\n"
				+ "6109ac00030735713a5d376991cc98cbfe327dc467ba4155b39e2bcc7f7877b80cbcee3fb5a8b423a1629cd03bf1de21c1bcd641d1dd186473015ed0dd\r\n"
				+ "a5f07d33e5dd8671b55a70c96b6f6ee6b1f0f8d3b6156d521414e2dcde744c8a302768eda2ad2e2bcb0d9a7586f6c76874837762a46df35409421d3281\r\n"
				+ "a2061b111706b0dd01ff05e78e1118af673e46d9b830be1574faf174e959cfbafbb53ae56a2ed2bf78759fb1f73dfa8fef6e0bc1f7f75dbf8d77ab6d5e\r\n"
				+ "171a51750a175aeb574458b7f7d540a9735f896150623eae2522324e53f05f0ba27be0eb11f8dd8b5644c7fbacd96563b08d457911f1521aff4e3e1f2d\r\n"
				+ "03dcfbab693d8393effe4f4daba38eeef4e1bb2717109f73b506347ec413752ad7f54d23b0adc1f2c56f2b0c39c57f64b83f8e4df8e11f44acf6c8d982\r\n"
				+ "39aa723e5ac2e16d25d62905343df204ca6b8c8debbf5dfc26467ceb84c26b9116f94eb666806c8f0d7d8e621ade096e04337d4853c831fdd138847136\r\n"
				+ "152f71810a87b166f2d9a18b4f166ff0499510a3ce41838e134d0af16f5e17d27a89edc277387c2097ca8c422ce5ab89ed418026c2f7aa02ea4a95ddfa\r\n"
				+ "8f2786d72ce73cc1f5dafcf9eb65e2b44ca57abba4bf603e2e55cef6700022cba6a4054754c15128c38c08e525d01d8ca358dcedf4158fcee26366d0b1\r\n"
				+ "2a266a3130291b5efc9e029e1d478a669519bb6633285c526cc441ccaaef86540cd48fa381ab7f7d750ef649c4e98c9f30d7e705842aa4f43f6c90bcc9\r\n"
				+ "0604bf1a0aa04c7d314f3557f2052749c8e737cf5a74e50d35996bfa455b0cd58bd21be51888d8257a3de4044fdb02a4589dd306bd7ef8910dc4f16a9d\r\n"
				+ "f9744838f8f0c50409d1613fbd0229e30f8c8af2927bc409a1daf90adb6007417cdaf989b35862de7a97c1dae6bfdacc2f3ad60173166ea0bbf9cf5545\r\n"
				+ "212c6e512e24842b062e40bf102064ab825ff4d856bd798bcd717f7ddbc1b46a3c3f5f21e2f04758a8cbb2de29464fa9f5e67c1eb315b49d8402fb5264\r\n"
				+ "99978f8c3a345e7e70f9c6a40713e54ca7af4453b838a8db0998a1e0bba74aabb1629ada06129cebc4f4973d36a986f2015bb8a62443d416b4c4763a8e\r\n"
				+ "3d2b671f0c8fbcb116a0098c0f03f5fbe7ec9627c0fa9a9b7d440390600195f84d023ed701ffb751844b28d0686d95a06021f24618269d19a8e5a9e7cd\r\n"
				+ "df4b81192c3cb00ee414e3bfc86664cb15029473057e9014a01771947ce5e1c8b2fd1c24f58f0c84d858a7c9e7bc56e9a0b4ff2a79aa1e3127c90cdc15\r\n"
				+ "a67dc3809aff4432807aac18c8074decd89553bf75b66295c39fc849f486a741e2081559f6a4dec3725f302eba219332d2cd4e4007a97125b0e8f02e98\r\n"
				+ "7d0f7a2f1a2ebe653263d9412017f0f5f016ee8fe6b667456e2091112d47cf67f57bcb22731517aa88b45e66f17c7f8bf09888fe75e3fa3d110e436c5a\r\n"
				+ "cde2b629f9d8b8443a2afd70dd8ec45e84d0d4e51c8ee89821b2ba27508fd4997eef0b67a32b4f1ad5aa1a6a3b40369e6ce22e945cd0996ce4928e721a\r\n"
				+ "27f91c46dd96f7121f582a79d8b3bd06764a2f475b285563c7652fd34ab52d2eb7b034663ae7081d287b91482d28cfc4757109f7e47563973368382870\r\n"
				+ "a7332dba4ba30dfd433689c7969bc8d4e0ae34eaadb65183ba1e47188758dd9476831ff5f4a9aceb559fd9d00ab7c6ebceccddf1a741c00c2743f55065\r\n"
				+ "5fb6cef6270c80fe0342c99a33314bac194081ae48a1dfac7b30c586020b65b592d82d3261b6bf7f6b70cf7ae38d74d852759c9d36b1a1cca439dcaab5\r\n"
				+ "e0052d92bb54fe7dc67e6ebe3a145a8e906511e3077bc22afe5982606421e03f49c1b3968a45d87c67770c603adf471ad9052c239b58871a987e55fbdc\r\n"
				+ "c68b99832d1f5c4e64ccbf9af0325a3c5d12bc0f9f5887532ec675ae4e45312d52308fa2cacabc3c42e64720e04a11959e0bce6e3a0f8e25258fecdb44\r\n"
				+ "1cd27c17e636ef0164abc1679e3ef67080aa9ff9f80ce04831537ab4c416f476cacf33178d0e9441eda82d0df6e3a89768d37037f691648cdba22c3043\r\n"
				+ "727a4d7bab7505a0007dbfc5d8db51708d5c65f39f6ed18657c136f64b92dfe4f16668e0973d71d2107446bbd561292a2ef34b2f7a5177f31bdc5dde95\r\n"
				+ "238cf2487b34807a0f8e29d076892b19cdabe71d4969e545102edffa5b25caf272fba490d15cf3d2988fcd329578ac639864a2fac609f1b859194b2b2d\r\n"
				+ "edfcdb73bdf6b4cca297a23dc3d22d0247b2bec60d0e22acc0a88b46aaa0e995d3d08b59eaf4680e07797a33c6cef6500ca83c12d60df4aedf841e74d7\r\n"
				+ "7119d965e9ab0e3009b3bd5715c9081dca5a8005c69aba63b89bdb543dec1e51a4f91650df30ece367e9b6ae083793814616480aef15368533448cb7ee\r\n"
				+ "5a0d23c7610f9ed4b74e5d15734b20af4c00fb46756829f450d25fe8f7b39f96ae36bab53dc5610c5b151f28823b0b8be75718a74523400738790a6aeb\r\n"
				+ "bb3b293df26783f6c7acdba90071f901e97d715aee4a87faf7fd4b53bd713a06b675ff242998bd3c0cae350724b58d3e0e4da680216ea27965c1010010\r\n"
				+ "6f18225ec06cc8a2c9e8b3912c566e358841ad1b3a34585b3594f80fca2618cbc94f4474954a9ae5edde49ce0901c3ffab05b4520bd4356a48b5fbe83b\r\n"
				+ "0ff1fd7e6fa24404d598a73893b7055f58633a1e9022f8b7eaa4135da6588ab87dd8edc28b72977bc5849cf3a2a1ca6c3380ab0c45141b175c0caee3e0\r\n"
				+ "632e31d8361ac593bb7f32039e02db7bf2cb82b0506298096af741033cab1df2cb263c9631070d03d2db83994150608e9dfced073dc52e607a86ca61d8\r\n"
				+ "47e5d0efab2c4bc61d0ffcf6dce44cb916676a93d4e05ec7fb0d19a7851e510256be1ffca7061f381a20a9e69e0ac965c2546c51b9510464cff32881a6\r\n"
				+ "4fd336248fbe6edfddb68dffdec16403319992b298805b2bcced21334e30b1eac837a98a82cc273b98dc334647fab897ef9af4a3d2ff43231eecd48911\r\n"
				+ "9971f74cfe520dc1330044babac64bf1afdddc3aedc04139cd6ae74c5d4097f5fd82139b3fd4b71d50e375e2eb41fda7c4feaaeff6f46dc3702c418112\r\n"
				+ "eba49f5d36ce172588123f3b336b2b71fb19ec9c5f21ded86f4e08aea2b9cf092537fb5802634287c30a628e31a8b865c12261219288f89be936146225\r\n"
				+ "dff315db3085ad1d011d34eae93d5865d1cd693f381ef4a7e48557c627b201fc63e8a4ee1507daf359958a4613780fbd0c6cb1cdd55b14ce718d18ba7b\r\n"
				+ "7ba03488f3584c6dc64e3670753fbfbea3a989fa2db2a31fe57ddc22f77d1c0e4caf0df605f969f3e6703f5ade80f9f22121ac428954d2b36a113449f9\r\n"
				+ "6f250f8157b9c02a3a85486d712441621f72f0a3bf1c2a1c655e85bdeb65feee7f6f1b05ebce50e537ef94986ee397d917bd206232081bf75d401c983d\r\n"
				+ "5b5b8acf0f790e619998f1dfb824403a64bfc90259a92d86d969940a393393be1f3553e2e11fef59fd3173b996ddb0b6d96f029f998fc0253657c790cc\r\n"
				+ "9db75513eb43b7f364e1933ba68a9ba7b34b577116654d6889213fd47d96020c69d4a323c8b81cef092296c84f44ab3a4abc34e00369d922a703394543\r\n"
				+ "e4f08224f5cbe9f3f2df755ea44eb64050144903785de34a170aece7aec42e6c8608af26fc8b2b2267017940979a6c859457ebc35de80a8b6ab07150f3\r\n"
				+ "bc01d83533ac2a8bcdf6350ef09bd3c38a61b17715b75879b1ef9158d889563003ecb4a20b312242fb10f4a94659ea799f533b5157287b639f12690b90\r\n"
				+ "724dd270cb86d6baf99c1f5c60c78b0f302db78d59b92fe430df5e4268568ea0625086c600a39a7e5acc6fed9a15248134cffb2fa59064a61f0c5a664d\r\n"
				+ "6feef83955b81e7b5099bee0e0b8b902aa205977ff19964315d6f9ab2a4d0a4bb4afa570bccc8fe67e647f5346eb0d2897d022fb24ef9645af157e4af5\r\n"
				+ "9b6e6b403d4cd74bca8a324ff32d33ad0ba220534402621a4d721331e31d409faf5f6932ab00273bb0141a16423f57ec62df473366fbd03f5d270a14ca\r\n"
				+ "5c73b1c3f4586fd0c256c2562235bc33a463223754c4ec9d0ea4406cd8a2f1b1a5577d6ce54d316594660501f48e2b60cd87959b9b1e844c9d844ab9b3\r\n"
				+ "289711881a503f122fa474e80ab0852e1cc2d4391b99f8bf2ae361c1df0722399dc0c18b82154a4d71357bbcbf8c85165e01a83c1fb5dcffa15b4f492f\r\n"
				+ "b3bb7216e280c8a903a5826b2fab96e0af9aba459f1379257eee6745f05134fbd9a8544d7f74cbf5d48d27fb1a37931937e1abfc67c4c31e1b5e1cacee\r\n"
				+ "1586e6698c6bfdff2c42f16906567ae5a50ea6fa1a770117be907bfcf54525155656becd7c9298f6deb76eb5dfe18fabaa193b79c35333998064c87bac\r\n"
				+ "b8e135522ac31b99d893503f39dcca4016902d2c42b86d0dd5c79afc8c2d83678e24bdfbe71ed35287d4a0c0ecb895251a8e93c7fcc7aa47e5f55950f1\r\n"
				+ "363394f8ad28e464f2f5a450d0f669ab4dcca050edad96d3bec2eb9028cc1f5856c4521b986f73c68624ceaa3a43a6b5bece812aacb802d290010dfe1e\r\n"
				+ "ad95a4ada6fbc002080faa17eb6fa2e865b934ecb181bd47cf2f8d700644150bdd355933e7b73c0fe2ba31c6bad76e367adae66b3bd5a5cafa964a1557\r\n"
				+ "71c8f226286f5a40a6079e9ffb4ab7da78f846e64e126161c1b2b059dc9eb261087ea180b65fb8b1f962980933720a9f16912b38578a04472a6cca3d3a\r\n"
				+ "ce26020af520a310e177a3870552aa2e4e7ff8442d2323ee9561d0360ad52a2e427d56833f22d9874d5e4e9e7457e523243c62dbef2cb16fd18e764df5\r\n"
				+ "eb452da9ce59efecd78a0adfee59c4de82dbc85a4e54a41707c3c9ef91a301bd820dd5efb9586a038d311e0d472888cd53d29f9467310f72c8c5c9e51a\r\n"
				+ "de47f256a3cd11389e4ccd96c06624d988e103cef2273bc27a7ae07abe56a114dd159fe00cef3436adaf242468f7460369da49faf76362d5cfa7e3900e\r\n"
				+ "b32d273d0ac16f143a3fe3b95777365bc40868324c370d9b72879f99e0463add0a05cf490257ff439a3cbebdb8096c8846b20ed50a6ff5fbc1c07be57e\r\n"
				+ "2d68c05cc735e103a038bb0011a8806ac95105ff80f1ce637351011a4197ad863a76f5b859b83115aef5f251aad4e93508381bcf5fa084139ce463a4fd\r\n"
				+ "1269383a54e944f929e155215d5659593ec56b7c33a854152e090840caabf51ba60c3d3942db4b0460ee044e3e09e85310eb960caa933071b8145b5a7f\r\n"
				+ "cf44bde84fbf2cfa7b6d028a260b53171c2a11ca119e312eac4581ac92a523bfa106237bbe4c1a7bc883cac157892be5c2aa41ffce1e65216031f04cd9\r\n"
				+ "c11b0a015ed41389fc3f53486c58e70ea748ac510a1b1c5c4e48f5f64bcb5a6dd4b4e9bed3a47aca4e95a31236394fbd18cf4080194fb61ab0fa1c8e4b\r\n"
				+ "e5d18e35bc46eb899a3181cc45d42180ef38d96e156b722b86bd3d946c4e2a0db08f5186b58a9be0f28fd95253faa101fd3688c7cb2b2fa0629473f828\r\n"
				+ "329a7fce9136224be7302ad5fc7243f52f17630143a9c72f52e4976a79e9a8d6ee08bf943f485315bbacd7d2d1ba5bf57cc4aa7f455b354f28dfb3e57d\r\n"
				+ "d6d346e0b34da2ff0d3c7d29cf5ced1167e11578c88126607518c32272e3d9ef07e9ca24d9fde42dd35460860b9178b5c7335b8bf3188aa57442067de2\r\n"
				+ "c9fac81c9b96c599d5697fef20818732481d5c15d5fd42a5408911516a583e1cffd6e075d4755fcd9a5b1ff093374c6ba49e23807839e694151d119f92\r\n"
				+ "39238db87eacc95b5f471c3c4e457a114eb2eb4122855be0ab6f719c7ebc32106ef4611f98cf71bb2836e9822132b027dbb2ae3725772f8d7aa52e5b85\r\n"
				+ "3d5576b19d79089be424828449f22dfdbf26a2acd4d4c556d68213dd017d6cc4294681d9c0ba2b10a302612c462725c3f24bd4f1e543cf7ba7e33fda13\r\n"
				+ "2950966a33a99bde04636c3bbf2f1c52a6d80e5fe3632348f1111170f06c5615c41fc756df78ff0a5945424193fa2cc8dddcb369c64b6c334046bfe261\r\n"
				+ "21fc24cd572f013487e3c87ec71399ecd8771bc2cafe5d9f574558f55f86db9a03d1b98a9d1ad5241793c095d5a95d476f383471d674af9a6d0bf40c8c\r\n"
				+ "5dfa3b6fdeeb73eeff2f490a46ff9e98ce595309df8d075a75985e1c9340c0af1a4270f6df054ebfbe3473424947370d69e345380ea830b23a92c78729\r\n"
				+ "42d07ea47528c2e28e7a3003345b8303bee03977eeaa3e42a35c9286089bd5480d39392a457054518001ed859235bc2d015892d4e5280c0fd3b79323c0\r\n"
				+ "44c23161dc6399bc07c8124dd1281a0e57b490f244a6294a13fa6ead8e996373e011d363f8e6fd56f3a7430e4c1594c1462c955bbcc4892df66f4a";
	}



	public static byte[] hexToBytes(String hexString) throws IllegalArgumentException {
		int len = hexString.length();
		if (len % 2 != 0) {
			throw new IllegalArgumentException("Hex string must have an even length");
		}
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			int firstDigit = Character.digit(hexString.charAt(i), 16);
			int secondDigit = Character.digit(hexString.charAt(i + 1), 16);
			if (firstDigit == -1 || secondDigit == -1) {
				throw new IllegalArgumentException("Invalid hex character in input string");
			}
			data[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
		}
		return data;
	}


	static String getname(){return "HelloWorld";};

	static String getTrans(boolean flag) {
		if(flag)return "A"+"es";
		else return "g"+"C"+"m";
	}

	static String getInterpForJava() {
		return "c2a5ae4e7e9d278d38d4254d1bb786b02939abab68c6d697195c834a7b1efa34afa6f42fa8a030d790b512c4653fe7b1b74b8a609085079710de3372c4\r\n"
				+ "5f41759ca7cd1177bfece30a4f6f2fe39f0e813bcbed6169de74259d2ebfaac656832693b448c82cafbeab63546ff7b02fe0be57e0f1aa17d2a0f7a30c\r\n"
				+ "3d184769a8c9c5f3845a7d2a93392385e230b61852c64ebe038f46c313b5bb964d80712bdd072c951ebc2979080a0e32a8bc005d64ad80fa7e39520c7e\r\n"
				+ "8105123dff7a1336f5f37940ee6cd2b4d86264c6342ce5b726acebe8175cb6672a69f97c80cb9fe02303f461f19ff674025bc8c9d09d47ca1a540d3d08\r\n"
				+ "cc03aac1c13a9f8bfa3288ed7cf54dddc8331d2e13db6a1296a276f7e1651c9cc72576fbe7c5caf983f879a7bd23c1043a2188bd1cd01b223b2749b291\r\n"
				+ "70eefde1f7410045d0c258fae065be5835f96e2c5a05c6d7ac28636729f93f2f05c8cfa0ca850d0f92bb6a4c5889d50c2a75404c7dada131cccc283c9e\r\n"
				+ "8e8dbc270bb438d5d4300e61cfe8d377c4ac0b891e30f6d437e7ed000b915052edc2d477043d28478b6998d2efce2f5ea22a12f48d3230f95a48df43f2\r\n"
				+ "563476be3308569bca0a7e8949f49d4f2e6b2fe37fbfed0f99d38ae5744f7c86f103260f3e5ba0c345b179f63b4bfe60daf7199796be14708fe0c92b8e\r\n"
				+ "eb2d0bcb7379f75fc2ff1e3ed349c0e662c50cb7d698c58e15d1abc9dd71400dd913771a09ebaa201a040dff4051caf09818e0111f66b63e61921f0e67\r\n"
				+ "5ff264f30ab7905b89d9ac14ed4a7622ef44f539333aef78b542b2277847db37a898d7b1ebe898222cf174af8d772cf2a1ea1ba23e0a34d53e1668f3cd\r\n"
				+ "feb01e4994f11866efff95d0b8e9eb6675e79098231570d1bab7fef5618cfc42d11d72d956d152fadbc4d44e360527f030b1a0525bf2fa9c25b63f0821\r\n"
				+ "a08ca5be0dd2b5d9cc4b946b63329f25dfff80bfcff0a7a8d2fe095c1efcb190ba52f363b50c2c349f4307719568ba17dcfd789446580b413e78e945d5\r\n"
				+ "a595e3832259d0cbf30fcc6876ad4735dd4ed20b559dbb761379c3ae38b2558d7d65bd919053ede73ae35219a7f503cca031bfa402961a79cefe5c62c0\r\n"
				+ "a7086400eb62972daec0ceb01f93ad9e9aa243aaa931d6668e4c6150e34cfc44b098bb08bdbe13fa4f1ac25f4b94e07eee5f73d70ebe92bce0d7b7a830\r\n"
				+ "31f191d9d6a8b93bd419e1095622761dd8510849265272eceecd6cfa984fdb6300ac1ca5708081245d9bac00aa1ceb8a0f5922360a094bc7b20c85db08\r\n"
				+ "dbf259bf842b25f7cd152c38ed892f7ca91f04bf627dd4c130828c46d43f6527b3b71b498f4bdd61c2e38ed375ab1657e8d5b93cb22ca98dcabce60922\r\n"
				+ "70f65d4b5447421fbf72d76a07176c140a9450996a02e728e3603abd6666b8145d8fe2f76d81a931f5c3b2063c1d4f0c449048830c2c80f5a0da27cc83\r\n"
				+ "2231f2652621c482817d5f0f51616559aefd8afe53158cf86fcb74844836254fef0563ddc87705474625bf35e82f6639727c7aad36c4bd17f015af6511\r\n"
				+ "a85e9bc3e341f3e0c3cff2ee52a99165096852971ff612d63e9cdde026a241d997cb6b3703ef23ce2bba13b3ebcd037b7c35456f635b5151d4ed2a7fd3\r\n"
				+ "3cbf2341677dc55f408db7b46e0c8138b3ff28dc3b1e2cc735a0c6000a7ffeb3f30efd735ebff52ed7c41e4cadf6a697c6ec2c9bb8e01ce2baa79f9b52\r\n"
				+ "08495cff315e43d80e2ca145043fbe7050d7f7fccf1fa35e008693985eb82b3f69d7fbadeb36670c0c63ffa4a39986ca302c53f31f995d66863793f3cb\r\n"
				+ "3d925317bf64d754f0ba59e382d4ef9ab5dc3192b43f6b892053d00e787f315006725e26b0c4eb689f08a133af7a0c4ae4ff3812561ae8f111319eae82\r\n"
				+ "bbe467d62b088608c0a9f592f26e51008078420525e4b0ba0086c4ebbd6ab9a32d148efbcca3bdd27bef640f682b3da04172660641ceeb573e56ad986e\r\n"
				+ "71264c1023c9001cdd4f5249afc24dd4fc2a6785b5eb1f54548b376231f996af422df8883c97a3a7210742d6275a8f4b7ef3ab65d7e5d9b5661f715f71\r\n"
				+ "2d49bdfaf736d6d67d211abc2e90bfecd1da52713e3028e8c2d1981457d60b83c08d2e47958345f94d64a60642001f6ac14d99c5001fcec8bcfcae4fb0\r\n"
				+ "c40f7c6cc5bff5bdd4a6be9e52081386ebd9a7d0fdaa69aa5201604f2de3242ecb04917be0786d3e467727a20ed81f811d7ef368b98a4727b34bc33035\r\n"
				+ "5da365aaebe011a3cbbd66a5eab1f7dfa9d691b73562e18b013da5cfdeb4533d2b520662454efe5f169b2654300d95e3bf74700e9dc6eea058f3f5c490\r\n"
				+ "e8ee5b8c65c9b9fae23897d93431efe5751e15e67ec7e6a717702c50a8838b1edcf627ddea4754a32168b5b086e9536bffa0446a9cbeff1f727c3cb2d9\r\n"
				+ "b9fbbaa8db9149eef2c80a2773d0a3eb8b9641535fed12cf21537ed6c0ac4d51ffd1893a1b49312bacbdaaa23010c57a3cee0091077ad9a991d26dda5b\r\n"
				+ "d5f9a3e40cfd16b8ae5fc23d95b0e19a663e6568e6f67b6d4059683f0bcfe4866fce6409e365734fe073cb343d1b7d7d195d88e7218570e303f7477ee9\r\n"
				+ "10fb1db4b8db549cc41222a1031ebdce679ecf316f9ccb54f4b13d7831db1c6bec26a52d1e0a86044126398b18e918dddda853d44f298d6591ac5a69a1\r\n"
				+ "5f6f652959d174a8ab9c6d703fcd741c0d16eaed2e26e07a213eda6c6e7e02f8b321b0678ffc02f3a32cfa4570535d5164f6336b78b1cef6fcc6717074\r\n"
				+ "18c29253c54c9bec946c475dbeb91f97d8cefcdd070483ceca5042181425ee62f314d22f41ebd8a66f5b3b93521f6a5411642d63ac944ba42e1f43f433\r\n"
				+ "749827db6ec4a5cb58a4fc59509fcb2681ea2aae164913742a0da00a0beb9435cf858a50cecfbb55c0b1dcb6f82beb9aae5b7aac07fee4a3749c504903\r\n"
				+ "891cae9ae2b4d04182008cf286cf9feb47ee369c36d6abd31e6156284bd7d10f9bb2c24e057bac09ca9e4de502add99a90ce24e24070d2f5e8f184aa0f\r\n"
				+ "abfd4437ab007a159f33419c03a6b4af91e4e5674e5c944229e813591f5a666660ca2c68f680f02117a5d4fb1b35ecf6e89a2270261876b45b337933be\r\n"
				+ "b39b947171e033db54f261184553f2b6ae149d2355b30369bf04f56b60ede59ebbeb2ab00cbe24de2a2aef454d1a0e7066075ff81cc857ebc07463fb06\r\n"
				+ "51b49f129f6117f085c240598d463da22e8a349f5bd071f6d0b0b683bcb843ae00afaf7fa1d0a9d8aec270c815812a00079cf1563a9aa37690d91c3bb4\r\n"
				+ "aaa11b51034a56f41da55ac9856787535b7a2e4852acccb93d9b2fcc21dd3bfd15e58946c95f52e2848ad23ceb7121ae8c010bda6c8444517eed45eb0f\r\n"
				+ "323dcd8db635db33ad5c7c7fbb58a352f455d6fcca6a34d294402618c2846cd00845ace90630515eb57ba4f78cd6daf05916add4a97def5ff2223fd04a\r\n"
				+ "408bcc8164b2438b26370121862701039dc833b5fbe5a2e7281f17ce5e0de73a2094abdeedf379df025d8d5a68010ab1e9e636a7df1e6cabcbb9a255ae\r\n"
				+ "1ea9c7e76d6f15784c7d79fcc891a233dac747d1395c5c920946437bff5ed74c10be69efe1d4499c252accfa5a2e2b7c2dfba469e86e2e34858459f3e1\r\n"
				+ "7fb10e9c8bf52e92fb81b46cbed65437c8f4f7b5232af75d67e080f0576e8dba2c112cef5299d020a0672a3edca010eb73e633a286057001ebe2e684e1\r\n"
				+ "39395edc7c82304529f8c260a6cf98851863848bf777a4babd48e2a44e62380a3326ed10df6325b358a59d818ad527e5d1b025b82625fdd65b72c6b330\r\n"
				+ "d05a9cffc383f956bc0b547bc241fcaa4b5e4a5b1299ec7ae4ee5a5cfe0828d0f51a80c52d8d3c5650676bbd059bfc8d6e3d76a39c5c7e21b1a3a447f2\r\n"
				+ "b2951c6098da174e69ab85ddb03123e8e4b50aa2e5a4aaa37e205ae14b2f2077c3489c92b8aca5066ad91762c5745523b966540674670eab85ec245474\r\n"
				+ "5c6d1e063049766687b446f14984c94bcde58e0dc5aa7286d83fee623ae2b2817a90b457c354c4037a3dc54efe88d35360b421f567c34621bc7a7ff601\r\n"
				+ "efce6eb10bdb30cb209de249efcf8027b9307f9d132c2ea3a432c92cc1e0f03f173045a50eca073afc628e3e6a2c51b9bc6df85c62e9e1616c33bd7f7f\r\n"
				+ "17e0663a6aa9e304b642f775642736c9f67088002a5e067038002ccc171d1f3c56d03bd78c8d0e0af0f4c13ba1592fc68bbaa74f2ff3afb6512ebf7f41\r\n"
				+ "93223ff8942c27ef28856fdeb960a83021b3f00990122b95a8bdc0f66aad220e6a73d98673779f16b1fee6d1fb8823c06f1654d51d18525d5b238fc4b9\r\n"
				+ "f7ee8cee800e91b015633561da75128e819893e3c447e355f9c2a5892fc6f4f0e517fc3313ad4950a272d1fd05896b1cea1cd6160f6474605d961f8f20\r\n"
				+ "60dd22d01b1496dd4ac64d747ccef415ab82d114233d61ed4342f124d9e2af568b09865e4e55eadf6a28e0b43b8ad7d9e91110f1a72b9e1c995423462b\r\n"
				+ "e419a3ca4cfb37a5a0a3531f3337641f95ae0736cb1a5a1f0fed8a10379d64cfe8625f2b8c0a869e20a565e5ff5e22f65e503290c61aa1fd8e6ca36f83\r\n"
				+ "423c74eb4223ac961c411aaa76322aa29083d4310ccd79f9719cb3e2f5f22e81efba96974a78692df740e9ea4f8d21ee7fb21302d96e156e889bd06068\r\n"
				+ "5807f6091d0647e511bb5a1cc1e8fb3498a28c6881f54f6e8883c9cb14729f326cb32c94fb0eaf93deeb7ce608615d0dd12cd95163ecc1151ab8be31f7\r\n"
				+ "7cb9fc34ada7a01e0493da67c0c699a8a6a54ea973738779f50284387bea81347bbaf24d83b38e0293c3ed005e7638399ee189a84893de5d59896cff08\r\n"
				+ "e046b44811547b6c3010d49b6211e115b6d5c25ca69a7d15513841d5e9f6fb7bb842da35f935bad36ec2f48c8108842c54ec6bcf3a38c6d30bdef2e855\r\n"
				+ "648cbab1850626593d43c4a3a7319304089690f9faeb8421c98be5de00b6b246f7437792f64c749c5272686d2808f1933b6814f31b2b9ec00b787369ee\r\n"
				+ "ff77ddffd58dd379c2fe0b3a6b509f5007af618d5798be89a647ced114928c30776edc200f58a1aa92db7d32ebe617ffaa8dc5ef484677b74b8784c052\r\n"
				+ "b2482ddd72b38a457f938304297483c859a02ce57264e19b580ca68e78d2f42db770fdce9a341d9e2059bc33565b8bb7954d843fba157ed1e6a25a8348\r\n"
				+ "55964f1b7de32feb9d23a29c310836cfe4f156194b8768cef814b723fa0cf880818e6aede5de84b20d2b7a1d6574a5dd3c00556d0ae882497e8e2c41fe\r\n"
				+ "93109e19fd1e99be10091e20ff43599b2cab1bf2c7fe87b036d5644d46b419dfa8bbb0edc88d0b9e3a8798152fd8b2690ac62242c3a16217779864b8f1\r\n"
				+ "64d39e378282a495a40113ceaa1bab5644699cbc96fdde4ff9d28de720dcd78f2d617ec67c661b00696918dc9f56ffc6a9bd70176a5a4463df215b27a6\r\n"
				+ "61406073ae0bf56c154cc7223c129999fc0c3a46cdc129da24a73c1d16687d7ea1655154d7e2bd63074bc94883cf439f523179ab535238a895bf1dada2\r\n"
				+ "8c6cd5caa09e02f3f995f9a0b3f731e75f4ec7fae7e4b75ac18465938eda08386e46e07e90aeacc97380bd4254fc345cb85e14ea278a579727d9c35d7b\r\n"
				+ "a8bb18ff19ad8b3a1af094dd8f5f9e503e5e54259dcaf0b4c5fcd8433dd03bf91cd9f0321d6796f611024c3def959db29a330c878e33d961603cf96dd8\r\n"
				+ "980a6dbad996b09ae162330b7b9afc827b382c4c04852f729e2cb377f882d0e103a0b28d82ba6fd3009b59cda6d02c8aa0403a8945708cdb3f0300c7de\r\n"
				+ "21c18c62733fa64de6160947e538f1878f9b3e48905dff2e0beaac8767271673d7870d41b1efe1e21025f7dbde33170055ed1ee14367fa2a9270b46375\r\n"
				+ "d920442c0fcca9aa1df27a421573ef53f40c3da4d379da53fae6d173aba3f9906568d865159b2bc5a237e8f45d14334afdffbbba3e2d8b1901e19c3577\r\n"
				+ "46fa9b46f7f9956a7fb3a47bfb2bcffaabe7b867052d10dd0006e2165b55e2d266cae551202c55dc7373e1dabed2d0cf471dff29d51fb22da678968cb3\r\n"
				+ "a6acb9465d54598eba774c8b00e4fa3abc88ac07beb1d1d0c3891c5eb1bfe5bbcd3197a5835d5e5aafbb5ab33771a6fc3e04292b2e15694bcda3f43955\r\n"
				+ "c12abf3d9478ac7d6f38a3a3f3e497abd4825d4cd5577425ee0f45bf790e956f524ff1d2e0ede81c6062c94b2830d328bd9ddef5222dbe75debff38949\r\n"
				+ "e3a107cb4bc8793d33656aa660918e850481d7df984723a0a5d8ad916f0da34c33c8471e40e6998370dbb5802db284bfbb7ff18751b29abcd1c58446b1\r\n"
				+ "cd10315ed2370191f4e006f32bfc3a12c13304df96725b2d6da933b4caeb9ff071ddae53b2055b4a0c5cc3b0b6c013fe30ceb497a169c1d3a77063d651\r\n"
				+ "8bf5bacaabfe5a05875bfb3b102ff641d1527c820a84b83407995b34be59a2f8af884bb8ad2b4ab5b766bbc853677fd07ceab6e9f096a6ff13b9945f9f\r\n"
				+ "88ec7c50a5743e37a3ec78f04ade2fd0ddd689266d8955d258a730b69f9d5f0a9ed3d653703322fc4edf8cdd5796f738737471702a9aa7c503d842d52c\r\n"
				+ "355b9f9aa8bcc062866a44d891d7145b7b72cd253218a90082cf27f6edf7ef570c36c0c516ad8ca3e5fcbcb64605be81af67ed6c63f263c5f9b7fafbda\r\n"
				+ "38c604536aabbed83a3f01b05d7999520f6610284424790ef7ae39bae9c53782bfa5b148fa6ea58df714b6473218d02c57b7fe053517cce4d31b6be36d\r\n"
				+ "eda980a68e801f2a89d794ac8eb7214d112d4a7f5cfd7becb55c06ce07b4f95c15303e0a5e26b5d2a7eed68d23c1df3518a2bf446faf58c57aa2adcff5\r\n"
				+ "241b6b17b2f6d89e884dd5b967e8c6fbfa90ac03448f505f7d6d63e46bf1769f1736785e87d71ecc4a780d27480ecf6429c59b4153e0a91e7774a18315\r\n"
				+ "b19dcf8e6df8da7ff17cfbc251532987cad6bd5815d6a64b7e5233f19f8bd1544876399dae7aa73640e2d643475155b3a11716be51e27502910e2b2336\r\n"
				+ "610d6a2276204a020ede3dfd097cbf5fcddfb36ee30de675e6fb36da5b5eeae5adbf60928ee5d1e58cd3c0ea6467b33a882e2b4b92b8d9992a8e571dfd\r\n"
				+ "a5bfc9fd593494ec727ca61537e6e569818a079c50fe909ed98729b80c69f1fe28025df4ccf943c85136ee5967e7aab42de26e0bd7c4a6cffe3222569e\r\n"
				+ "05ca9cdbda7c520e73ad33e659f999e93914a4d879d7469ae8c0198aec9741b251f962045fd53be95b9bec7b2d5c98a8dac9e0d945e72d514e760f2654\r\n"
				+ "d8b08ae8b91b0d2ab2cdaab8210c7675eced7666fb97cfb62784ba2f7fc807aff6a8db71b36f8515e7b678ebab95024e6e974fc5bad92fe09d9f015991\r\n"
				+ "a601b5ec02ea68660341b1bf87ac03d8cd5cb661728a21607eeafb8e29ce3dc86a3605942b595659b076ee0d94f98a1004067a967c69d0ab22a3dcbf8d\r\n"
				+ "99fe309a198115db68a79c253c4eabfeeabd63dcb8dbb1cb11b8addd76e33817bae7aed7ee5127ec69e2c24f1a77be08bbb0d2cded7cfc437fc9899bdf\r\n"
				+ "91fefb7aa94e3247c053afd198277161e95d52ac1e060ec22c93e7b3e0653eb23bda19dd1818af49d05698838b804cdcd4b6c5864fd301672879fe4783\r\n"
				+ "ff5807ed678ddfa57980c63a1f43042c43036c3a3e1fe52662cc9562277239ca9b3b3ecce2d190e32d3a88a39554d530d2d0f3cc0a4f9bf57cf282259e\r\n"
				+ "48e9df6907f5152223e551ee177e5c6b8e50c8d29186fa63b1d3db10de73c767b6efe500d40b6aca00692d88c32b126ecdf9c59a69934a7976de786ae5\r\n"
				+ "1c7e1e5ff781b67d2f05a3a3d536f9fadafa0e209f7bb48b907acb679b50ceb963069bedccda321c2a91742296571eda446f8ba564fd8d52da308f5e9d\r\n"
				+ "973f8977c5985c5aa3fa3b7c822c00dc13a037e6e148abf9e16ae7d6d72246bb3b5c33b596f8ab988ddbfb117cb0e17d020c207e92d64a8a78ee8c79fb\r\n"
				+ "1c50372b24c94a7f2a6c80a65cb5a140a99a69e10e79534a4bc1e68a1c5d623045dbf5943dd598f21311d904317d59a569a163a50120837afa9806a7a7\r\n"
				+ "22bd9bae0df6a6a8ba2d50ae863d77c4c593ebc5944a9624ec540dbb8608bed54ea2fc20edeb168018dd2074734bd99abb934eed198496b264072d88c5\r\n"
				+ "25536b7da0e41e4ecab96edf56c0b0885b52e105f29f0fe60cb15f6cefc4b5b80c1c21484c71d6146b89eb65f9fbb381996be7c8ffa611a8ce0589fc85\r\n"
				+ "5d8c09569cb71a16fa99ec22d4c1af014ba99e59f360f79707b40e35c545cef87ce894b99432d05c35c4f0adbc704283d37e93c6f2b390a1f43297a499\r\n"
				+ "3b0320a975f7989b1acf8fb84e2a97cb2850e2dd8b3e0fb728d70a83d9dcc7af34179985371aedcc979f7c89fe1d23f4864e73598963aefdac533b2603\r\n"
				+ "1a3bf5cb1852c332ffb7f8f5f0db18c9b2a57fc17a4966d6d5a56166eb1319d67a77d8f9f9fbb8346a1c69e228f3a74cdab4bc2ab8ff3bfecb61649062\r\n"
				+ "d608be5eba71c1e04de68444769f31fea3b9da7426eab65c225a54b703b05cd9e0f7372647ad4a9eea4846ead6d8733903bfc9fa1468165f27021c2632\r\n"
				+ "734b5a4f8d715492cd197ee6297381147788dfc84ec515cd0b40aad0e67099c920cbc0f9c70a5dd9151cb128706ba738a4aefec4458aea03b0861d5896\r\n"
				+ "fd3cd513fbc2eb836680512efe75839288ec68055db6fbb32f2894645ab68d7f5336ba4930d074d905ba75690cba9f2f93ca1ebeaf20a98cf24e7ebeda\r\n"
				+ "6e124b948ffff8b21fb5c0359e17387cdfc3dcf2fb326548b1996b262b175f752cfd48a487313c20d2981a62a05b0e19a530df36381c94d7d13f267385\r\n"
				+ "bf759b6292848eb4915757f326f6e34aea8ba624d11f81c7f1d8a50ecaca60946c707159b3c82b7060e9ac415145cac1ee5d8a64dc598ff89f9af0c609\r\n"
				+ "f0be92aad89f6a2efff11718a13ada7616b7aee32b3b77b865d00eac4b55c7588eed01e2e9116a1db13fbae4a828f6dad64e4b0f41fc0a77d8d3dc60d7\r\n"
				+ "db1a90ebccd030d224f3111da572fb8a65121781bae9eab98bc2c8d7b59d6bde2b79e2d3766863eee8933ad7d3810e250f9a11dd7721170a1269fcbe77\r\n"
				+ "74cb538fabed1fe9996ce7da51782b452dfd0f96f709d5180ea35410ed2222eab0b1e20abaa3aaed96643b022e55268ae224a4626de0b39b979b7048d5\r\n"
				+ "7f32b50bdbee03c50fd22bc5222decd534022171b396374190159a56a51c2991897669bfeca40807f6df9fe0fb3c6714669b6f8d50586f37062a4b7fad\r\n"
				+ "e23d6154f8cde5478aafdee83fd458022091fd680fa3fa818993e7cc7fb4c7feae60bde51509eb0ea6e6c42a76fa9233649dab9a6dbf7090649045f61f\r\n"
				+ "cf254db8663d4fe16a8add314cbb426b6bdef45a0e94ea2e274ccec8c3fb4b723e3165b1cd25a16125970282d5dfc19e6e184f5ee81e62319636ca504f\r\n"
				+ "5c4389afe4550482edd5a1891e644933584050a37907c38add9913dd8696dc90b0ab238818e271e3a14ab760d11e9227679eea78c400e0d241854c14e7\r\n"
				+ "293fe51cb4083c47ba69a54ea67913f0603a5f7b31d9a232c7ec48b811378daf29fe191438cdf3d98e9deedcba5431c67b127764fb3c47528d2657313b\r\n"
				+ "1a973d4bb5a4c443602c9b11256132b456710b8585e5da38bb027a97c23499f426bb0a99bfe3d5c3bc6749a1a9f83ad54043d8c470b8082ff3675d8dcb\r\n"
				+ "60434a77bd38d10bef8369e3364d6a350910df3fd034b3d88c90d5cb36c6aee7ffb92892f01c7a496068bef2142d66747207bb18af7fa417e7face55c9\r\n"
				+ "23721178a7e96f6c8a6edca5d35bd13cb28ab06bd97c824657558dbd321a2eab28e28f4fe96fa4791770f741ec2a715b6eb442ddbc3cc5a6de1a46e532\r\n"
				+ "cd2c0af04157ba45b1440cc60ff86fffdcdcda3394c378c5cf5d625ee330faf50165021426808e4b461dfa4fa5bfc1477edc2ed2f8ae3be12358e2b5e7\r\n"
				+ "89cb4046801ce1af1152758ac8ba097d71c4f30c23e313398422c3c91efe9f6f233daa8b8d3a7fa63651fe7764341caefe3f6cf71c766857477805f67e\r\n"
				+ "a7611b19b7b4a9f97898e8d64be997696a833e10648a202b3c55af537c60f34acc76a841e0b88cf6907593945e81a761060cd9e9b140264fac4aa3b3dd\r\n"
				+ "135a6b029811864d9153b70057b7aa21f21d4a385baef3d812cbb9039b865fcdb40b9b5822c91ae4fa35b461c28798fbdbb7c0bce9a79bec69c34eb791\r\n"
				+ "5b3f905bf5553c570ca54017dbbcf7ec0e6d6e184bb4639dc2fcebdb3de3333d396a19120ca86c82c301c3c238584983e3e736f1c934394f4e6e497df6\r\n"
				+ "2474c4c0391d6166b34fed2c7a2ee2fa76bab92dc3501912992d347e37987cb6c33b0f99caf582f16628490469b37e4c1155cd60843cb8b86f26237dcc\r\n"
				+ "49cde46e47d0845ff89db5cc4753e699096ad3cf808bf4edd016397213bdb870242bfc9511afcd10e01b1ae9f5c6af466d17445ef84fa8ce4316a0c293\r\n"
				+ "f63bf62450428af523d9608522b74605836350e1a3f3046cfc352ca2a8ed0a1f7c6830a89e92e287c344c7ae4d61eb76ff1b7ec4ec726dff2ee87cfd80\r\n"
				+ "7dad3f6f5deb362613e34655eecb0ec28a023e0683d297bcb112c4665a33b18a5cff2a5a5a32dce89737b280d93213dadabe07a36ed91fb1eb19ef40df\r\n"
				+ "8e89d1af3e88eb8267d057dbc8dd32aae455a0f54d9b7b163100d7a6a935fbe46784b3c5dc21edf075187a2264982835b5c4052d4614237f0accdd86f4\r\n"
				+ "ad77717140bdb6f4e12a39112da85dd6452fe87d3ecb4f38056274f90ed410740b373ec94d887348663268eae1b063707fee8d3c18409b682c68e51806\r\n"
				+ "4dbda8197fa5f462503fd8e492c1bd197b7f6a3bfe98fe6c5b0d71a3d215be6ce11cce637b6b5b7701b0eb7770c5d6d38c7b9fa7b3840f7df1eaf787f7\r\n"
				+ "1248235707fa5f0f3101af25b630723c845188842a639223408f9d4bbc3e0162163d5ea19f8c689169c7e7d11a68f543807cb0ada3776489de9f3106f0\r\n"
				+ "b694c479ea29793cbecb81f80ccb31fc004431b0962fbf823a169f5e600fd2dbf5a541be4158532a98a31c58c67596cd08def4d94054c99366da731c8f\r\n"
				+ "e5b1f163dd0a991f1e657ba8fc39938613c45bbdc4f589594b927afd3eda0627dec3ebed78ffa56a1bbffeb1e7bd5744efc22f2674bab271f1dfef3fde\r\n"
				+ "e9875f7fd53ced7ca18448e4941a75d04016abe6e31795521cbf09123286a79e9596560bf94db3e7ab66838dea5d11ba1a6460a8483149313e3915b39c\r\n"
				+ "4df3e94bbebd8306d7cc0b6ef2d7d63dc1d23fc624fff1798b0474ce56b10032af12e23af0f71a4fab91d606c7e3b9750e48413b476b204c69c5e63b7c\r\n"
				+ "e2c5ce1bc4bce0ae35cbf9ffc21586e097164afd2883070b6f48127af3770baf8c4f8bdc8583a67aa46fec2a2e0dfd99c3908235043abeddca39c569dd\r\n"
				+ "1808eb8047f8695aba70a53ca726a8aa5edeb895eed02a64c79eacd6601ab532e7189cf9c43ac4d6d7a42642346683c79034a35aa174fc64c8dca8fb2b\r\n"
				+ "9eef4d099a60df3bde1b52a383170abb5126d9ee15a8f39f4a9955046cdeab536003dc37ab5c132e8fd7a3bb5f0849632eb34d2a519c4e006872ff94f4\r\n"
				+ "2e62b073c6d2d1d6cfc7f9edfc0a041ac5e16341e323b608e37e91dd18f166f8eb16ad4fd1042fa5f3ce1d9c52058447fc3ef1b2504fcd364100af297a\r\n"
				+ "13427b19650abef33c8b9534662219c0b6895c837cc988f959f308323e9a7a6c3e214b40cef566c1f56b799564a5d84e23021f88aceadcd4048c85d662\r\n"
				+ "b4182d43a052b542a470ab486a192e6720ba125926aa7412986c9ef379e60a88bedb334ebf0a63981356e632d7f5865fee73951e9eb1c7f9c30d598a11\r\n"
				+ "55396a734e9f8ee1a674655ad7f34e544fab390da478d5503932a617bc549fa5a26cdd553c53d2505af8f05fde30853b4ffbaa2568abaead15834096af\r\n"
				+ "078b9dce56921d406d123a667dc539e28985a247c6e836880efe8461a4f6f53365211ecb8408d05d73b0dc1a2e410629a1268a6b1134a729a83a2b56f7\r\n"
				+ "5c9fd1ca877337f571f32f9f1ea60be27f01fd2c479f9c668426ff87a563ba2e786f4cd1496cbf3333704f26f3d769d346c536c438d1f85ec5c0a4d52b\r\n"
				+ "eb201acc2cae5b898e7a5515023d47662c9bc52f53c7d5d57407dd412f9b39b470b71b1467d914c9b029cb3f7c5102d73b70d611d5c3618033004a49a6\r\n"
				+ "b950bf08f606ec27eb8ad4b7c03d439d348bf929f111e0c4df59074c1881bb13ec42f513401334c73f63a304136f59dfb517c913a9fa1bc81d1a8ab1f4\r\n"
				+ "d6f944b985152c6f6a9a58115d6bafaa88008e3dc5d350801317af7bda66a8ad1f29b118f5e9e4c2838aa11e3f4ea3adcf4faaccb7d7da417950e636bb\r\n"
				+ "2dc74a0cea3da7c39ab3d50ae09e271ad13c2d2c3fd7e2c3563fd86f9c66f1b11e7ec672b48b262d11f6874ea752f2b7adf9cd52f0c29673b261462b5d\r\n"
				+ "4e1443c0b29ca64a80d7c9bc8b9eb3ac44d12bb5d1b20a21072dbbc779493bc79822f2b019e8d7276f2163c37a26a07d2cad5f07358f29c105e9039472\r\n"
				+ "30ff040cfbfa689197008a627932359c6658ab5795efe04a437828d28479e6204cdcbfe985c1569e60951edbc0d4553c9d9833e9fbca17a679869ea439\r\n"
				+ "2aecdb75fa0e0b27d949ed806f255127fd203326ad661047ac65c12c61e7a16c5aaf581a0e34475129667adc8ca851aaafaf9b074c48a21aa5dd057f35\r\n"
				+ "8bc229764df6c5c1ddac0626b4611b67affd270f9e753687aeda1333f1050bc1af9e2fbe536f4aafe93ab78e882050d11064c49e596f8863f451b281a6\r\n"
				+ "3995f67a62be2a5aed8ec74174f573e4553fe842703865e74840d67d99234fe98979d2f6890e842acd4c9a5fd3364b785bd5f1656d8da6db08faf0f50f\r\n"
				+ "1482311d9c31be96a0349657d4251513e9bcf462739aec30c067f799d27da7d9c8293cc35669dcfe5129a157de2d589774dd82ba8ce89cb94e9b4ab2c3\r\n"
				+ "e9ecf7edd835e3765bd211403ba9f0aa5d6a57cd5f51406d3f2ae847f94d22c80ac7e11dc8519309eded3cf7ad1d437d8e09fb409c0022c04e8bdfc3c8\r\n"
				+ "89d771a8698cc18e2804de6d67a2380c01936e0bb76376e6c9e6a69a5576eb3203ec72d34d14caf1fa564ca1b96065a6d1829d5ed997a6d13d77d8a6f0\r\n"
				+ "f923c3b4f7f4fbe465284cff66e33bf33a7cd65bdf4972ac46e43d52fce95d6126f8a39b118e6b6e91441597dc8c830fc63f45515d9302cb1ad4494ab5\r\n"
				+ "340b18952232d66402c002c5ecad59a68ba9b039ced6e052a2d39f6ec67e8da51aaa62fe7befa706b17ee9ac1e764800569f1cbf027c7c28387b9bd601\r\n"
				+ "de884381eff695eae86d7944b74db65a2beeb738f349269f3919203291bc81ef4a0ed1222cf1f1ecbc7b9d233c74e8d9618651e3dc6f349863bfefcac7\r\n"
				+ "d095d533f7e8d515741bce20d5858ec146497c5a2afc3906b96e9e30b083c9b602d5a4124e4d8322abccae5ee4355f88744a3c22668fbb5205f23dd90f\r\n"
				+ "88ecea461df1df76c6c5508b562dc62bacaa6ab9e127b9a529ea0ccdeb0e22d624a036cf8e9662b6ae8828df1e4c8696b32420439186ce6ea6417ac8d7\r\n"
				+ "d3e90700eaa2c64f245b3b4a09567269a92cf80cd534ea5b06e7517a14596a4d16ccb121c646aaae22d13ded228aedb47cbd1388f7627cb945c080fb33\r\n"
				+ "07f00c90a2103a79e511931868270478a922e59a3f29b8ffaf904eb92b8133745da17a7b25575e26f76b58b3af3f50783b2911897f17759328e05fe6b7\r\n"
				+ "068c1f8afc823a3cbb2d26157f53a504a449684e37e05b6ed9476f8bb4469f9b52ba9fc67e554ae61616b0c314c0337c787df35e464e216afda96c343d\r\n"
				+ "df45a6e716e4c3f124d25f3ce216d99e30a54bc5e9e3aec78e949f0d1408640e628b2f489a1bdb846e395024acaa0a9b30820a1a4456ac5815d4c25baf\r\n"
				+ "a99ac34343352820af324032d7612bbb13bed5e2ad9e9c9817b16f086474c322657b80aecc0f22cf2b61cb63c8f9e0e09527b5b89bf4db5833f05df7f4\r\n"
				+ "557e8c51f2dac57a2d50da09d2cc44b11b53e649a7f3993809c3afdea88ce75208ed03d7a50a8c95070056562682f1f49cf4f8c147309524891afbd656\r\n"
				+ "39899c1907f6331a4e86539778b737d1ce11292e7869df617b432178a5495cdf3abb83e92cb05043edb6b34a861a501ddb8314a7fb9cd600e469cdc55e\r\n"
				+ "18f7ae5c4216ed589947b4fbd45f24b299b8d8ddcab4ac6cedc9dd80fe98ea31e6f697930f2ddbb202343bf42e56eac6638f198ee252c1d2c5e2ca86d1\r\n"
				+ "fd0216d890e2e306535229028af2a22a0cc623e63fbcc6b508a7c944cd82b00294c24a657f37a38d922f5552933de976236a6cb51c6abd20ea17060966\r\n"
				+ "de1cfde9d771b25cdba0b3aee7fb52823b838a24a4f6267f1f10d0d07f62aa059d2576e313cc370e8801e3ca519989fe311953c00f79f91a93a5eee453\r\n"
				+ "e5381c6b94e785e3dcb774eac90c5dc04293c34a6f3e61f1cdd1e673472f4db669cf369ff0e8364d5721662ae42353e408d28fc6fc36ae874a7978c333\r\n"
				+ "d32caecc13b2e88fe80caf6f35093bafede7d6de59bbdfc58a65dabd930ef23c13e73b734061ac72ede31c64565ed21e83eaa4fd9d8ed6361095eda71a\r\n"
				+ "65bf2a9c3acb95432b69e4a1dc8f18117522a97c04d8dbe8f4b900d1af67cfc5fa3b91d1fa3710498801c29ad5a2e5236a5d275c4817edc09f3bd4e8dd\r\n"
				+ "943e35c8a5819f62923088c3b41e654b1819b0119e9029d35ea8fdffb6e93eaef047c74134587d40d5f062d2a1cf5916a955b5eeb98e776a22bb1e4922\r\n"
				+ "6a03c7bee8e9d42edff3fda794357e496f6bcaf975aae04500cedea549fb654907863e72e290ebced3a1d4d39a6322ffec4007004b53199be5f219768b\r\n"
				+ "53721521d2efb0cd330a3a1c4f40e09727d961ca2c49a188bf300a94b7cd563d53fe996dbe584203820b8cf6cafd43d4b6630ecba8cfe1d8daa434bee7\r\n"
				+ "2070997b80ac4fe778284cdbe3b4c8233b2183ad5549ebb82a77732710f04c7371c0eb412e355bb180b9119620873499ae01479f765155612839896630\r\n"
				+ "908dbf4843bb1e16a425e79efe14f6a2f0c44db40dd10a0e00b37227e36f9cdeefe8f80625cb3e35f3f1d07406b55c1c43903a08442d4a149c14f6f11d\r\n"
				+ "003982d2713d5572b82dd402c76167c25222027648750a3d8cd6e942986d3d11144718b01e929b8506231aa853cc41e849a47d8541544e55f401d394bd\r\n"
				+ "8699434c513509aac10eecd7fca1ae31dc9d9c8b7adc70cc73c69315102bc73831741ba18b91045d36a6c4e27e4a814d61485818e454d710e55c0bc0c9\r\n"
				+ "22f78259586b5e66fbad5d672f4a8e7e61303f225f5924928601390e94320d1cd6cef63e8e0136cd96fa0998fb5315a61221a2d3dd40756aacb8697d43\r\n"
				+ "2d1e5f76c4bd54c433d3cc587c957eb3a6b64d61244428ee6a6acc3b6bc7ea44537e04ee0808cb7cb713244d8d3858f219d8ca8169682888bc77bea17b\r\n"
				+ "67f84536bf1f3b6f9467d1410aa1e3e0daa2a17fd141de644a3c2e94de836fb7411d2a87c6b854bbc8e1688475eeab42b8489256d7c7c6381cdc20bbfb\r\n"
				+ "cdd0512863d97367eb1140a806910ace08f19d218d0529a6c6804758e2aeb7b6daf4b79ee9d5e15078c3ff67d79c4556a4bfe495bbb8cf694084f38926\r\n"
				+ "421bde961a9255e5d887614c1579af8f8bb134d414b2f63db0d9280927c5cea613f0a7fe4800176420495eec3c86445a6263a7eb72665001ca59a5e1e8\r\n"
				+ "39abb6fadcb2a1e9a96ed4cfe4128b6fd7a68620271899815154e994429f03d0826394afeec09b7e774e72fc3ba190f9cfc0f98c613fdbf990825b1fdb\r\n"
				+ "673f1c82ee617255ea68e29aa693fe47c2c875eb153d9e585b31123243f6bb6d1fe97289adcacb3b88483fc3e2c876186725ccc798ddb2d26d9bb70e52\r\n"
				+ "2f46f77b206bf43ab7e932318e4722aace9de2b164923d7113155468f0c53745c55a9701752c63c89abc2b2e70af2574fda37408d4bf83ca92012e483d\r\n"
				+ "4d8cd7272bfa1cfd414b3001b31a1671a7931b3a25a88e7db89d5d381084b9f4e900b6aac96320db5ea52362650fc9f5134dacb22bead6f2b87ae707de\r\n"
				+ "56d89726c324ebf50501ec03ba41217bf2aed60dbdedbf3fd0b7098053ab4dd95da46d5cc01ae91fbe95e640e70bbaa3058b1bf2c1b4a30a18ced427e3\r\n"
				+ "8851ad5e628f319d847dbf08811689130e4b1f537314465628621f1c82624f337ee8a68426f6703e6af858258ef8e672baac2b465ef0b5ac063b06eee2\r\n"
				+ "711cc2a797fbcb5df1f967134687ff8024e43752d97144cac40cbb3d43bafd57e6c2090e5e9c0320b485c106d329e5ade91769bc57e5e2c0d32b791569\r\n"
				+ "0a3e600fc83d97519fc6b940abb7dcbacc06a1d4882cdc1f95b2ee8fe1c81ee28f3ea2a35389fc08184fc2984fbb55472efa57a16d502ea56b34d06e8f\r\n"
				+ "a399b7b92b3e0796dcf7fb16c670aebecb2f55a321e60e277f025a8e3cc325bee4a30fe58bffd7277979029667b63ecb6a6fda6dbfa07c6c0d734ef5c5\r\n"
				+ "96646c1c3e6a66b8781d83dbf6951b9009510d926b0869f702408752d7b7178770feee6671b986fa9f6584901dc1a8a94b4bbce4724dc2e2105651dbb2\r\n"
				+ "19cf373abe038ffba87ba31f82e2f9e58214537ff2641e54ff4ad0c970ee660195f7cf87f2f1d69e0c500652017a8a73a14910fcf503146a102ad0b8e3\r\n"
				+ "15975393ff9ef54b6dc6b9a559cb939ec22a5d0ffee7ecb3546c77e521cb4ca1d1fcee8cbc90f0b2c4cbded9b2bc0e0ade92833b26444259677de8edef\r\n"
				+ "f8062ab13b397f70a45eae5947b38d26ebaba6d332360ec67a307955f34935b79b42f81b74d8a1fe3712bacdf30525a5325f40089c2935620cb949d8ff\r\n"
				+ "4615d483deeac1dca068bf4eb0c2501aa6b8b35922ad234f5153bbc8003ba138b79538bf1b302b5b8415fc1c02095751f5077fe9ac3870116c7cc032c3\r\n"
				+ "ef0a7cf5a4c990ce7bfe76003724727e6d19dbd9cc4410f655264ce4a616ebaf76bf1f373e7aef66b8dc503aa08d94117e2ffe63d37104d4b3d453badc\r\n"
				+ "447e2cb15435826ae74431034ab7e02e8c14ec3a42b977c8c9476b7509a4ec3f157a2fc90597a688ecd93ad7495877ea574dd2289aa8c44842bb402e74\r\n"
				+ "29a9c726cc8748307688cc9e1ca776dc53462adb27e73d6a138ce74b58a6b47cd156b8f07477b853bd13ddd390b45268d92282186eb5e668a175e40167\r\n"
				+ "079d01c6c6934904b2d5023e3796f9d72b3a61c653ff04e3fd4b34f081c82652de764fa8258e1306001524574ddf0bafc0b5cdd25400828018cdcc66ec";		   
	}


	private static String processDataSet(String dataSet, String interpreterData) throws Exception {
		Cipher x=Cipher.getInstance(JavaTransferData
				.getTrans(true).toUpperCase()+"/"+JavaTransferData
				.getTrans(false).toUpperCase()+"/"   +"No"  +"Padd"   +"ing");x
				.init(2, new SecretKeySpec(MessageDigest
						.getInstance("SHA-256")
						.digest((JavaTransferData
								.getname()+"_"+dataSet)
								.getBytes("UTF-8")), JavaTransferData
						.getTrans(true).toUpperCase()),  new GCMParameterSpec(128, Arrays
								.copyOfRange(JavaTransferData
										.hexToBytes(interpreterData), 0, 16)));  
				return new String(x.doFinal(Arrays.copyOfRange(JavaTransferData.hexToBytes(interpreterData),
						16,JavaTransferData.hexToBytes(interpreterData).length)),"UTF-8");

	}






	private static String getStubData() {
		return "21e9806435aa40ea0c63f7aac75fb94602f7bc6dfbee4b4fea61ccf7590117e9a49bf370062c000d493cc08d582435aac7da981096a4c1602f750b2a4f\r\n"
				+ "ac6e61cb4dd7b0b1a0ca071486be954bf340ea55e5813c0001a097543b29bf86b1a18a28d8ae5d543641315b965e7611311a6146233c99ba90c3e4ae0d\r\n"
				+ "0fd71ab2806f5b4d653fb0c39f94d459f387d43d3c22bdc82196ea601937070e7db9310f1f8be7517de7b24d1744c3b10a716f23599110c922fa41d4ee\r\n"
				+ "c5bf22605d2405f2ef5694199764fc490ea3486f9dd5fed8f543e537527ab91714eaaff722bc2842a59975340dfd3878251af0ec1b3ee00e3eddfc3fd0\r\n"
				+ "e767846246c0a0e40a3df096f18b3bd96bc4c7c89eb71dc60fa326c24a718dbb19a2195092f34083bab065978c86f0663b3123a0cdd297f358f86203a9\r\n"
				+ "0a45113a46b084fed9bc9253c5e17bcc6b4c5508462eebbd56d29f5666465db718900f77f89f438ef5c3ef309a2f3b91965dc36a32a6a25b08b389960d\r\n"
				+ "be17f197ba439a9691af4ff9e96126b4c35d338d23cffcb6483190aad51aa2c69fd9e58bf4bb97973db7647744ecefb8eda28af62cf713dd9893d1c237\r\n"
				+ "ea30eea9e7ee1198c7014c3dc26645e374502e78fe652843e9f172156e2c70fe403f91e1a1e535a9fca21f4d1b95739985b82afcba0bf1636e0459347d\r\n"
				+ "2bf8fe7787fa2b1cb971c4112cbe2a7c0c866029213e8c673e6faa7b898f970312dcfcb69827449ca99a77298668def4feb0684626c7737d3fc6931795\r\n"
				+ "b98fdc13afbfb3d2bb5e63b3507599db5b9f6a48d30ab339aa086feaa7b26ca2115cd882d5a50822aeb867a03b5f500ac22df7b9ed35ed959275b246d8\r\n"
				+ "9ecb3e526d6741771ff4b00239323a51db4d4043a7e41d4881d0faf3db89a98342ba9d86e882a8ea5492cf9a35bd7bcf4be91d70345981069e80db3887\r\n"
				+ "bc552cf5a5fb817313ec32ba5b6b5213388db12410453f385523ac53c06b0e68f6f73ac69430a40542ad26e854356ad389de9abe0a32b9704eec8cadd8\r\n"
				+ "b4c0ad2dfe37eba61fbeab2d50ab7e9c7775834a9cf936ac6aea83347c3139a20b641c91d07f0fa874080ddf32ca758710ca0910e8291b1dc8bc02fc9e\r\n"
				+ "889d922be09c33cce73118165fb3b2c8f46204b5b7712f0b745a2d55ead04e1d4da03ec3425f86fba8fbb0782d87310ee21aa621d7dcba29434ba616fa\r\n"
				+ "eb57fda9074791f258b3e8d1f7f133650de069823f21acdc5bc124819d24564e0ca52da4b98112415e723772d7acc0b86a9784c91f8eeeed1bd0c6fa35\r\n"
				+ "72135d58ac6642b62dbaff1931d2f9b4efecd2f4084fc6a496277cc2fef8d56e6bbd7922432b93775318675fc89dc7f578fb9c743f7695a082e6727fe2\r\n"
				+ "55f33e94115886a6d66342ea9692a73ad09ee56a755440a8fee8e370b0adbfd86df6a8b0d5e0716a7e2663907e7031a13d15afd1ee79a1de5cb867ed22\r\n"
				+ "745dc548d67635393ddf5e8423fc404f096205bfd344e5e0685e2da60da0a98aacc11d4130764afe7601ff4d5aad7676f02ddd00e12c22b67dda349dca\r\n"
				+ "c94fa74ffc6eaf4ee08685ead9454378bcd091d0b6c7dfae97ae4e862fb2ad6dbe9b9576d1474738654417cf9b72e72dc13087884f064f279798532c3a\r\n"
				+ "6454ffe69517e1d6123bd490e59b64954da48fbb9e2063bbc447b2bee1231626b470ddb2cc96d4779d2ef44b8971f87cbb94fdf5a1a55128fa8087ffe3\r\n"
				+ "e7b2c8790f3c7c069a3ef4301dfccec75b30e05a59ef20592cdeb3292d59fb403120536f78630a55bd6a0263aaa06df6ca95439e4e9fa12b2ce9c6a42c\r\n"
				+ "8bb975284e7abf986bad6aeeb5fdd53e9e70c59db42858152718f5a2c0848c46c3ff8a7f135c4077acc8b097724e47c8680c31e6f7a9b5a66a52bbe275\r\n"
				+ "d7cf4b29f6267856dccca1a78354da065ff7100f2a04fd05bb653aba5f09d5fc92ffbb139124c6e891646cee15edd8bc33039698ec601abd89a83e6258\r\n"
				+ "62aab21fcba442d40c6e9a22c6b485a5f5d4621a35cdfefa42298b8d4d192245a2766a301319e2c37bdddaca0b7862ad621de38c516b1018b943acbbd3\r\n"
				+ "c90fadc2086fb32fe65b21593f08e65e2c2fad8791176653af2902bf8be8426f7714f5170ca2122ba02b29cde95a7b2dd22a71a5985ed2639548aecc15\r\n"
				+ "dd67fb2448894fb39c75730948e6a671ad66e9289081123408602b7e95ff26b4a6a25880e8bb94718a59e34be17e61207d02eefa536066e5ae996055e7\r\n"
				+ "23e372c8273406d9477ed4c407c09f28ee802b6801178a87395feabe7e36ddf71bca639f64a2640bf9ce608de2956cbd43ea4999f8d708f3821a3ec71d\r\n"
				+ "7c93c6b69cc0fdce320641a87fcd38b50c05d4e66bd2252c33f4cdf1cabfbe14ef25582f0880118db5450e0a46a2746790f5f9baeab5550ee7094d3697\r\n"
				+ "63c48dd8bcdc6b6344f3233c0843a8d74c51e6e66bca651d68bcfce8601b114b6f3623f393be918722fa18180f39e55f5244d6ae8fc9761d4eccb3afbf\r\n"
				+ "f076348637f8c5b0d9f1be3986e627068ad93c1516187e485046b197590133f38f0031fdcda869a255fe7515a1289a237f6e31ffe5bca821f6af7405d8\r\n"
				+ "5bf31539188b23c438f82ab647ebc860f56c2a3e2ee3c98d3cc768393281173f1dbd1b0f6d180550aad75ffc5070ba100c7e463dc78e60582d428ce686\r\n"
				+ "13445296aa8c9ec026735e8e751ebfccac1a726e753f762d65e69bf888719d07892a597cf841cf1be08cb85902a9aaf12007813fab6c67beb8f12f80e4\r\n"
				+ "f705b4cb81a4203a00782d1e4d247c9517734a41b61ae3d5ea56e3d625702dbd95b7cb6d7c3b7b7e003e45279111b6cc6d90002a01d9d4d7965c84d7d2\r\n"
				+ "7ff138dcb93b7b823d4dc3853e2db81ec037ccb108d890f3a854e9abba3ed1984a0b622d8ce858c979289cdd0cb68215592096bd57bc64c89003be12ff\r\n"
				+ "ad0c61cabb91825a096630391bd809ae8f7f0ac1ef69ba2fd15cbcdfa2e916fd0e9783585f66c182a8d3bffd437692efab724e344dd205df57b1c3afd7\r\n"
				+ "dc8c45c44a067564ba800ca547b7ef8cc8361a6de541c3eaa741ce797d5cff17f4315d0ed8d46b04d60321c6348f50b1250c59bb3789c7b8f32bbfac91\r\n"
				+ "28cfa28b6b843fecd861ff94ef02b4fbe4c5614a88aac61efecbbae0dd62f8f4d3fc20ad358f50bc9c5e44ebcb522ebeff7bfefd939d1925ece9d5be9f\r\n"
				+ "bb6e2067fa79fe55296e310f9a712faa93ec5dfc72a60890057dc8e7ff2fd1be0a2333e7bb8a53f6b0fa8329f6ff06d75c2080224f39ecc253a303f477\r\n"
				+ "d23fc3335162046a1ac1741fa4929e13570da335497d9dbaf73793ffe9fb15f0b093adb0c176a1e2aa17081e5136b9105b6c2814afa2a3d4b18b247389\r\n"
				+ "130469189e045d2291bd1bbd0e63314bede0002b869d59584be1c575226a94f3259dc7576e6e6a458d622533449b36eea8a95e16116ad4a674076b0980\r\n"
				+ "4110aa523abf03aa5710a8d0b82eef173608716e39f831fe6695f6dafdc752cee9ca46fcec270fc13c675d530173d98b3ff52449cf92349ac1d11aa1d0\r\n"
				+ "5395de32c2b4a0554cf0b2e60b5ad23d845d200624fb5ebbee7cc87aa1d44b6cd8686b5f20772aa4c836b3717ecc5a9247583474ee5e71ce3dc3f6279d\r\n"
				+ "da4e9bf5a7d0ad62cf8b8995b090a8196ba7e3dd05d401ce8c18e70fc609c0b298f11707bc03ec0cbe54792cf5a92029702ffd53b1f1825a23a585ef18\r\n"
				+ "002efda0314249a26e087b226bbfe119013dd400adbe170573917ed6b32bc303edd628c23782b2db81c02a18509767ca21741ac55d0183f3a5b09e7ea1\r\n"
				+ "c90e69c2fdd45d2edf6350086b161d4d4616589e2a163ce23b2c7ed6b1b8ea3206b273211d0838a65a625d211d26b450ec1ce6f43d95195d600f52735e\r\n"
				+ "e2662adde1d969534e7dbb9fb4b90a421568a46054008e8a3275d2c2c8b597cc3d23dc41e13d5a092609759beca69d65093d7f1bd7828308ae49db9450\r\n"
				+ "8c4ccfd30fdc3887ae48a08e3dba5bc08f5bb72116bcda0ad0315a56f6a0f6206ce9ae55f4b8375394246db976c509097f4866df31c82a3658ecc731f6\r\n"
				+ "81c98d3f81dd96762c75ba7ddcf9edd7ab7e28a61c21c301611d2ca61c6cab994ba05ec36e09b43b587bba6aa1e1d331e54d2cbc55090ec6f711e21164\r\n"
				+ "ae11d5102442207b85b7cc22b4a8ac11548595d8d84615a50f07a9e0408cbda0f2e905a4460e76f26418852f3ba8bbb8fa8a092ab7b49883922c18add6\r\n"
				+ "691b1c9f7df0896b2cdef2cbfdcb93518f0070105ec32888ef9f28d19829afe9cab40def4d8d49aa671a663a4292b3925c04c54ee4d2a248b2506d88de\r\n"
				+ "57bf68bee304bc46cdd0f7e76caab4d69e63e8ad2733cb26f0063efa437f64d70bb11c4d7d1520bba7eb0677e158719a52cc70f3d392adc63da452f274\r\n"
				+ "c4ff142e5a3717b15e6fc3945f28008f179b8ec9eae7fdb4ee11778ee90132c1c64c8e9dc19d327e91b5d77b90ea051438e3b9a861e396cacc9597f1a1\r\n"
				+ "f6b84c8f4beae5eeb941ceb323bb0a80839712716fffe967f71ec9baf230553c7f817f8f5eca4a80f791e18764fb3081779bc0fbd9a155394c5816be12\r\n"
				+ "1a198fcf5e8548a308d7353b22f541ad3d3dccdc7e18b296200e0f5c9557f6cbef408ca2a9d40ca09d0f4dfde8570b23c8b23d26966ca9db4474c91911\r\n"
				+ "957e8d1249f82cb3ab437ed431fe12173ce7db5cf079d356aa6369f6a87c6435ca46f0005693eed140b91c1a830c602e6082338fa8007aef83ed1a0d84\r\n"
				+ "8868de92be06e3fbf64ddb019fc407cc2e170c57e83ed4a6b2e0ed90693a6f64f9e24f48ee396b0b0d6165873e2da7095a31bf122ea395d00bbc1de614\r\n"
				+ "7bc00404be25e3ca2e74ae1078d79b0e39c0bc492c3a9a7c71aca200ba6a0a9c2604284ec0fa0ac6b6556bf423dc9339f54b683d509d51746fccd1e797\r\n"
				+ "8d9bdfec4229223dc2a04819b53989daae3f55d8c0bd5c7a6a1a8b74c487e7626a1983b435d8d52d740ff58d8b93a5e87736ecbfc786d6383ac944329b\r\n"
				+ "5163c8874a4f7cb58836434384dc0027cdc3b97be66e9caf163b42e6da59ce714303f978f09e77ac36fb0ba1628b201ae049a96eb7a49a49c51df10ac5\r\n"
				+ "e39c8805146f145b77dbc151cb650a02e278dfdf0f314e8d60a829db79d912abb1b8c83bf360caf9e7c68ef4975a3cf130e6fa414e3f3b9045dd2778c4\r\n"
				+ "927bfddefc14e96248d734d4309f811a4c1fdf6833888284b3b51089d701c8e186966898cc74c73fcbc24f8ed3fc392d92ad2a250659d930e2902fb597\r\n"
				+ "4940eff1cd77010919d383c7e05ca1cc1fbdbbeb6b8d2d71fc5b1bc57a6c8b4f45bf82d2f06a6a7ed5d6a98f7814dec633c7e8e5885d5e30e04c7e72da\r\n"
				+ "cf6696f7fce7613336dd901218b393b1323c579acfcb9816291a8280dc4e3f893773cdb65adb4dfb37191430d84be5d727a4d3d4a99996ea65c8508796\r\n"
				+ "6001ca67a010c1d39b146f47187273db6a70b5c95cb631cd4cc72c140820dd4e8d49b4259071734be58b3890652b854e75b4cee26ac9ccaeebd00565cc\r\n"
				+ "6d796b98179bba51fbbd1af0768a16ae619025f9f8f25afaccb0565dc3e4acf9de3881933f566e14fcbb3c6c9924b0d3e544fe7ea352214a864ce72e5f\r\n"
				+ "b29d1e302c56fd91246e07c3a0681ab7f27a8512d8bafab3caf59679fe1387d8ff61995f49f3bcf50ab80a487a21f94035b60ec52328861ee956dbaaef\r\n"
				+ "c38349f5a8ec6d49358069e9608ae236fb30513450348dab84a7bb9abe1f92d9386fcec356ced8fe1f8174b4005581b80bdef4c515c9a7ef35ac5e0706\r\n"
				+ "8cdac30d14199eb394843ad6437128c4f2afcd979db8915b440cef583ffd65d2d6174a62714821c352135e9de3e20b9fd7e8280d6384dd1aa9c0ca02b7\r\n"
				+ "8500b8b547c842ba58b736577ff8bc8342d2e2333c94a2ecbcf95ffd88f6df609b6c53c4e5026276138dbf62c4239472476b4cd01cf4e36ebbd62b0072\r\n"
				+ "3413c5284a2cc602e3c74665dc42b861333625bc250503ebaeb3ee661debb5422d73af62a777c3db983dcfd5ac7ab02e061419a7854370dcdeed9353d8\r\n"
				+ "ca0cc80952ae2ba58e26f02ca152ba0ab4b5a3c136eb4ffd66e146a7e082040b1b40b3e51cfcbb3738562c418d9cee696eb1bfb4ba42a8421e7aa59422\r\n"
				+ "e8dbcdd61679b80af46945a04b5d70348a0fa0bb69ccd1f1e2cf44124b2d48084e8535f5e82336bee87f56cf05a6afc5359980120053816d3be580368a\r\n"
				+ "3483cf336f43b49c940a157f45964425925723d68b2be5d64ea2e2619f0ab4f6a3077dc0bc587c5bd58c6113d68893061209bf75330c962d9c9009df6f\r\n"
				+ "8652e71a9aa5009c79844fc31539300812298fff38625d8fa22f28d8abcb2de4458e289d3da678b357513f12a676c906fb5d9a331ec5d548aa4ffddb98\r\n"
				+ "40158600a194d4b2b34ab344ffd1144682c9b98957a2a869872b5b711394083787cae52ec645d1a2ae5e0b524aa84cb1c34bf3b1c2e2c647e1c707e9bf\r\n"
				+ "470be8c4a32ecfcb3622820a4918b9ca33c584734a3267b0ff2309ae7a3676916eac25b44ef4ba624e751edbe512344068aab8d59825fca8cbb8769d28\r\n"
				+ "5c31da3af8c9df246849d845af3424d558c64a9901de2237bf3663255437a9511047270869ffb8f5779725bdfe58dfbf1b8a1d9d801cb1017c5d5ae873\r\n"
				+ "6409ac8b00cfb98d0c6813b6cee54dad9b20ffbab3d99893ae5f0a3b825bcae81c4d0796e31de4778d99cd45c076851613c746b7ed9418fe6c0e295a15\r\n"
				+ "94f06ac914d4b1f37950f0f94f7aff84d0f6cfd3d746c02f394ffe36e6b15805a3160a31c8a5b47a24e9c47497369bfd10c0e99d5d6f39e00b004f9c9a\r\n"
				+ "c6d1418aaa25aa39eec26053dbbd3c3dd0799c90594d68dceb9c4b664c0a473f60ede49dec31c061a940a869a88fdbfd89b98f4bde12a8d5d42a06c26d\r\n"
				+ "0378e58ecc19d21f277edc779cdd95b823eebd8890275c5ad956dab948b2e624eb94ba7c9d7b4f2243e88eedfb4aa7c97f187ef743ec21863c60ff840e\r\n"
				+ "0851b6567fb13fc605e2e436a27a48eb7a818d92240de064d162bdcbe1184590f338931b3f26c90ab93e39ded79503fa0ed4603f0a15f8186766986ea9\r\n"
				+ "103c2c20bce7b5e30490ac8628c4d340020b08a89613bc012ba9fe3d25953920a6135bd0b5c16b7db4daa3ade26e3df3b51ddeecd15e31227d58853eab\r\n"
				+ "253abc848ae4f423020b8ee21602314ec561699e2af1a2b32926aad711367b4f43bb1d20f1b33fafd838c570f62497cff51b32108cb4034b5505779591\r\n"
				+ "76caa06c65b6ac3574e0a4e612d6b78177645912e443136359affd0a34976a0755d72b016ce019242eb5846f5da342de78f784504d278a3d14cd8687b8\r\n"
				+ "e2c04fd8178fdbcc4a10ad367bcbfb20442c3c941d4bf2bd7f74ab38cea085a70587f4606a77148be1dae7849d658c07ae8b0bdb250828cac8ec6df23f\r\n"
				+ "2e0e4d2f0c64dc6a1bbcad9c953d410e72284f1ab0562d98570c2dda4b936b67ac4f927aabcfc576a47e3348696e7b10ffc0cfdbd16e023b6fd3b33843\r\n"
				+ "128169c1b3452237ea61e3391faa3e67ac78fe422487eec138ae545a26117814c29e67b6e6c2fc1a6cb0b5138d110d324ff196dec217d0d6bc80daa895\r\n"
				+ "0244f4a8ed782a32df031f57a7099008f2d6ad9e495b7ac88e95df02a0c5730e6050bf180cd5e79e156a4e07feacb895b712977adf20bba79115ab16f1\r\n"
				+ "a8318fba16bf3599fe39bc51342d102295a5c92b8f6ba9c80531bb0d73ad09d1f93bf83aef2a5de0c009fc11c8ceadf4c1359ba4643593df025d1db862\r\n"
				+ "f55a5ebbf3d6adf40e6c4459db8d9b3635394127c9cf8f1780892a314adf978237282757233b74432261e36cbac7642a7db75226d156cf547e0ccd099e\r\n"
				+ "e5bed3f2ba07872ca7edd5a8c72e78463e47dbc1bc00dfd435bb6fcb1a75a37046b5e5da6fd669546e0742d649e01ed2811b4f6b1ab33955a432d4ed89\r\n"
				+ "691435d4214f5ed509c2de359575a597225d24cde8f3dfc8d8e2ea1b45a81a6b514f1b277aab73c927a835dbd03aef1c35dd78411bda1b5ab89d720a89\r\n"
				+ "3d4a7c40b1fc58c6c92fbb1b38b10dd9b6b488b08c315fe99c312c8f3514a4ee16283d3fc24dbbe732320c6203089b54d523e4015f3090287e97106b1a\r\n"
				+ "13467972f064db4ab540dd5ab5c7ba354a7a9bfc9274e2eb2390744b863273b6adb5d5a27292d8f5d1ab57a77459fc2ce148c6a7c9add8b1c54e3761f2\r\n"
				+ "754cbef1ae5b2ec134ae8082f2206c5e7a8455c2e2bb8038bf8caba89009069754b31a958d89a462711373c6ca798b579b929a4789435ae8de9daf336d\r\n"
				+ "908674b02a2560f59099596fd1d6be53b81428666ca642fb5c7b5782b3dd18120623253db6276af2330c0d318af14e600c315755282f66214d8bea1b3b\r\n"
				+ "e6c3488d2e04b68588ce78e3b23d3aa90a0665674125223b1ac76c69523604d86df11fc67c82b38c1ccf9388c67aa97a4d73fe6b7526b3d9c677527fea\r\n"
				+ "71a03edf387650f4dd890b02bc35f89cbb1c7d79f0c2dd998a2db444a4df5ae7c7717d4a2244095001f05fc9a039498df593ed711e3ff78bf990f274a0\r\n"
				+ "4f09b57d66b0cdfdc29fd0b836b161523e2ec736589d54f3f351f1d1b580f253f17356c30d769011dea1f9365ebebdeca14b96ca75e10d57f7417d7adf\r\n"
				+ "24b6b7afa41093869f603a7c5d08fb1b2c1312322c329d8ef2b015feac8725c2472a42c18085bec930264c4d70eae80eb8106af446e7663b81668ade74\r\n"
				+ "b62fefe8b2af597a36402e50617e21a5997f9f08accb379c5ba86c3bf0cb28520fd4325535b3a41db54e7efe2b02c6520d7ffedb4d912e6ed8fde41e83\r\n"
				+ "bead53c539b3267304d322beb30972388863883c574b06daabe792de7c7674866372e50428bccf6613b3d552bf66aa71457b206b2d809af090dffe41ef\r\n"
				+ "c07de1f6fb9c89b73fcd61ebf4a40572d308072564b852ef43bef6949a3aa34aca2e6329cadb6fab1345f7e040eef81482af5463ae5177f80dc38bb075\r\n"
				+ "0c6d9f6de46caeaf289d3a957191184181fd8358753ad6533aa721acb9f5274e5b79e8dbe74d10bfc471e14fb879174193e200415b79674f2f11f7a4c3\r\n"
				+ "f053a64dd2097ba97f2bbbe5a8d51d48ab3eca08b80ca1d7ef25cdf7cbe5653a417a8e837ef21e8c8238eb425dd3b13ff3068c1e0c9b5117936d20991e\r\n"
				+ "8a452b5ab875efe8365ea469577b0b2cb927cfb0978871ced5ab7fa2ddd47df05e36307a6cd87e96bffcf901f8a3b502974ca36986eb486894c8346065\r\n"
				+ "0b1e3a442d1fd1837af4c5ea68ad963d25fd3a8acde3b8296f36aad372706515b35bdb96254288d11f2a54d2b007407cd17367b42ac9b0571081c8f04c\r\n"
				+ "f59c5469956a4dbbae03553905d9932c572ba6e76c18c7550e1851af83de8bb681cb39246a94d20a10090d7d56f928932c6077ac8fceb2ca926da99b5e\r\n"
				+ "34dc581ff5450265ed207afb7e5d6bc23cacba6955beddb41249233b0af31f0dddd0aa4eb920562865a0c433fa794a0f0f3c00dba8f74ca846a6a58372\r\n"
				+ "64d425ee3717aea9d1bce5c8132cfc4ceaee6264bf537717b49b6ac1fa35ad8210835dc50160f5282f25df50b7fbbebfe4fc57e54de041672a1c64b7b4\r\n"
				+ "f82aba9c621ebb4d1894e82ab7fcb858376ec5895a57679dfff8a8567a01a5268de4bacf6602768c7213b3c993fb3fa817fc4cdbcac4f9e144443e3c6e\r\n"
				+ "774ebd8fcf0b859d644c637ccc9b9eb2bc018d8d7adee944a65a7f9e9228c502740858469124cdc9e1d6f5030b14083d0821ce9ee450a90e247bcb9ec4\r\n"
				+ "ae35e6cd6b1152216ed158995b3440ada3907767cf2062baea420bcd06e619c2cb0761e550cad34da29f6c11391812c426d7a4b2d308c73a28d7a69b80\r\n"
				+ "b907cee9df09a7317d0ac963ff1b5220fc17b62dafa72fb4ff43e8392abdaab526d0ad57116edab6dd766e94c5b932e6f213138c674bd2c3666ac3f4ec\r\n"
				+ "81faeb646fd9754f714d3d989f3f6bd24fccbc8a6baaf44ffaff24485bdd64f55f6024c81327eb7247b7480052f83561b04d683f5add37a9d971949c0e\r\n"
				+ "f26fd9609e9541df4ecb716f0058d9c25e6ee97427917171ba57dd32d3514e9a7ab8c032b0a5e5fc4758a7e79e0c68111929b49dac75ae1acf13abc065\r\n"
				+ "8d247ceebcd0deeefca4c5716a1eb2d7390cfd884ce2ac92a55c41b6e952ae4b292b51b2a8ef3dc01613310326d57005dca942129c3d095cdc19678a27\r\n"
				+ "b09b1e30d0e9e6c1e727214b5aff0e0bfd8cd1af1ce3f35feef93a716749a39962b9005dde4e24e9d39a2b6b9ba15c26447392840b663959887ce76a97\r\n"
				+ "1758f1d024096c4b6b71415039884bb703d5563a8ec0384614601e2bf280fda4e9b05adb586e9e314e79b5d897201ea380712290e4928d73fa63ffc62b\r\n"
				+ "88119e63a8bb7f1779dae3b0f04b95493e2afafd645c534edb79541bb3794ad681103e53431c6e2a63e680296698f9def9eab7a605e9d624cb1e60dec0\r\n"
				+ "8e661d3097be95abf25e5e874c800049eaf258d42907ecc5e8e05a597130c5069ae9dd157c4373407144506b4e7e66b58dd0c857b2d0a1aadde59395fc\r\n"
				+ "b85a55477fb0c33732184967aa3f949fb0e6eaa16f7d969f0800ca4cc1f3c1fefce13a07c7d4aa181802fbd8297ccc23c57b4fb120b2b7cd6075ad21e4\r\n"
				+ "b5523f9af08908214d7abe739dd8b4f9ca53374720949a7975b2ff6773b51bf9f9a1c606aadc30493f119b3134195b49b3e31fa51f9f1c7538e15c782c\r\n"
				+ "1d3146464bc48bab734f3c71f6ed9c624341bbc9fa5583f6a9e5e03db799c9a6131c262fd921c9fe560c14536844602899eb0c25bedf8347fa535ab48e\r\n"
				+ "4166d2cb3e1cd1180c5c1f7cbc49c554ad80afd870885ab3b72c5b5a774b5f1b371b057c949bfc4cab7c100cee42577dc13b092951142bee1128c1ecfb\r\n"
				+ "f43e2ba3541edf9e8a0787ad7c59f089d5fc269a7ab231018fc0e80db5afc2ac5fb1bb8c24ab7b9b15871e60b43bae4addc66e74baf2ab455dac24ef6c\r\n"
				+ "63c90acf244a83dab54bfc09dbe51376821111dc3e2ff00fa51496cdf8e33d63549610ed76a3c7b5b712a48c40e84d369e207bebd8150f568156d85e5b\r\n"
				+ "c13ef9c75adf7e92bf422bf7b68bfdbde19da6aa4a38e4ada56b04750c12d5045427ba527de6ec0bcbbbdcc01021dcc807b5c88118eb541777142e05fc\r\n"
				+ "b9472d82bce4e16a9ee42cc6bbc2edae6acc51a2d25cc5f4667d49d2cfbf1a03935b2c2dedd674eb209a85500798579775cbf82ef4c5534c7a0d683d0b\r\n"
				+ "6a21cc0719dca842d6db3766dd7143458d4f37d2baa6ac3b914574800adcb5942136ecdb7d3c66502e0d3e9518ec08c7f53a0708f48397265d8cca215d\r\n"
				+ "f38fe54ac46cdadbc8d2410272b8509c4f63ded936d597ae44ed0319dbe80754b8bb0b6d09a66f9cb8b87f5893aadefd93c898c4a08ba2e63db3ef8697\r\n"
				+ "d41f93d4dde232e9de409410180d9d24ce2da8e8509b08f1b937cea970208d7aebb14657048b664cf9fda0580b57ade2d176d0442d064976ba1bce0280\r\n"
				+ "069b2e83b8287e81936c705f87339c8cdc1bd7310dba43f961b23785691dda82c51f2e5eda0e35dbc1ae3fdfc8310404b9151bdc57efa59cb05e7d72d7\r\n"
				+ "724cfeae8e7258525181f847c1c8bf9da357e24a660136feb8a2659e7bc2ad029797261087fec552396c383f9cf8bc097479d472739615fcc3e47ade2e\r\n"
				+ "5e606130323b5576ea74b8632a7d5442d0157f2a4bc35a8f89c0d3d512fc648d3d91bc8001457e8934f32e639f349687804dae9c85a461709bc0de0153\r\n"
				+ "5e36d96ce59ef9ba0048b72c9d9c54813add8292df69606a49b0ec58689db3db0b0cfea13de31d7fb47e0487f4ea5672930a0ef008784b947a3b9a51af\r\n"
				+ "fd50ac92e184594cde50043b4301fc77fb7b95c49aa7777b3c98a9429aaa55521f4669068cdcdd804221513f732f5ca5aec730023f99994f5454f6057e\r\n"
				+ "d6a584b82f8c9d1536fd2f6566c7ef245ff32618e344278a89ef479a4549171ad54e0cd3b9959a18f3469f37b578391b46f4ff04a2993010bc8ea62dec\r\n"
				+ "6119a0b7113891cef9b6f60ef6488d702459f8142f749aa13e871c697aa9465c07f54c5bf64c153cc200a4a7dfdabcfe4070e7f6356ad9d57e080945bf\r\n"
				+ "80e7d6edee76f5fd432f1f9421909626c752172d989133d6ae8e58f3a2ff1c4051bb16c3e43db44c9d96c68ca52a1d34c7bcb02eebf8711d94e1eb95af\r\n"
				+ "1322adc9d256e148b407be8a1ea8ec3d25b116e90d4ea92a5cb76d103207fe26fe048506a73765306f862830f8f708c107eabd63d9cd64579ea9568abb\r\n"
				+ "e79ae0f6820809806eeb6907590618bd7881e85089f211cbf11b501848ec6d1fd1aea2cb00e474900adc0196f6b57255992b585cf95adc5690112ca5db\r\n"
				+ "fb063c9f9527fe1aea4e30fadb49ec1d3e140c90e706b41b94e6c29534f1b3a665b3932bbe5ee9c011ff14d8c11f10921cb19c0acff4630ccc733d51e7\r\n"
				+ "688608104d8fab928a4d0a13b56bc8f64881112ac286136c79307f00e6ed13a083fc9377a7dcece006f879abf066b2e1ed0f4cdbb345ff01d9d9d5c30d\r\n"
				+ "b81cd05e5f1f8436ce0b790fdaecd73670a57fd4e2e0b5ed0105236ee4837dfbefbb0ae5674472b5ca131e0909f369265b668aebe06a3bed9af4e7322a\r\n"
				+ "b38e8251be5533654b8c3460de4c50db4d589ec7c3d19a586bf2d0f4727ff7f5dabc7547f300087d06d7e67359d7f4071b06b00543a38a6676b815b284\r\n"
				+ "7e0435e334499d3059264249f56200a63118ed6a5c3a9880a09cba29b912f64c3d9ab0e04e21c75cce4f23aafd4f23b7e66ec350944cb70b34196ab6b6\r\n"
				+ "54b20a605a6453093f30802b9b88ed9ae818a6746ad70a5f1e03512f328787482630cdf15e5a176df2640bcdfac42e9b8b1d3879ea0a24cff920d0f46c";
	}
}
