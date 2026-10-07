package specializedAutomation_Utility;
import com.tyss.optimize.common.util.*;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class DictateAnswer implements Nlp {
    @InputParams({ @InputParam(name = "Question", type = "java.lang.String") })

    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {

        Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
        String question = (String) programElementsInput.get("Question");
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        String answer = "";
        try {
            // Your program element business logic starts here ...
            if (question.equalsIgnoreCase("Can you explain the main architectural concepts behind React and how\n"
                    + "you implement them in a production QA environment?")) {

                answer = "React is based on reusable components, state, props, Virtual DOM, and one-way data flow. These concepts make applications fast and maintainable. As a QA engineer, I test React applications by validating UI changes, component behavior, API integration, and complete user workflows using Playwright. I also ensure dynamic elements load correctly by using Playwright's built-in waiting and assertions.";

            } else if (question
                    .equalsIgnoreCase("Tell me about a challenging technical problem you solved while working\n"
                            + "with Automation. How did you debug and optimize it?")) {
                
                answer = "One challenge I faced was automating a dynamic web application where elements loaded asynchronously, causing intermittent test failures. I debugged it using Playwright traces and logs, identified synchronization issues, replaced fixed waits with Playwright's auto-waiting and explicit assertions, and improved the locators. This made the tests much more stable and reduced flaky failures.";

            } else if (question
                    .equalsIgnoreCase("Suppose we need to design a feature that is highly concurrent. How would\n"
                            + "you design the data layers and cache system to handle millions of requests using\n"
                            + "React?")) {
                
                answer = "React is mainly used for the UI. For a highly concurrent system, I would keep the frontend lightweight, fetch data through APIs, use caching (like browser cache or React Query), paginate or lazy load data, and let the backend handle concurrency, databases, and distributed caching (such as Redis) to support millions of requests.";

            } else if (question
                    .equalsIgnoreCase("Implement a function that handles data transformations. Given a complex\n"
                            + "payload, optimize the parsing logic for memory efficiency.")) {
                
                answer = "I would process the payload in small chunks, avoid unnecessary object copies, use only the required fields, and optimize loops to reduce memory usage and improve performance.";

            } else if (question.equalsIgnoreCase("Where do you see yourself in 3 years, and how does joining our\n"
                    + "engineering team align with your career goals?")) {
                
                answer = "In 3 years, I see myself as a strong Automation QA Engineer, contributing to scalable testing solutions and mentoring others. Joining your engineering team will help me learn, grow, and work on challenging real-world projects.";

            }

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
                    "powershell -Command \"Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('"+answer+"')\"");

            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
            nlpResponseModel.setStatus(CommonConstants.pass);
        } catch (Exception e) {
            // Your program element Exception handling goes here ...
            nlpResponseModel.setStatus(CommonConstants.fail);
            nlpResponseModel.setMessage(e.getMessage());
            throw new NlpException(e.getMessage());

        }

        // Your program element business logic ends here ...
        return nlpResponseModel;
    }

}